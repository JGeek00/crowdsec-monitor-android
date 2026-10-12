package com.jgeek00.crowdsecmonitor.constants

/**
 * Notification defaults mirroring the iOS Constants/Defaults.swift values for
 * the notification form (threshold "x times in y seconds" + cooldown picker).
 */
object NotificationDefaults {
    const val DEFAULT_COUNT = 3
    const val DEFAULT_WINDOW_SECONDS_TEXT = "10"
    const val WINDOW_MIN_SECONDS = 10
    const val WINDOW_MAX_SECONDS = 86400
    const val DEFAULT_COOLDOWN_SECONDS = 60
    val COOLDOWN_OPTIONS = listOf(10, 20, 30, 60, 120, 300)
}
