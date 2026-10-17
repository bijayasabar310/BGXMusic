package com.bgx.music.ui.screens.albums

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bgx.music.ui.components.AlbumGridCard
import com.bgx.music.ui.components.EmptyState
import com.bgx.music.ui.viewmodel.AlbumsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumsScreen(
    onAlbumClick: (Long) -> Unit,
    viewModel: AlbumsViewModel = hiltViewModel()
) {
    val albums by viewModel.albums.collectAsState()

    Scaffold(topBar = { TopAppBar(title = { Text("Albums") }) }) { padding ->
        if (albums.isEmpty()) {
            EmptyState(
                title = "No albums yet",
                subtitle = "Albums from your local music will show up here.",
                icon = Icons.Filled.Album,
                modifier = Modifier.fillMaxSize()
            )
            return@Scaffold
        }
        LazyVerticalGrid(
            columns = GridCells.Adaptive(150.dp),
            contentPadding = PaddingValues(16.dp),
            modifier = Modifier.padding(padding).fillMaxSize()
        ) {
            items(albums, key = { it.albumId }) { album ->
                AlbumGridCard(
                    title = album.name,
                    subtitle = album.artistName + " - " + album.songCount + " songs",
                    albumId = album.albumId,
                    onClick = { onAlbumClick(album.albumId) },
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}
