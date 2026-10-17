package com.bgx.music.ui.screens.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bgx.music.ui.components.SongRow
import com.bgx.music.ui.viewmodel.SearchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onBack: () -> Unit,
    onAlbumClick: (Long) -> Unit,
    onArtistClick: (Long) -> Unit,
    onPlaylistClick: (Long) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusRequester = remember { FocusRequester() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    OutlinedTextField(
                        value = uiState.query,
                        onValueChange = viewModel::onQueryChanged,
                        placeholder = { Text("Search songs, albums, artists...") },
                        singleLine = true,
                        trailingIcon = {
                            if (uiState.query.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onQueryChanged("") }) {
                                    Icon(Icons.Filled.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().focusRequester(focusRequester)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.query.isBlank()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Icon(Icons.Filled.Search, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline)
            }
            return@Scaffold
        }

        LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (uiState.artistResults.isNotEmpty()) {
                item { SearchSectionLabel("Artists") }
                items(uiState.artistResults, key = { "artist_" + it.artistId }) { artist ->
                    ListItem(
                        headlineContent = { Text(artist.name) },
                        supportingContent = { Text(artist.songCount.toString() + " songs") },
                        leadingContent = { Icon(Icons.Filled.Person, contentDescription = null) },
                        modifier = Modifier.clickableItem { onArtistClick(artist.artistId) }
                    )
                }
            }
            if (uiState.albumResults.isNotEmpty()) {
                item { SearchSectionLabel("Albums") }
                items(uiState.albumResults, key = { "album_" + it.albumId }) { album ->
                    ListItem(
                        headlineContent = { Text(album.name) },
                        supportingContent = { Text(album.artistName) },
                        leadingContent = { Icon(Icons.Filled.Album, contentDescription = null) },
                        modifier = Modifier.clickableItem { onAlbumClick(album.albumId) }
                    )
                }
            }
            if (uiState.playlistResults.isNotEmpty()) {
                item { SearchSectionLabel("Playlists") }
                items(uiState.playlistResults, key = { "playlist_" + it.playlistId }) { playlist ->
                    ListItem(
                        headlineContent = { Text(playlist.name) },
                        leadingContent = { Icon(Icons.Filled.QueueMusic, contentDescription = null) },
                        modifier = Modifier.clickableItem { onPlaylistClick(playlist.playlistId) }
                    )
                }
            }
            if (uiState.songResults.isNotEmpty()) {
                item { SearchSectionLabel("Songs") }
                items(uiState.songResults, key = { "song_" + it.mediaStoreId }) { song ->
                    SongRow(song = song, onClick = { viewModel.playSong(song) })
                }
            }
            if (!uiState.isSearching && uiState.songResults.isEmpty() && uiState.albumResults.isEmpty() &&
                uiState.artistResults.isEmpty() && uiState.playlistResults.isEmpty()
            ) {
                item {
                    Text(
                        "No results for " + uiState.query,
                        modifier = Modifier.padding(24.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchSectionLabel(text: String) {
    Text(
        text, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
    )
}

private fun Modifier.clickableItem(onClick: () -> Unit): Modifier =
    this.clickable(onClick = onClick)
