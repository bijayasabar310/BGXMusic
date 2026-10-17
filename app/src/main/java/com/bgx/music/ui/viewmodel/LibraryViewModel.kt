package com.bgx.music.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bgx.music.data.repository.MusicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Owns device-library synchronisation. The library is scanned automatically as
 * soon as audio permission has been granted, so the app never sits on an empty
 * database on first launch.
 */
@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val musicRepository: MusicRepository
) : ViewModel() {

    private var scanStarted = false

    /** Kicks off a device scan once per app session. */
    fun syncLibraryOnce() {
        if (scanStarted) return
        scanStarted = true
        viewModelScope.launch { musicRepository.rescanLibrary() }
    }

    fun rescan() = viewModelScope.launch { musicRepository.rescanLibrary() }
}
