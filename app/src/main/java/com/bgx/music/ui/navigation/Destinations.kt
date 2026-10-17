package com.bgx.music.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavDestination(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    data object Home : BottomNavDestination("home", "Home", Icons.Filled.Home, Icons.Outlined.Home)
    data object Songs : BottomNavDestination("songs", "Songs", Icons.Filled.MusicNote, Icons.Outlined.MusicNote)
    data object Albums : BottomNavDestination("albums", "Albums", Icons.Filled.Album, Icons.Outlined.Album)
    data object Artists : BottomNavDestination("artists", "Artists", Icons.Filled.Person, Icons.Outlined.Person)
    data object Playlists : BottomNavDestination("playlists", "Playlists", Icons.Filled.QueueMusic, Icons.Outlined.QueueMusic)

    companion object {
        val items = listOf(Home, Songs, Albums, Artists, Playlists)
    }
}

object Routes {
    const val SEARCH = "search"
    const val FOLDERS = "folders"
    const val FOLDER_DETAIL = "folder_detail/{path}"
    const val ALBUM_DETAIL = "album_detail/{albumId}"
    const val ARTIST_DETAIL = "artist_detail/{artistId}"
    const val PLAYLIST_DETAIL = "playlist_detail/{playlistId}"
    const val NOW_PLAYING = "now_playing"
    const val QUEUE = "queue"
    const val EQUALIZER = "equalizer"
    const val SETTINGS = "settings"

    fun albumDetail(albumId: Long) = "album_detail/$albumId"
    fun artistDetail(artistId: Long) = "artist_detail/$artistId"
    fun playlistDetail(playlistId: Long) = "playlist_detail/$playlistId"
    fun folderDetail(path: String) = "folder_detail/" + java.net.URLEncoder.encode(path, "UTF-8")
}
