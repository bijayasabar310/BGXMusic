package com.bgx.music.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bgx.music.data.local.entity.Song
import com.bgx.music.ui.components.AlbumGridCard
import com.bgx.music.ui.components.EmptyState
import com.bgx.music.ui.components.SectionHeader
import com.bgx.music.ui.components.SongRow
import com.bgx.music.ui.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    onPlaylistClick: (Long) -> Unit,
    onNavigateToSongs: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    if (!uiState.isLoading && !uiState.hasAnyMusic) {
        EmptyState(
            title = "No music found",
            subtitle = "We could not find any audio files on this device yet. Add some music or pull to rescan.",
            icon = Icons.Filled.LibraryMusic,
            modifier = Modifier.fillMaxSize(),
            action = {
                Button(onClick = { viewModel.refreshLibrary() }) { Text("Scan for music") }
            }
        )
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 12.dp, bottom = 100.dp)
    ) {
        item {
            Text(
                "Good to see you",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )
        }

        uiState.continueListening?.let { song ->
            item { ContinueListeningCard(song) { viewModel.playSong(song, uiState.recentlyPlayed) } }
        }

        if (uiState.recentlyPlayed.isNotEmpty()) {
            item { SectionHeader("Recently Played") }
            item { SongRail(uiState.recentlyPlayed) { viewModel.playSong(it, uiState.recentlyPlayed) } }
        }

        if (uiState.recentlyAdded.isNotEmpty()) {
            item { SectionHeader("Recently Added") }
            item { AlbumRail(uiState.recentlyAdded) { viewModel.playSong(it, uiState.recentlyAdded) } }
        }

        if (uiState.mostPlayed.isNotEmpty()) {
            item { SectionHeader("Most Played") }
            item { SongRail(uiState.mostPlayed) { viewModel.playSong(it, uiState.mostPlayed) } }
        }

        if (uiState.favorites.isNotEmpty()) {
            item { SectionHeader("Favorites") }
            item { AlbumRail(uiState.favorites) { viewModel.playSong(it, uiState.favorites) } }
        }
    }
}

@Composable
private fun ContinueListeningCard(song: Song, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            com.bgx.music.ui.components.AlbumArtwork(
                albumId = song.albumId,
                modifier = Modifier.size(64.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text("Continue Listening", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Text(song.title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(song.artistName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            FilledIconButton(onClick = onClick) {
                Icon(Icons.Filled.PlayArrow, contentDescription = "Play")
            }
        }
    }
}

@Composable
private fun SongRail(songs: List<Song>, onSongClick: (Song) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(songs, key = { it.mediaStoreId }) { song ->
            Box(modifier = Modifier.width(280.dp)) {
                SongRow(song = song, onClick = { onSongClick(song) })
            }
        }
    }
}

@Composable
private fun AlbumRail(songs: List<Song>, onSongClick: (Song) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(songs, key = { it.mediaStoreId }) { song ->
            AlbumGridCard(
                title = song.title,
                subtitle = song.artistName,
                albumId = song.albumId,
                onClick = { onSongClick(song) }
            )
        }
    }
}
