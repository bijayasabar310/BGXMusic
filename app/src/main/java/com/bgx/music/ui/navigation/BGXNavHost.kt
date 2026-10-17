package com.bgx.music.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.bgx.music.ui.screens.albums.AlbumDetailScreen
import com.bgx.music.ui.screens.albums.AlbumsScreen
import com.bgx.music.ui.screens.artists.ArtistDetailScreen
import com.bgx.music.ui.screens.artists.ArtistsScreen
import com.bgx.music.ui.screens.equalizer.EqualizerScreen
import com.bgx.music.ui.screens.folders.FolderDetailScreen
import com.bgx.music.ui.screens.folders.FoldersScreen
import com.bgx.music.ui.screens.home.HomeScreen
import com.bgx.music.ui.screens.playlists.PlaylistDetailScreen
import com.bgx.music.ui.screens.playlists.PlaylistsScreen
import com.bgx.music.ui.screens.queue.QueueScreen
import com.bgx.music.ui.screens.search.SearchScreen
import com.bgx.music.ui.screens.settings.SettingsScreen
import com.bgx.music.ui.screens.songs.SongsScreen

@Composable
fun BGXNavHost(
    navController: NavHostController,
    onOpenNowPlaying: () -> Unit
) {
    NavHost(navController = navController, startDestination = BottomNavDestination.Home.route) {

        composable(BottomNavDestination.Home.route) {
            HomeScreen(
                onPlaylistClick = { navController.navigate(Routes.playlistDetail(it)) },
                onNavigateToSongs = { navController.navigate(BottomNavDestination.Songs.route) }
            )
        }
        composable(BottomNavDestination.Songs.route) { SongsScreen() }
        composable(BottomNavDestination.Albums.route) {
            AlbumsScreen(onAlbumClick = { navController.navigate(Routes.albumDetail(it)) })
        }
        composable(BottomNavDestination.Artists.route) {
            ArtistsScreen(onArtistClick = { navController.navigate(Routes.artistDetail(it)) })
        }
        composable(BottomNavDestination.Playlists.route) {
            PlaylistsScreen(onPlaylistClick = { navController.navigate(Routes.playlistDetail(it)) })
        }

        composable(Routes.SEARCH) {
            SearchScreen(
                onBack = { navController.popBackStack() },
                onAlbumClick = { navController.navigate(Routes.albumDetail(it)) },
                onArtistClick = { navController.navigate(Routes.artistDetail(it)) },
                onPlaylistClick = { navController.navigate(Routes.playlistDetail(it)) }
            )
        }

        composable(Routes.FOLDERS) {
            FoldersScreen(onFolderClick = { navController.navigate(Routes.folderDetail(it)) })
        }
        composable(
            route = Routes.FOLDER_DETAIL,
            arguments = listOf(navArgument("path") { type = NavType.StringType })
        ) {
            FolderDetailScreen(onBack = { navController.popBackStack() })
        }

        composable(
            route = Routes.ALBUM_DETAIL,
            arguments = listOf(navArgument("albumId") { type = NavType.LongType })
        ) {
            AlbumDetailScreen(onBack = { navController.popBackStack() })
        }

        composable(
            route = Routes.ARTIST_DETAIL,
            arguments = listOf(navArgument("artistId") { type = NavType.LongType })
        ) {
            ArtistDetailScreen(
                onBack = { navController.popBackStack() },
                onAlbumClick = { navController.navigate(Routes.albumDetail(it)) }
            )
        }

        composable(
            route = Routes.PLAYLIST_DETAIL,
            arguments = listOf(navArgument("playlistId") { type = NavType.LongType })
        ) {
            PlaylistDetailScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.QUEUE) {
            QueueScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.EQUALIZER) {
            EqualizerScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onOpenEqualizer = { navController.navigate(Routes.EQUALIZER) }
            )
        }
    }
}
