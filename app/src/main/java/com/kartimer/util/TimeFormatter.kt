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

    fun formatLapTime(ms: Long): String {
        val minutes = ms / 60_000L
        val seconds = (ms % 60_000L) / 1_000L
        val millis  = ms % 1_000L
        return String.format(Locale.ROOT, "%d:%02d.%03d", minutes, seconds, millis)
    }

    fun parseLapTime(input: String): Long? {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return null
        return try {
            val colonIdx = trimmed.indexOf(':')
            val dotIdx   = trimmed.indexOf('.')
            when {
                colonIdx >= 0 && dotIdx > colonIdx -> {
                    val min = trimmed.substring(0, colonIdx).toLong()
                    val sec = trimmed.substring(colonIdx + 1, dotIdx).toLong()
                    val ms  = trimmed.substring(dotIdx + 1).padEnd(3, '0').take(3).toLong()
                    (min * 60_000L + sec * 1_000L + ms).takeIf { sec < 60 }
                }
                dotIdx >= 0 -> {
                    val sec = trimmed.substring(0, dotIdx).toLong()
                    val ms  = trimmed.substring(dotIdx + 1).padEnd(3, '0').take(3).toLong()
                    sec * 1_000L + ms
                }
                else -> trimmed.toLong() * 1_000L
            }
        } catch (_: NumberFormatException) { null }
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
