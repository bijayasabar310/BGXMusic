package com.bgx.music.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bgx.music.data.local.entity.Album
import com.bgx.music.data.local.entity.Song
import com.bgx.music.data.repository.MusicRepository
import com.bgx.music.playback.PlaybackController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class AlbumsViewModel @Inject constructor(
    musicRepository: MusicRepository
) : ViewModel() {
    val albums: StateFlow<List<Album>> = musicRepository.observeAlbums()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}

data class AlbumDetailUiState(
    val album: Album? = null,
    val songs: List<Song> = emptyList()
)

@HiltViewModel
class AlbumDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    musicRepository: MusicRepository,
    val playbackController: PlaybackController
) : ViewModel() {

    private val albumId: Long = checkNotNull(savedStateHandle["albumId"])

    val uiState: StateFlow<AlbumDetailUiState> = combine(
        musicRepository.observeAlbum(albumId),
        musicRepository.observeSongsForAlbum(albumId)
    ) { album, songs -> AlbumDetailUiState(album, songs) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AlbumDetailUiState())

    fun playAlbum() = playbackController.playSongs(uiState.value.songs, 0)
    fun shuffleAlbum() = playbackController.shuffleAll(uiState.value.songs)
    fun playSong(song: Song) {
        val songs = uiState.value.songs
        playbackController.playSongs(songs, songs.indexOf(song).coerceAtLeast(0))
    }
}
