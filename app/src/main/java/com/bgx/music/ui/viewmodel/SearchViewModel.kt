package com.bgx.music.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bgx.music.data.local.entity.Album
import com.bgx.music.data.local.entity.Artist
import com.bgx.music.data.local.entity.Playlist
import com.bgx.music.data.local.entity.Song
import com.bgx.music.data.repository.MusicRepository
import com.bgx.music.data.repository.PlaylistRepository
import com.bgx.music.playback.PlaybackController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val query: String = "",
    val songResults: List<Song> = emptyList(),
    val albumResults: List<Album> = emptyList(),
    val artistResults: List<Artist> = emptyList(),
    val playlistResults: List<Playlist> = emptyList(),
    val isSearching: Boolean = false
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val musicRepository: MusicRepository,
    private val playlistRepository: PlaylistRepository,
    val playbackController: PlaybackController
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null
    private var allAlbums: List<Album> = emptyList()
    private var allArtists: List<Artist> = emptyList()
    private var allPlaylists: List<Playlist> = emptyList()

    init {
        viewModelScope.launch { musicRepository.observeAlbums().collect { allAlbums = it } }
        viewModelScope.launch { musicRepository.observeArtists().collect { allArtists = it } }
        viewModelScope.launch { playlistRepository.observePlaylists().collect { allPlaylists = it } }
    }

    fun onQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(query = query, isSearching = query.isNotBlank())
        searchJob?.cancel()
        if (query.isBlank()) {
            _uiState.value = _uiState.value.copy(
                songResults = emptyList(), albumResults = emptyList(),
                artistResults = emptyList(), playlistResults = emptyList(), isSearching = false
            )
            return
        }
        searchJob = viewModelScope.launch {
            delay(180)
            val songs = musicRepository.search(query)
            val lower = query.lowercase()
            _uiState.value = _uiState.value.copy(
                songResults = songs,
                albumResults = allAlbums.filter { it.name.lowercase().contains(lower) },
                artistResults = allArtists.filter { it.name.lowercase().contains(lower) },
                playlistResults = allPlaylists.filter { it.name.lowercase().contains(lower) },
                isSearching = false
            )
        }
    }

    fun playSong(song: Song) {
        val songs = uiState.value.songResults
        playbackController.playSongs(songs, songs.indexOf(song).coerceAtLeast(0))
    }
}
