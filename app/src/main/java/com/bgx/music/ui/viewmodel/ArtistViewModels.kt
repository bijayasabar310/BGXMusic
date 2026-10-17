package com.bgx.music.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bgx.music.data.local.entity.Album
import com.bgx.music.data.local.entity.Artist
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
class ArtistsViewModel @Inject constructor(
    musicRepository: MusicRepository
) : ViewModel() {
    val artists: StateFlow<List<Artist>> = musicRepository.observeArtists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}

data class ArtistDetailUiState(
    val artist: Artist? = null,
    val albums: List<Album> = emptyList(),
    val songs: List<Song> = emptyList()
)

@HiltViewModel
class ArtistDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    musicRepository: MusicRepository,
    val playbackController: PlaybackController
) : ViewModel() {

    private val artistId: Long = checkNotNull(savedStateHandle["artistId"])

    val uiState: StateFlow<ArtistDetailUiState> = combine(
        musicRepository.observeArtist(artistId),
        musicRepository.observeAlbumsForArtist(artistId),
        musicRepository.observeSongsForArtist(artistId)
    ) { artist, albums, songs -> ArtistDetailUiState(artist, albums, songs) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ArtistDetailUiState())

    fun playAll() = playbackController.playSongs(uiState.value.songs, 0)
    fun shuffleAll() = playbackController.shuffleAll(uiState.value.songs)
    fun playSong(song: Song) {
        val songs = uiState.value.songs
        playbackController.playSongs(songs, songs.indexOf(song).coerceAtLeast(0))
    }
}
