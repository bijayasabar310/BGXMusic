package com.bgx.music.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.bgx.music.core.permissions.AudioPermissionGate
import com.bgx.music.ui.components.MiniPlayer
import com.bgx.music.ui.navigation.BottomNavDestination
import com.bgx.music.ui.navigation.Routes
import com.bgx.music.ui.screens.nowplaying.NowPlayingScreen
import com.bgx.music.ui.navigation.BGXNavHost
import com.bgx.music.ui.viewmodel.PlayerViewModel

@Composable
fun BGXApp() {
    AudioPermissionGate {
        val navController = rememberNavController()
        val backStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = backStackEntry?.destination?.route

        var isNowPlayingExpanded by remember { mutableStateOf(false) }
        val playerViewModel: PlayerViewModel = hiltViewModel()
        val playerState by playerViewModel.uiState.collectAsState()

        val showBottomBar = BottomNavDestination.items.any { it.route == currentRoute }

        Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    NavigationBar {
                        BottomNavDestination.items.forEach { dest ->
                            val selected = currentRoute == dest.route
                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    navController.navigate(dest.route) {
                                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = {
                                    Icon(
                                        if (selected) dest.selectedIcon else dest.unselectedIcon,
                                        contentDescription = dest.label
                                    )
                                },
                                label = { Text(dest.label) }
                            )
                        }
                    }
                }
            }
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(bottom = if (showBottomBar) padding.calculateBottomPadding() else 0.dp)) {
                BGXNavHost(navController = navController, onOpenNowPlaying = { isNowPlayingExpanded = true })

                if (showBottomBar) {
                    MiniPlayer(
                        song = playerState.currentSong,
                        isPlaying = playerState.isPlaying,
                        progressFraction = if (playerState.durationMs > 0) playerState.positionMs.toFloat() / playerState.durationMs else 0f,
                        onPlayPause = { playerViewModel.togglePlayPause() },
                        onNext = { playerViewModel.skipNext() },
                        onDismiss = { },
                        onClick = { isNowPlayingExpanded = true },
                        modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp)
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = isNowPlayingExpanded,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it })
        ) {
            Surface(modifier = Modifier.fillMaxSize()) {
                NowPlayingScreen(
                    onCollapse = { isNowPlayingExpanded = false },
                    onOpenQueue = { navController.navigate(Routes.QUEUE); isNowPlayingExpanded = false },
                    onOpenEqualizer = { navController.navigate(Routes.EQUALIZER); isNowPlayingExpanded = false }
                )
            }
        }
    }
}
