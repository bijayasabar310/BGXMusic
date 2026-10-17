package com.bgx.music.core.util

import java.util.concurrent.TimeUnit

fun formatDuration(durationMs: Long): String {
    val totalSeconds = TimeUnit.MILLISECONDS.toSeconds(durationMs.coerceAtLeast(0))
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) String.format("%d:%02d:%02d", hours, minutes, seconds)
    else String.format("%d:%02d", minutes, seconds)
}

fun formatRemaining(remainingMs: Long): String {
    val totalMinutes = TimeUnit.MILLISECONDS.toMinutes(remainingMs.coerceAtLeast(0))
    val seconds = TimeUnit.MILLISECONDS.toSeconds(remainingMs) % 60
    return String.format("%d:%02d", totalMinutes, seconds)
}
