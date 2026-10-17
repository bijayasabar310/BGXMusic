package com.bgx.music.ui.screens.equalizer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bgx.music.ui.components.EmptyState
import com.bgx.music.ui.viewmodel.EqualizerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EqualizerScreen(
    onBack: () -> Unit,
    viewModel: EqualizerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Equalizer") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Switch(
                        checked = uiState.equalizerEnabled,
                        onCheckedChange = { viewModel.setEqualizerEnabled(it) },
                        modifier = Modifier.padding(end = 12.dp)
                    )
                }
            )
        }
    ) { padding ->
        if (!uiState.capabilities.equalizerAvailable) {
            EmptyState(
                title = "Equalizer not supported",
                subtitle = "This device does not expose audio effects for this app to control.",
                icon = Icons.Filled.GraphicEq,
                modifier = Modifier.fillMaxSize().padding(padding)
            )
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            if (uiState.presets.isNotEmpty()) {
                Text("Presets", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(vertical = 12.dp))
                androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(uiState.presets.size) { index ->
                        AssistChip(
                            onClick = { viewModel.usePreset(index.toShort()) },
                            label = { Text(uiState.presets[index]) }
                        )
                    }
                }
                Spacer(Modifier.height(20.dp))
            }

            Text("Bands", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth().height(220.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                uiState.bands.forEach { band ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxHeight()) {
                        Slider(
                            value = band.levelMillibel.toFloat(),
                            onValueChange = { viewModel.setBandLevel(band.index, it.toInt().toShort()) },
                            valueRange = band.minMb.toFloat()..band.maxMb.toFloat(),
                            modifier = Modifier
                                .graphicsLayerRotateVertical()
                                .width(160.dp)
                        )
                        Text(band.frequencyHz.toString() + "Hz", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            if (uiState.capabilities.bassBoostAvailable) {
                Text("Bass Boost", style = MaterialTheme.typography.titleMedium)
                Slider(
                    value = uiState.bassBoost.toFloat(),
                    onValueChange = { viewModel.setBassBoost(it.toInt()) },
                    valueRange = 0f..1000f
                )
                Spacer(Modifier.height(16.dp))
            }

            if (uiState.capabilities.virtualizerAvailable) {
                Text("Virtualizer (Surround)", style = MaterialTheme.typography.titleMedium)
                Slider(
                    value = uiState.virtualizer.toFloat(),
                    onValueChange = { viewModel.setVirtualizer(it.toInt()) },
                    valueRange = 0f..1000f
                )
                Spacer(Modifier.height(16.dp))
            }

            Text("Balance", style = MaterialTheme.typography.titleMedium)
            Slider(
                value = uiState.balance,
                onValueChange = { viewModel.setBalance(it) },
                valueRange = -1f..1f
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("L", style = MaterialTheme.typography.labelSmall)
                Text("R", style = MaterialTheme.typography.labelSmall)
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun Modifier.graphicsLayerRotateVertical(): Modifier = this.then(
    Modifier.graphicsLayer(rotationZ = -90f)
)
