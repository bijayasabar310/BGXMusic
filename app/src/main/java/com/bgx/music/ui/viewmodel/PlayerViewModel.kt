package com.bgx.music.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bgx.music.data.local.entity.Song
import com.bgx.music.data.preferences.RepeatMode
import com.bgx.music.data.preferences.UserPreferences
import com.bgx.music.data.repository.MusicRepository
import com.bgx.music.playback.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NowPlayingUiState(
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val isFavorite: Boolean = false,
    val shuffleEnabled: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val playbackSpeed: Float = 1f,
    val queue: List<Song> = emptyList(),
    val queueIndex: Int = -1,
    val sleepTimer: SleepTimerState = SleepTimerState()
)

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val connection: PlaybackConnection,
    private val queueManager: QueueManager,
    private val musicRepository: MusicRepository,
    private val preferences: UserPreferences,
    private val sleepTimerManager: SleepTimerManager,
    val audioEffects: AudioEffectsController,
    val playbackController: PlaybackController
) : ViewModel() {

    init {
        connection.connect()
        viewModelScope.launch {
            while (isActive) {
                connection.refreshPosition()
                delay(500)
            }
        }
    }

    private val currentSongFlow: Flow<Song?> = combine(
        connection.uiState.map { it.currentSongId }.distinctUntilChanged(),
        queueManager.state
    ) { songId, queueState ->
        queueState.songs.firstOrNull { it.mediaStoreId == songId } ?: queueState.currentSong
    }

    val uiState: StateFlow<NowPlayingUiState> = combine(
        currentSongFlow,
        connection.uiState,
        queueManager.state,
        sleepTimerManager.state
    ) { song, playback, queue, timer ->
        NowPlayingUiState(
            currentSong = song,
            isPlaying = playback.isPlaying,
            isBuffering = playback.isBuffering,
            positionMs = playback.positionMs,
            durationMs = playback.durationMs,
            shuffleEnabled = queue.shuffleEnabled,
            repeatMode = queue.repeatMode,
            playbackSpeed = playback.playbackSpeed,
            queue = queue.songs,
            queueIndex = queue.currentIndex,
            sleepTimer = timer
        )
    }.combine(favoriteFlow()) { state, isFavorite -> state.copy(isFavorite = isFavorite) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), NowPlayingUiState())

    private fun favoriteFlow(): Flow<Boolean> = currentSongFlow.flatMapLatest { song ->
        song?.let { musicRepository.observeIsFavorite(it.mediaStoreId) } ?: flowOf(false)
    }

    fun togglePlayPause() = connection.togglePlayPause()
    fun skipNext() = connection.skipToNext()
    fun skipPrevious() = connection.skipToPrevious()
    fun seekTo(positionMs: Long) = connection.seekTo(positionMs)
    fun setPlaybackSpeed(speed: Float) = connection.setPlaybackSpeed(speed)

    fun toggleShuffle() = playbackController.toggleShuffle()

    fun cycleRepeatMode() = viewModelScope.launch {
        val next = when (uiState.value.repeatMode) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
        preferences.setRepeatMode(next)
        queueManager.setRepeatMode(next)
    }

    fun toggleFavorite() = viewModelScope.launch {
        val song = uiState.value.currentSong ?: return@launch
        musicRepository.toggleFavorite(song.mediaStoreId, !uiState.value.isFavorite)
    }

    fun startSleepTimer(minutes: Int) =
        sleepTimerManager.start(minutes * 60_000L, SleepTimerEndAction.PAUSE)

    fun cancelSleepTimer() = sleepTimerManager.cancel()

    fun playFromQueue(index: Int) = playbackController.playFromQueue(index)
    fun removeFromQueue(index: Int) = playbackController.removeFromQueue(index)
    fun reorderQueue(from: Int, to: Int) = playbackController.reorderQueue(from, to)
}
