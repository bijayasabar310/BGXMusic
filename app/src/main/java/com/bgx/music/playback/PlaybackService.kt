package com.bgx.music.playback

import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.bgx.music.MainActivity
import com.bgx.music.data.local.entity.Song
import com.bgx.music.data.preferences.RepeatMode
import com.bgx.music.data.preferences.UserPreferences
import com.bgx.music.data.repository.MusicRepository
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@AndroidEntryPoint
class PlaybackService : MediaSessionService() {

    @Inject lateinit var musicRepository: MusicRepository
    @Inject lateinit var userPreferences: UserPreferences
    @Inject lateinit var queueManager: QueueManager
    @Inject lateinit var audioEffects: AudioEffectsController
    @Inject lateinit var sleepTimer: SleepTimerManager

    private var mediaSession: MediaSession? = null
    private lateinit var player: ExoPlayer
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private var playCountedForCurrentItem = false
    private var crossfadeMs = 0

    override fun onCreate() {
        super.onCreate()

        player = ExoPlayer.Builder(this)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .build(),
                true
            )
            .setHandleAudioBecomingNoisy(true)
            .setSkipSilenceEnabled(false)
            .build()

        player.addListener(playerListener)

        val sessionActivity = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        mediaSession = MediaSession.Builder(this, player)
            .setCallback(sessionCallback)
            .setSessionActivity(sessionActivity)
            .build()

        observeQueue()
        observePreferences()
        restoreSavedStateIfEnabled()

        sleepTimer.onFinished = {
            serviceScope.launch(Dispatchers.Main) { player.pause() }
        }
    }

    private fun restoreSavedStateIfEnabled() {
        serviceScope.launch {
            val resume = userPreferences.resumeOnLaunch.first()
            if (!resume) return@launch
            val saved = userPreferences.savedPlaybackState.first()
            if (saved.queueIds.isEmpty()) return@launch
            val songs = musicRepository.getSongsByIds(saved.queueIds)
                .sortedBy { saved.queueIds.indexOf(it.mediaStoreId) }
            if (songs.isEmpty()) return@launch
            val startIndex = songs.indexOfFirst { it.mediaStoreId == saved.songId }.coerceAtLeast(0)
            queueManager.setQueue(songs, startIndex)
            buildAndSetPlayerItems(startIndex)
            player.seekTo(startIndex, saved.positionMs)
            player.prepare()
        }
    }

    private fun observeQueue() {
        queueManager.state.onEach { state ->
            playCountedForCurrentItem = false
            audioEffects.attach(player.audioSessionId)
        }.launchIn(serviceScope)
    }

    private fun observePreferences() {
        userPreferences.crossfadeMs.onEach { crossfadeMs = it }.launchIn(serviceScope)
        userPreferences.repeatMode.onEach { mode ->
            player.repeatMode = when (mode) {
                RepeatMode.OFF -> Player.REPEAT_MODE_OFF
                RepeatMode.ONE -> Player.REPEAT_MODE_ONE
                RepeatMode.ALL -> Player.REPEAT_MODE_ALL
            }
            queueManager.setRepeatMode(mode)
        }.launchIn(serviceScope)
        userPreferences.gaplessPlayback.onEach { enabled -> }.launchIn(serviceScope)
    }

    fun playQueue(songs: List<Song>, startIndex: Int, shuffle: Boolean) {
        queueManager.setQueue(songs, startIndex, shuffle)
        buildAndSetPlayerItems(startIndex)
        player.playWhenReady = true
        player.prepare()
    }

    private fun buildAndSetPlayerItems(startIndex: Int) {
        val state = queueManager.state.value
        val items = state.songs.map { it.toMediaItem() }
        player.setMediaItems(items, startIndex.coerceIn(0, items.lastIndex.coerceAtLeast(0)), C.TIME_UNSET)
    }

    private fun Song.toMediaItem(): MediaItem {
        val artworkUri = Uri.parse("content://media/external/audio/albumart/$albumId")
        val metadata = MediaMetadata.Builder()
            .setTitle(title)
            .setArtist(artistName)
            .setAlbumTitle(albumName)
            .setArtworkUri(artworkUri)
            .setIsPlayable(true)
            .build()
        return MediaItem.Builder()
            .setMediaId(mediaStoreId.toString())
            .setUri(contentUriString)
            .setMediaMetadata(metadata)
            .build()
    }

    private val playerListener = object : Player.Listener {
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            playCountedForCurrentItem = false
            val songId = mediaItem?.mediaId?.toLongOrNull() ?: return
            val index = queueManager.state.value.songs.indexOfFirst { it.mediaStoreId == songId }
            if (index >= 0) queueManager.moveToIndex(index)
            serviceScope.launch { musicRepository.recordPlaybackStart(songId) }
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            if (playbackState == Player.STATE_ENDED && player.repeatMode == Player.REPEAT_MODE_OFF) {
                queueManager.advanceToNext()
            }
        }

        override fun onEvents(p: Player, events: Player.Events) {
            if (events.contains(Player.EVENT_POSITION_DISCONTINUITY) ||
                events.contains(Player.EVENT_PLAYBACK_STATE_CHANGED)
            ) {
                maybeCountPlay()
                persistResumeState()
            }
        }
    }

    private fun maybeCountPlay() {
        if (playCountedForCurrentItem) return
        val duration = player.duration
        val position = player.currentPosition
        if (duration <= 0) return
        val threshold = minOf(duration / 2, 30_000L)
        if (position >= threshold) {
            playCountedForCurrentItem = true
            val songId = player.currentMediaItem?.mediaId?.toLongOrNull() ?: return
            serviceScope.launch { musicRepository.incrementPlayCount(songId) }
        }
    }

    private fun persistResumeState() {
        val songId = player.currentMediaItem?.mediaId?.toLongOrNull() ?: return
        val position = player.currentPosition
        val queueIds = queueManager.state.value.songs.map { it.mediaStoreId }
        serviceScope.launch { userPreferences.savePlaybackState(songId, position, queueIds) }
    }

    private val sessionCallback = object : MediaSession.Callback {
        override fun onCustomCommand(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            customCommand: SessionCommand,
            args: android.os.Bundle
        ): ListenableFuture<SessionResult> {
            when (customCommand.customAction) {
                ACTION_TOGGLE_SHUFFLE -> {
                    val newState = !queueManager.state.value.shuffleEnabled
                    queueManager.setShuffle(newState)
                    serviceScope.launch { userPreferences.setShuffleEnabled(newState) }
                }
                ACTION_CYCLE_REPEAT -> {
                    val next = when (queueManager.state.value.repeatMode) {
                        RepeatMode.OFF -> RepeatMode.ALL
                        RepeatMode.ALL -> RepeatMode.ONE
                        RepeatMode.ONE -> RepeatMode.OFF
                    }
                    serviceScope.launch { userPreferences.setRepeatMode(next) }
                }
            }
            return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    override fun onTaskRemoved(rootIntent: Intent?) {
        if (!player.isPlaying) {
            stopSelf()
        }
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        audioEffects.release()
        sleepTimer.cancel()
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        serviceScope.cancel()
        super.onDestroy()
    }

    companion object {
        const val ACTION_TOGGLE_SHUFFLE = "com.bgx.music.TOGGLE_SHUFFLE"
        const val ACTION_CYCLE_REPEAT = "com.bgx.music.CYCLE_REPEAT"
    }
}
