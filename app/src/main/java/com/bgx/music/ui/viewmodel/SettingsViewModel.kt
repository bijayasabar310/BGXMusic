package com.bgx.music.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bgx.music.data.preferences.*
import com.bgx.music.data.repository.MusicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val theme: AppTheme = AppTheme.SYSTEM_DYNAMIC,
    val useDynamicAccent: Boolean = true,
    val gaplessPlayback: Boolean = true,
    val crossfadeMs: Int = 0,
    val resumeOnLaunch: Boolean = true,
    val animationsEnabled: Boolean = true,
    val libraryViewStyle: LibraryViewStyle = LibraryViewStyle.GRID,
    val excludedFolders: Set<String> = emptySet(),
    val librarySize: Int = 0
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferences: UserPreferences,
    private val musicRepository: MusicRepository
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        preferences.theme, preferences.useDynamicAccent, preferences.gaplessPlayback,
        preferences.crossfadeMs, preferences.resumeOnLaunch, preferences.animationsEnabled,
        preferences.libraryViewStyle, preferences.excludedFolders, musicRepository.observeLibrarySize()
    ) { values ->
        SettingsUiState(
            theme = values[0] as AppTheme,
            useDynamicAccent = values[1] as Boolean,
            gaplessPlayback = values[2] as Boolean,
            crossfadeMs = values[3] as Int,
            resumeOnLaunch = values[4] as Boolean,
            animationsEnabled = values[5] as Boolean,
            libraryViewStyle = values[6] as LibraryViewStyle,
            excludedFolders = values[7] as Set<String>,
            librarySize = values[8] as Int
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsUiState())

    fun setTheme(theme: AppTheme) = viewModelScope.launch { preferences.setTheme(theme) }
    fun setUseDynamicAccent(enabled: Boolean) = viewModelScope.launch { preferences.setUseDynamicAccent(enabled) }
    fun setAccentColor(argb: Int) = viewModelScope.launch { preferences.setAccentColorArgb(argb) }
    fun setGaplessPlayback(enabled: Boolean) = viewModelScope.launch { preferences.setGaplessPlayback(enabled) }
    fun setCrossfadeMs(ms: Int) = viewModelScope.launch { preferences.setCrossfadeMs(ms) }
    fun setResumeOnLaunch(enabled: Boolean) = viewModelScope.launch { preferences.setResumeOnLaunch(enabled) }
    fun setAnimationsEnabled(enabled: Boolean) = viewModelScope.launch { preferences.setAnimationsEnabled(enabled) }
    fun setLibraryViewStyle(style: LibraryViewStyle) = viewModelScope.launch { preferences.setLibraryViewStyle(style) }

    fun toggleExcludedFolder(path: String) = viewModelScope.launch {
        val current = uiState.value.excludedFolders
        preferences.setExcludedFolders(if (path in current) current - path else current + path)
    }

    fun rescanLibrary() = viewModelScope.launch { musicRepository.rescanLibrary() }
}
