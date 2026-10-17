package com.bgx.music.playback

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

data class PlaybackUiState(
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val currentSongId: Long? = null,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val playbackSpeed: Float = 1f
)

@Singleton
class PlaybackConnection @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var controllerFuture: ListenableFuture<MediaController>? = null
    var controller: MediaController? = null
        private set

    private val _uiState = MutableStateFlow(PlaybackUiState())
    val uiState: StateFlow<PlaybackUiState> = _uiState.asStateFlow()

    private val listener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _uiState.value = _uiState.value.copy(isPlaying = isPlaying)
        }
        override fun onPlaybackStateChanged(playbackState: Int) {
            _uiState.value = _uiState.value.copy(isBuffering = playbackState == Player.STATE_BUFFERING)
            refreshPosition()
        }
        override fun onMediaItemTransition(mediaItem: androidx.media3.common.MediaItem?, reason: Int) {
            _uiState.value = _uiState.value.copy(
                currentSongId = mediaItem?.mediaId?.toLongOrNull(),
                durationMs = controller?.duration?.coerceAtLeast(0) ?: 0L
            )
        }
        override fun onPlaybackParametersChanged(playbackParameters: androidx.media3.common.PlaybackParameters) {
            _uiState.value = _uiState.value.copy(playbackSpeed = playbackParameters.speed)
        }
    }

    fun connect(onConnected: () -> Unit = {}) {
        if (controllerFuture != null) { onConnected(); return }
        val sessionToken = SessionToken(
            context,
            ComponentName(context, com.bgx.music.playback.PlaybackService::class.java)
        )
        val future = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture = future
        future.addListener({
            controller = future.get().also { it.addListener(listener) }
            _uiState.value = _uiState.value.copy(
                isPlaying = controller?.isPlaying == true,
                currentSongId = controller?.currentMediaItem?.mediaId?.toLongOrNull(),
                durationMs = controller?.duration?.coerceAtLeast(0) ?: 0L
            )
            onConnected()
        }, MoreExecutors.directExecutor())
    }

    fun refreshPosition() {
        controller?.let {
            _uiState.value = _uiState.value.copy(
                positionMs = it.currentPosition.coerceAtLeast(0),
                durationMs = it.duration.coerceAtLeast(0)
            )
        }
    }

    fun play() = controller?.play()
    fun pause() = controller?.pause()
    fun togglePlayPause() {
        controller?.let { if (it.isPlaying) it.pause() else it.play() }
    }
    fun seekTo(positionMs: Long) = controller?.seekTo(positionMs)
    fun skipToNext() = controller?.seekToNextMediaItem()
    fun skipToPrevious() = controller?.seekToPreviousMediaItem()
    fun setPlaybackSpeed(speed: Float) = controller?.setPlaybackSpeed(speed)

    fun release() {
        controller?.removeListener(listener)
        controllerFuture?.let { MediaController.releaseFuture(it) }
        controllerFuture = null
        controller = null
    }
}
