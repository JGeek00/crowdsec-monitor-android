package com.jgeek00.crowdsecmonitor.extensions

import android.content.Context
import com.jgeek00.crowdsecmonitor.R
import java.time.Instant
import java.time.LocalDate
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

fun String.toRelativeDay(context: Context): String {
    return try {
        val instant = Instant.parse(this)
        val zone = ZoneId.systemDefault()
        val date = instant.atZone(zone).toLocalDate()
        val today = LocalDate.now(zone)
        when (date) {
            today -> context.getString(R.string.today)
            today.minusDays(1) -> context.getString(R.string.yesterday)
            else -> DateTimeFormatter.ofPattern("dd-MM-yyyy").format(date)
        }
    } catch (_: Exception) {
        this
    }
}

fun String.toFormattedTime(): String {
    return try {
        val instant = Instant.parse(this)
        DateTimeFormatter
            .ofPattern("HH:mm:ss")
            .withZone(ZoneId.systemDefault())
            .format(instant)
    } catch (_: Exception) {
        this
    }
}

