package com.bgx.music.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bgx.music.BuildConfig
import com.bgx.music.data.preferences.AppTheme
import com.bgx.music.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onOpenEqualizer: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var themeMenuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState())
        ) {
            SettingsSectionLabel("Playback")
            SettingsSwitchRow("Gapless playback", uiState.gaplessPlayback, viewModel::setGaplessPlayback)
            SettingsSwitchRow("Resume on launch", uiState.resumeOnLaunch, viewModel::setResumeOnLaunch)
            SettingsSliderRow(
                "Crossfade", (uiState.crossfadeMs / 1000).toString() + "s",
                value = uiState.crossfadeMs.toFloat(), range = 0f..12000f,
                onValueChange = { viewModel.setCrossfadeMs(it.toInt()) }
            )

            HorizontalDivider(Modifier.padding(vertical = 8.dp))
            SettingsSectionLabel("Library")
            ListItem(
                headlineContent = { Text("Rescan music") },
                supportingContent = { Text(uiState.librarySize.toString() + " songs in library") },
                modifier = Modifier.clickableRow { viewModel.rescanLibrary() }
            )
            ListItem(
                headlineContent = { Text("Excluded folders") },
                supportingContent = { Text(uiState.excludedFolders.size.toString() + " folders excluded") }
            )

            HorizontalDivider(Modifier.padding(vertical = 8.dp))
            SettingsSectionLabel("Appearance")
            ListItem(
                headlineContent = { Text("Theme") },
                supportingContent = { Text(uiState.theme.label()) },
                modifier = Modifier.clickableRow { themeMenuExpanded = true },
                trailingContent = {
                    DropdownMenu(expanded = themeMenuExpanded, onDismissRequest = { themeMenuExpanded = false }) {
                        AppTheme.entries.forEach { theme ->
                            DropdownMenuItem(
                                text = { Text(theme.label()) },
                                onClick = { viewModel.setTheme(theme); themeMenuExpanded = false }
                            )
                        }
                    }
                }
            )
            SettingsSwitchRow("Use dynamic color (Android 12+)", uiState.useDynamicAccent, viewModel::setUseDynamicAccent)
            SettingsSwitchRow("Animations", uiState.animationsEnabled, viewModel::setAnimationsEnabled)

            HorizontalDivider(Modifier.padding(vertical = 8.dp))
            SettingsSectionLabel("Audio")
            ListItem(
                headlineContent = { Text("Equalizer") },
                leadingContent = { Icon(Icons.Filled.Tune, contentDescription = null) },
                modifier = Modifier.clickableRow(onOpenEqualizer)
            )

            HorizontalDivider(Modifier.padding(vertical = 8.dp))
            SettingsSectionLabel("About")
            ListItem(
                headlineContent = { Text("BGX Music") },
                supportingContent = { Text("Version " + BuildConfig.VERSION_NAME) }
            )
            ListItem(headlineContent = { Text("Open-source licenses") })
            ListItem(headlineContent = { Text("Privacy") }, supportingContent = { Text("100% offline. No data ever leaves your device.") })

            Spacer(Modifier.height(100.dp))
        }
    }
}

@Composable
private fun SettingsSectionLabel(text: String) {
    Text(
        text, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
    )
}

@Composable
private fun SettingsSwitchRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    ListItem(
        headlineContent = { Text(label) },
        trailingContent = { Switch(checked = checked, onCheckedChange = onCheckedChange) }
    )
}

@Composable
private fun SettingsSliderRow(
    label: String, valueLabel: String, value: Float, range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.bodyLarge)
            Text(valueLabel, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Slider(value = value, onValueChange = onValueChange, valueRange = range)
    }
}

private fun AppTheme.label(): String = when (this) {
    AppTheme.LIGHT -> "Light"
    AppTheme.DARK -> "Dark"
    AppTheme.AMOLED -> "AMOLED"
    AppTheme.SYSTEM_DYNAMIC -> "System / Dynamic"
}

private fun Modifier.clickableRow(onClick: () -> Unit): Modifier =
    this.clickable(onClick = onClick)
