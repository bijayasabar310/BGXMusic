package com.bgx.music.ui.viewmodel

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

data class HomeUiState(
    val isLoading: Boolean = true,
    val hasAnyMusic: Boolean = false,
    val continueListening: Song? = null,
    val recentlyPlayed: List<Song> = emptyList(),
    val recentlyAdded: List<Song> = emptyList(),
    val mostPlayed: List<Song> = emptyList(),
    val favorites: List<Song> = emptyList(),
    val playlists: List<Playlist> = emptyList()
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val musicRepository: MusicRepository,
    private val playlistRepository: PlaylistRepository,
    val playbackController: PlaybackController
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        musicRepository.observeRecentlyPlayed(15),
        musicRepository.observeMostPlayed(15),
        musicRepository.observeFavorites(),
        playlistRepository.observePlaylists(),
        musicRepository.observeLibrarySize()
    ) { recentlyPlayed, mostPlayed, favorites, playlists, librarySize ->
        HomeUiState(
            isLoading = false,
            hasAnyMusic = librarySize > 0,
            continueListening = recentlyPlayed.firstOrNull(),
            recentlyPlayed = recentlyPlayed,
            recentlyAdded = emptyList(),
            mostPlayed = mostPlayed,
            favorites = favorites.take(10),
            playlists = playlists
        )
    }.combine(recentlyAddedFlow()) { state, recentlyAdded ->
        state.copy(recentlyAdded = recentlyAdded)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())

    private fun recentlyAddedFlow(): Flow<List<Song>> =
        musicRepository.observeSongs().map { it.sortedByDescending { s -> s.dateAdded }.take(15) }

    fun playSong(song: Song, queueContext: List<Song>) {
        val index = queueContext.indexOf(song).coerceAtLeast(0)
        playbackController.playSongs(queueContext, index)
    }

    fun refreshLibrary() {
        viewModelScope.launch { musicRepository.rescanLibrary() }
    }
}
