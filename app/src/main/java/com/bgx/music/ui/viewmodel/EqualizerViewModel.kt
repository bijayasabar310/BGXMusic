package com.bgx.music.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bgx.music.data.preferences.UserPreferences
import com.bgx.music.playback.AudioEffectsCapabilities
import com.bgx.music.playback.AudioEffectsController
import com.bgx.music.playback.EqBand
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EqualizerUiState(
    val capabilities: AudioEffectsCapabilities = AudioEffectsCapabilities(false, false, false, false),
    val bands: List<EqBand> = emptyList(),
    val presets: List<String> = emptyList(),
    val bassBoost: Int = 0,
    val virtualizer: Int = 0,
    val balance: Float = 0f,
    val equalizerEnabled: Boolean = true
)

@HiltViewModel
class EqualizerViewModel @Inject constructor(
    private val audioEffects: AudioEffectsController,
    private val preferences: UserPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(EqualizerUiState())
    val uiState: StateFlow<EqualizerUiState> = _uiState.asStateFlow()

    init {
        refreshFromEngine()
        viewModelScope.launch {
            preferences.bassBoostStrength.collect { setBassBoost(it, persist = false) }
        }
        viewModelScope.launch {
            preferences.virtualizerStrength.collect { setVirtualizer(it, persist = false) }
        }
        viewModelScope.launch {
            preferences.balance.collect { setBalance(it, persist = false) }
        }
    }

    private fun refreshFromEngine() {
        _uiState.value = _uiState.value.copy(
            capabilities = audioEffects.capabilities(),
            bands = audioEffects.getBands(),
            presets = audioEffects.getPresetNames()
        )
    }

    fun setBandLevel(band: Short, level: Short) {
        audioEffects.setBandLevel(band, level)
        _uiState.value = _uiState.value.copy(bands = audioEffects.getBands())
    }

    fun usePreset(index: Short) {
        audioEffects.usePreset(index)
        _uiState.value = _uiState.value.copy(bands = audioEffects.getBands())
    }

    fun setBassBoost(strength: Int, persist: Boolean = true) {
        audioEffects.setBassBoostStrength(strength.toShort())
        _uiState.value = _uiState.value.copy(bassBoost = strength)
        if (persist) viewModelScope.launch { preferences.setBassBoostStrength(strength) }
    }

    fun setVirtualizer(strength: Int, persist: Boolean = true) {
        audioEffects.setVirtualizerStrength(strength.toShort())
        _uiState.value = _uiState.value.copy(virtualizer = strength)
        if (persist) viewModelScope.launch { preferences.setVirtualizerStrength(strength) }
    }

    fun setBalance(value: Float, persist: Boolean = true) {
        _uiState.value = _uiState.value.copy(balance = value)
        if (persist) viewModelScope.launch { preferences.setBalance(value) }
    }

    fun setEqualizerEnabled(enabled: Boolean) {
        audioEffects.setEqualizerEnabled(enabled)
        _uiState.value = _uiState.value.copy(equalizerEnabled = enabled)
    }
}
