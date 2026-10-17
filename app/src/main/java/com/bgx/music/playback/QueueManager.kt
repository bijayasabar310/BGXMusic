package com.bgx.music.playback

import com.bgx.music.data.local.entity.Song
import com.bgx.music.data.preferences.RepeatMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

data class QueueState(
    val songs: List<Song> = emptyList(),
    val currentIndex: Int = -1,
    val shuffleEnabled: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val originalOrder: List<Song> = emptyList()
) {
    val currentSong: Song? get() = songs.getOrNull(currentIndex)
    val hasNext: Boolean get() = repeatMode == RepeatMode.ALL || currentIndex < songs.lastIndex
    val hasPrevious: Boolean get() = repeatMode == RepeatMode.ALL || currentIndex > 0
}

@Singleton
class QueueManager @Inject constructor() {

    private val _state = MutableStateFlow(QueueState())
    val state: StateFlow<QueueState> = _state.asStateFlow()

    fun setQueue(songs: List<Song>, startIndex: Int = 0, shuffle: Boolean = false) {
        val ordered = if (shuffle) songs.shuffled() else songs
        _state.value = _state.value.copy(
            songs = ordered,
            originalOrder = songs,
            currentIndex = ordered.indices.firstOrNull { ordered[it] == songs.getOrNull(startIndex) } ?: 0,
            shuffleEnabled = shuffle
        )
    }

    fun addToQueueNext(song: Song) {
        val s = _state.value
        val insertAt = (s.currentIndex + 1).coerceAtMost(s.songs.size)
        _state.value = s.copy(songs = s.songs.toMutableList().apply { add(insertAt, song) })
    }

    fun addToQueueEnd(song: Song) {
        val s = _state.value
        _state.value = s.copy(songs = s.songs + song)
    }

    fun removeAt(index: Int) {
        val s = _state.value
        if (index !in s.songs.indices) return
        val newSongs = s.songs.toMutableList().apply { removeAt(index) }
        val newIndex = when {
            index < s.currentIndex -> s.currentIndex - 1
            index == s.currentIndex -> s.currentIndex.coerceAtMost(newSongs.lastIndex)
            else -> s.currentIndex
        }
        _state.value = s.copy(songs = newSongs, currentIndex = newIndex)
    }

    fun reorder(fromIndex: Int, toIndex: Int) {
        val s = _state.value
        val list = s.songs.toMutableList()
        if (fromIndex !in list.indices || toIndex !in list.indices) return
        val moved = list.removeAt(fromIndex)
        list.add(toIndex, moved)
        val playingSong = s.currentSong
        val newIndex = playingSong?.let { list.indexOf(it) } ?: s.currentIndex
        _state.value = s.copy(songs = list, currentIndex = newIndex)
    }

    fun clear() {
        _state.value = QueueState()
    }

    fun moveToIndex(index: Int) {
        if (index in _state.value.songs.indices) {
            _state.value = _state.value.copy(currentIndex = index)
        }
    }

    fun advanceToNext(): Boolean {
        val s = _state.value
        return when {
            s.repeatMode == RepeatMode.ONE -> true
            s.currentIndex < s.songs.lastIndex -> {
                _state.value = s.copy(currentIndex = s.currentIndex + 1); true
            }
            s.repeatMode == RepeatMode.ALL && s.songs.isNotEmpty() -> {
                _state.value = s.copy(currentIndex = 0); true
            }
            else -> false
        }
    }

    fun goToPrevious(): Boolean {
        val s = _state.value
        return when {
            s.currentIndex > 0 -> {
                _state.value = s.copy(currentIndex = s.currentIndex - 1); true
            }
            s.repeatMode == RepeatMode.ALL && s.songs.isNotEmpty() -> {
                _state.value = s.copy(currentIndex = s.songs.lastIndex); true
            }
            else -> false
        }
    }

    fun setShuffle(enabled: Boolean) {
        val s = _state.value
        val current = s.currentSong
        val newSongs = if (enabled) s.originalOrder.shuffled() else s.originalOrder
        val newIndex = current?.let { newSongs.indexOf(it) } ?: 0
        _state.value = s.copy(songs = newSongs, currentIndex = newIndex.coerceAtLeast(0), shuffleEnabled = enabled)
    }

    fun setRepeatMode(mode: RepeatMode) {
        _state.value = _state.value.copy(repeatMode = mode)
    }
}
