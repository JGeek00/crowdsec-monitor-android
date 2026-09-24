package com.jgeek00.crowdsecmonitor.extensions

import android.content.Context
import com.jgeek00.crowdsecmonitor.R
import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

/**
 * Canonical API timestamp format: `YYYY-MM-DD HH:MM:SS ±ZZZZ ZZZ`
 * (numeric offset repeated as the zone field, e.g. `2026-09-24 16:19:29 +0200 +0200`).
 */
private val canonicalTimestampPattern =
    Regex("^(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}) ([+-]\\d{4}) [+-]\\d{4}$")

private val canonicalOffsetFormatter =
    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss Z", Locale.ENGLISH)

/**
 * Parses an API timestamp into an [Instant]: accepts ISO 8601 (Instant.parse)
 * and the canonical format, preserving the instant the value represents.
 * Returns null when the value cannot be interpreted.
 */
fun String.toInstant(): Instant? {
    if (isBlank()) return null
    try {
        return Instant.parse(this)
    } catch (_: Exception) {
        // Fall through to the canonical format parser
    }
    val match = canonicalTimestampPattern.find(this) ?: return null
    return try {
        OffsetDateTime.parse("${match.groupValues[1]} ${match.groupValues[2]}", canonicalOffsetFormatter)
            .toInstant()
    } catch (_: Exception) {
        null
    }
}

fun String.toFormattedDate(style: FormatStyle = FormatStyle.MEDIUM): String {
    val instant = toInstant() ?: return this
    return DateTimeFormatter
        .ofLocalizedDate(style)
        .withZone(ZoneId.systemDefault())
        .format(instant)
}

fun String.toFormattedDateTime(style: FormatStyle = FormatStyle.MEDIUM): String {
    val instant = toInstant() ?: return this
    return DateTimeFormatter
        .ofLocalizedDateTime(style)
        .withZone(ZoneId.systemDefault())
        .format(instant)
}

fun String.toRelativeDay(context: Context): String {
    val instant = toInstant() ?: return this
    val zone = ZoneId.systemDefault()
    val date = instant.atZone(zone).toLocalDate()
    val today = LocalDate.now(zone)
    return when (date) {
        today -> context.getString(R.string.today)
        today.minusDays(1) -> context.getString(R.string.yesterday)
        else -> DateTimeFormatter.ofPattern("dd-MM-yyyy").format(date)
    }
}

fun String.toFormattedTime(): String {
    val instant = toInstant() ?: return this
    return DateTimeFormatter
        .ofPattern("HH:mm:ss")
        .withZone(ZoneId.systemDefault())
        .format(instant)
}

fun String.toFormattedDateTimeCustom(): String {
    val instant = toInstant() ?: return this
    return DateTimeFormatter
        .ofPattern("dd MMM. yyyy HH:mm:ss")
        .withLocale(Locale.ENGLISH)
        .withZone(ZoneId.systemDefault())
        .format(instant)
}

fun String.toFormattedTimeOrNull(): String? {
    val instant = toInstant() ?: return null
    return DateTimeFormatter
        .ofPattern("HH:mm:ss")
        .withZone(ZoneId.systemDefault())
        .format(instant)
}
