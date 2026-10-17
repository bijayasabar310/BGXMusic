package com.bgx.music.ui.screens.nowplaying

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bgx.music.core.util.formatDuration
import com.bgx.music.data.preferences.RepeatMode
import com.bgx.music.ui.components.AlbumArtwork
import com.bgx.music.ui.viewmodel.PlayerViewModel

@Composable
fun NowPlayingScreen(
    onCollapse: () -> Unit,
    onOpenQueue: () -> Unit,
    onOpenEqualizer: () -> Unit,
    viewModel: PlayerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val song = uiState.currentSong
    var showSleepTimerSheet by remember { mutableStateOf(false) }
    var showSpeedSheet by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onCollapse) {
                Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Collapse")
            }
            Text("Now Playing", style = MaterialTheme.typography.titleMedium)
            IconButton(onClick = onOpenEqualizer) {
                Icon(Icons.Filled.Tune, contentDescription = "Equalizer")
            }
        }

        Spacer(Modifier.height(24.dp))

        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            androidx.compose.animation.AnimatedContent(
                targetState = song?.albumId,
                transitionSpec = {
                    (androidx.compose.animation.fadeIn(tween(350)) + androidx.compose.animation.scaleIn(initialScale = 0.92f, animationSpec = tween(350)))
                        .togetherWith(androidx.compose.animation.fadeOut(tween(200)))
                },
                label = "artwork"
            ) { albumId ->
                if (albumId != null) {
                    AlbumArtwork(
                        albumId = albumId,
                        cornerRadius = 24,
                        modifier = Modifier.fillMaxWidth().aspectRatio(1f)
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    song?.title ?: "Nothing playing",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    song?.let { it.artistName + " - " + it.albumName } ?: "",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(onClick = { viewModel.toggleFavorite() }) {
                Icon(
                    imageVector = if (uiState.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (uiState.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        val fraction = if (uiState.durationMs > 0) uiState.positionMs.toFloat() / uiState.durationMs else 0f
        val animatedFraction by animateFloatAsState(targetValue = fraction, label = "progress")
        Slider(
            value = animatedFraction.coerceIn(0f, 1f),
            onValueChange = { viewModel.seekTo((it * uiState.durationMs).toLong()) }
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(formatDuration(uiState.positionMs), style = MaterialTheme.typography.labelSmall)
            Text(formatDuration(uiState.durationMs), style = MaterialTheme.typography.labelSmall)
        }

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.toggleShuffle() }) {
                Icon(
                    Icons.Filled.Shuffle, contentDescription = "Shuffle",
                    tint = if (uiState.shuffleEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = { viewModel.skipPrevious() }, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Filled.SkipPrevious, contentDescription = "Previous", modifier = Modifier.size(36.dp))
            }
            FilledIconButton(
                onClick = { viewModel.togglePlayPause() },
                modifier = Modifier.size(72.dp)
            ) {
                Icon(
                    imageVector = if (uiState.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = if (uiState.isPlaying) "Pause" else "Play",
                    modifier = Modifier.size(36.dp)
                )
            }
            IconButton(onClick = { viewModel.skipNext() }, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Filled.SkipNext, contentDescription = "Next", modifier = Modifier.size(36.dp))
            }
            IconButton(onClick = { viewModel.cycleRepeatMode() }) {
                Icon(
                    imageVector = if (uiState.repeatMode == RepeatMode.ONE) Icons.Filled.RepeatOne else Icons.Filled.Repeat,
                    contentDescription = "Repeat",
                    tint = if (uiState.repeatMode != RepeatMode.OFF) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            TextButton(onClick = onOpenQueue) {
                Icon(Icons.Filled.QueueMusic, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp)); Text("Queue")
            }
            TextButton(onClick = { showSpeedSheet = true }) {
                Icon(Icons.Filled.Speed, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp)); Text(uiState.playbackSpeed.toString() + "x")
            }
            TextButton(onClick = { showSleepTimerSheet = true }) {
                Icon(Icons.Filled.Bedtime, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    if (uiState.sleepTimer.isActive)
                        com.bgx.music.core.util.formatRemaining(uiState.sleepTimer.remainingMs)
                    else "Sleep"
                )
            }
        }
    }

    if (showSpeedSheet) {
        PlaybackSpeedSheet(
            current = uiState.playbackSpeed,
            onSelect = { viewModel.setPlaybackSpeed(it); showSpeedSheet = false },
            onDismiss = { showSpeedSheet = false }
        )
    }
    if (showSleepTimerSheet) {
        SleepTimerSheet(
            isActive = uiState.sleepTimer.isActive,
            onSelectMinutes = { viewModel.startSleepTimer(it); showSleepTimerSheet = false },
            onCancel = { viewModel.cancelSleepTimer(); showSleepTimerSheet = false },
            onDismiss = { showSleepTimerSheet = false }
        )
    }
}

private fun tween(durationMs: Int) = androidx.compose.animation.core.tween<Float>(durationMs)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlaybackSpeedSheet(current: Float, onSelect: (Float) -> Unit, onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Text("Playback speed", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(20.dp))
        listOf(0.75f, 1f, 1.25f, 1.5f, 1.75f, 2f).forEach { speed ->
            ListItem(
                headlineContent = { Text(speed.toString() + "x") },
                trailingContent = { if (speed == current) Icon(Icons.Filled.Check, contentDescription = null) },
                modifier = Modifier.clickableRow { onSelect(speed) }
            )
        }
        Spacer(Modifier.height(16.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SleepTimerSheet(isActive: Boolean, onSelectMinutes: (Int) -> Unit, onCancel: () -> Unit, onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Text("Sleep timer", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(20.dp))
        listOf(5, 15, 30, 45, 60, 90).forEach { minutes ->
            ListItem(
                headlineContent = { Text(minutes.toString() + " minutes") },
                modifier = Modifier.clickableRow { onSelectMinutes(minutes) }
            )
        }
        if (isActive) {
            ListItem(
                headlineContent = { Text("Cancel timer", color = MaterialTheme.colorScheme.error) },
                modifier = Modifier.clickableRow { onCancel() }
            )
        }
        Spacer(Modifier.height(16.dp))
    }
}

private fun Modifier.clickableRow(onClick: () -> Unit): Modifier =
    this.clickable(onClick = onClick)
