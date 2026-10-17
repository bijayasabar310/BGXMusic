package com.bgx.music.playback

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

enum class SleepTimerEndAction { PAUSE, STOP_AFTER_TRACK }

data class SleepTimerState(
    val isActive: Boolean = false,
    val remainingMs: Long = 0L,
    val endAction: SleepTimerEndAction = SleepTimerEndAction.PAUSE
)

@Singleton
class SleepTimerManager @Inject constructor() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var tickJob: Job? = null

    private val _state = MutableStateFlow(SleepTimerState())
    val state: StateFlow<SleepTimerState> = _state.asStateFlow()

    var onFinished: (() -> Unit)? = null

    fun start(durationMs: Long, endAction: SleepTimerEndAction = SleepTimerEndAction.PAUSE) {
        cancel()
        _state.value = SleepTimerState(isActive = true, remainingMs = durationMs, endAction = endAction)
        tickJob = scope.launch {
            var remaining = durationMs
            while (remaining > 0) {
                delay(1000)
                remaining -= 1000
                _state.value = _state.value.copy(remainingMs = remaining.coerceAtLeast(0))
            }
            _state.value = SleepTimerState()
            onFinished?.invoke()
        }
    }

    fun addTime(deltaMs: Long) {
        if (!_state.value.isActive) return
        start((_state.value.remainingMs + deltaMs).coerceAtLeast(0), _state.value.endAction)
    }

    fun cancel() {
        tickJob?.cancel()
        tickJob = null
        _state.value = SleepTimerState()
    }
}
