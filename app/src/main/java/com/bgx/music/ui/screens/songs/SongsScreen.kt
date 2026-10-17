package com.bgx.music.ui.screens.songs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bgx.music.data.preferences.SongSortOrder
import com.bgx.music.ui.components.AddToPlaylistSheet
import com.bgx.music.ui.components.EmptyState
import com.bgx.music.ui.components.SongRow
import com.bgx.music.ui.viewmodel.PlaylistsViewModel
import com.bgx.music.ui.viewmodel.SongsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongsScreen(
    viewModel: SongsViewModel = hiltViewModel(),
    playlistsViewModel: PlaylistsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var sortMenuExpanded by remember { mutableStateOf(false) }
    var showAddToPlaylist by remember { mutableStateOf(false) }
    val playlists by playlistsViewModel.playlists.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.isSelectionMode) "${uiState.selectedIds.size} selected" else "Songs") },
                actions = {
                    if (uiState.isSelectionMode) {
                        IconButton(onClick = { viewModel.favoriteSelected() }) {
                            Icon(Icons.Filled.FavoriteBorder, contentDescription = "Favorite")
                        }
                        IconButton(onClick = { showAddToPlaylist = true }) {
                            Icon(Icons.Filled.PlaylistAdd, contentDescription = "Add to playlist")
                        }
                        IconButton(onClick = { viewModel.clearSelection() }) {
                            Icon(Icons.Filled.Close, contentDescription = "Cancel selection")
                        }
                    } else {
                        IconButton(onClick = { sortMenuExpanded = true }) {
                            Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = "Sort")
                        }
                        DropdownMenu(expanded = sortMenuExpanded, onDismissRequest = { sortMenuExpanded = false }) {
                            SongSortOrder.entries.forEach { order ->
                                DropdownMenuItem(
                                    text = { Text(order.label()) },
                                    onClick = { viewModel.setSortOrder(order); sortMenuExpanded = false }
                                )
                            }
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (!uiState.isSelectionMode && uiState.songs.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = { viewModel.shuffleAll() },
                    icon = { Icon(Icons.Filled.Shuffle, contentDescription = null) },
                    text = { Text("Shuffle") }
                )
            }
        }
    ) { padding ->
        if (uiState.songs.isEmpty()) {
            EmptyState(
                title = "No songs yet",
                subtitle = "Local audio files will appear here once found.",
                icon = Icons.Filled.MusicNote,
                modifier = Modifier.fillMaxSize().padding(padding)
            )
            return@Scaffold
        }

        LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
            items(uiState.songs, key = { it.mediaStoreId }) { song ->
                SongRow(
                    song = song,
                    isSelected = song.mediaStoreId in uiState.selectedIds,
                    isSelectionMode = uiState.isSelectionMode,
                    onClick = {
                        if (uiState.isSelectionMode) viewModel.toggleSelection(song.mediaStoreId)
                        else viewModel.playSong(song)
                    },
                    onLongClick = { viewModel.toggleSelection(song.mediaStoreId) }
                )
            }
        }
    }

    if (showAddToPlaylist) {
        AddToPlaylistSheet(
            playlists = playlists,
            onDismiss = { showAddToPlaylist = false },
            onPlaylistSelected = { viewModel.addSelectedToPlaylist(it); showAddToPlaylist = false },
            onCreateNew = { name -> playlistsViewModel.createPlaylist(name); showAddToPlaylist = false }
        )
    }
}

private fun SongSortOrder.label(): String = when (this) {
    SongSortOrder.TITLE -> "Title"
    SongSortOrder.ARTIST -> "Artist"
    SongSortOrder.ALBUM -> "Album"
    SongSortOrder.DATE_ADDED -> "Date added"
    SongSortOrder.DURATION -> "Duration"
}
