package com.jgeek00.crowdsecmonitor.extensions

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

fun String.toFormattedDate(style: FormatStyle = FormatStyle.MEDIUM): String {
    return try {
        val instant = Instant.parse(this)
        DateTimeFormatter
            .ofLocalizedDate(style)
            .withZone(ZoneId.systemDefault())
            .format(instant)
    } catch (_: Exception) {
        this
    }
}

fun String.toInstant(): java.time.Instant? {
    return try {
        java.time.Instant.parse(this)
    } catch (_: Exception) {
        null
    }
}

fun String.toFormattedDateTime(style: FormatStyle = FormatStyle.MEDIUM): String {
    return try {
        val instant = Instant.parse(this)
        DateTimeFormatter
            .ofLocalizedDateTime(style)
            .withZone(ZoneId.systemDefault())
            .format(instant)
    } catch (_: Exception) {
        this
    }
}
