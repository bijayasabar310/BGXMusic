package com.bgx.music.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bgx.music.data.local.entity.Playlist
import com.bgx.music.data.local.entity.Song
import com.bgx.music.data.repository.MusicRepository
import com.bgx.music.data.repository.PlaylistRepository
import com.bgx.music.playback.PlaybackController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaylistsViewModel @Inject constructor(
    private val playlistRepository: PlaylistRepository
) : ViewModel() {

    val playlists: StateFlow<List<Playlist>> = playlistRepository.observePlaylists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun createPlaylist(name: String) = viewModelScope.launch {
        if (name.isNotBlank()) playlistRepository.createPlaylist(name.trim())
    }

    fun deletePlaylist(id: Long) = viewModelScope.launch { playlistRepository.deletePlaylist(id) }
}

data class PlaylistDetailUiState(
    val playlist: Playlist? = null,
    val songs: List<Song> = emptyList()
)

@HiltViewModel
class PlaylistDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val playlistRepository: PlaylistRepository,
    musicRepository: MusicRepository,
    val playbackController: PlaybackController
) : ViewModel() {

    private val playlistId: Long = checkNotNull(savedStateHandle["playlistId"])

    val uiState: StateFlow<PlaylistDetailUiState> = combine(
        playlistRepository.observePlaylist(playlistId),
        playlistRepository.observeSongsInPlaylist(playlistId)
    ) { playlist, songs -> PlaylistDetailUiState(playlist, songs) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PlaylistDetailUiState())

    fun playAll() = playbackController.playSongs(uiState.value.songs, 0)
    fun shufflePlaylist() = playbackController.shuffleAll(uiState.value.songs)
    fun playSong(song: Song) {
        val songs = uiState.value.songs
        playbackController.playSongs(songs, songs.indexOf(song).coerceAtLeast(0))
    }

    fun removeSong(songId: Long) = viewModelScope.launch {
        playlistRepository.removeSong(playlistId, songId)
    }

    fun reorder(fromIndex: Int, toIndex: Int) = viewModelScope.launch {
        val current = uiState.value.songs.toMutableList()
        val moved = current.removeAt(fromIndex)
        current.add(toIndex, moved)
        playlistRepository.reorder(playlistId, current.map { it.mediaStoreId })
    }

    fun rename(newName: String) = viewModelScope.launch {
        uiState.value.playlist?.let { playlistRepository.renamePlaylist(it, newName) }
    }
}
