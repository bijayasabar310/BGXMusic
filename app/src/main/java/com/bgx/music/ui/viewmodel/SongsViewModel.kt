package com.bgx.music.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bgx.music.data.local.entity.Song
import com.bgx.music.data.preferences.LibraryViewStyle
import com.bgx.music.data.preferences.SongSortOrder
import com.bgx.music.data.preferences.UserPreferences
import com.bgx.music.data.repository.MusicRepository
import com.bgx.music.data.repository.PlaylistRepository
import com.bgx.music.playback.PlaybackController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SongsUiState(
    val songs: List<Song> = emptyList(),
    val sortOrder: SongSortOrder = SongSortOrder.TITLE,
    val viewStyle: LibraryViewStyle = LibraryViewStyle.GRID,
    val selectedIds: Set<Long> = emptySet(),
    val isSelectionMode: Boolean = false
)

@HiltViewModel
class SongsViewModel @Inject constructor(
    private val musicRepository: MusicRepository,
    private val playlistRepository: PlaylistRepository,
    private val preferences: UserPreferences,
    val playbackController: PlaybackController
) : ViewModel() {

    private val selectedIds = MutableStateFlow<Set<Long>>(emptySet())

    val uiState: StateFlow<SongsUiState> = combine(
        musicRepository.observeSongs(),
        preferences.songSortOrder,
        preferences.libraryViewStyle,
        selectedIds
    ) { songs, sort, viewStyle, selected ->
        SongsUiState(songs, sort, viewStyle, selected, selected.isNotEmpty())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SongsUiState())

    fun setSortOrder(order: SongSortOrder) = viewModelScope.launch { preferences.setSongSortOrder(order) }
    fun setViewStyle(style: LibraryViewStyle) = viewModelScope.launch { preferences.setLibraryViewStyle(style) }

    fun playSong(song: Song) {
        val list = uiState.value.songs
        playbackController.playSongs(list, list.indexOf(song).coerceAtLeast(0))
    }

    fun shuffleAll() = playbackController.shuffleAll(uiState.value.songs)

    fun toggleSelection(songId: Long) {
        selectedIds.value = selectedIds.value.let {
            if (songId in it) it - songId else it + songId
        }
    }

    fun clearSelection() { selectedIds.value = emptySet() }

    fun addSelectedToPlaylist(playlistId: Long) = viewModelScope.launch {
        playlistRepository.addSongs(playlistId, selectedIds.value.toList())
        clearSelection()
    }

    fun favoriteSelected() = viewModelScope.launch {
        selectedIds.value.forEach { musicRepository.toggleFavorite(it, true) }
        clearSelection()
    }

    fun addSelectedToQueue() {
        val songs = uiState.value.songs.filter { it.mediaStoreId in selectedIds.value }
        songs.forEach { playbackController.addToQueueEnd(it) }
        clearSelection()
    }
}
