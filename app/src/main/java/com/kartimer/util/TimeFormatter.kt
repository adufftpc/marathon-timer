package com.kartimer.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object TimeFormatter {

    /**
     * Format seconds as "HH:MM:SS"
     */
    fun formatSeconds(totalSeconds: Long): String {
        val absSeconds = if (totalSeconds < 0) 0L else totalSeconds
        val hours = absSeconds / 3600
        val minutes = (absSeconds % 3600) / 60
        val seconds = absSeconds % 60
        return String.format(Locale.ROOT, "%02d:%02d:%02d", hours, minutes, seconds)
    }

    /**
     * Format seconds as "MM:SS" (for shorter durations)
     */
    fun formatMinSec(totalSeconds: Long): String {
        val absSeconds = if (totalSeconds < 0) 0L else totalSeconds
        val minutes = absSeconds / 60
        val seconds = absSeconds % 60
        return String.format(Locale.ROOT, "%02d:%02d", minutes, seconds)
    }

    fun formatTimestamp(epochMillis: Long): String {
        if (epochMillis <= 0L) return "—"
        return SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(epochMillis))
    }

    /**
     * Format elapsed/remaining display text
     */
    fun formatTimerDisplay(elapsedSeconds: Long, totalSeconds: Long): Pair<String, String> {
        val elapsed = formatSeconds(elapsedSeconds)
        val remaining = formatSeconds(maxOf(0L, totalSeconds - elapsedSeconds))
        return elapsed to remaining
    }
}
