package com.jgeek00.crowdsecmonitor.utils

import com.jgeek00.crowdsecmonitor.R

/**
 * Pure (non-Compose) formatting logic shared by the condition editor UI and its
 * unit tests. Resource IDs are returned instead of resolved strings so the
 * mapping is testable without instrumentation; callers resolve them with
 * [androidx.compose.ui.res.stringResource] or context resources.
 */
object ConditionFormatting {

    /**
     * Split of a rule's value list into the shown head (at most two values) and
     * the count of remaining values rendered as "(+N more)". Values with two or
     * fewer entries are shown in full (moreCount == 0).
     */
    fun valuesSummaryParts(values: List<String>): ValuesSummary {
        return if (values.size <= 2) {
            ValuesSummary(head = values.joinToString(", "), moreCount = 0)
        } else {
            ValuesSummary(head = values.take(2).joinToString(", "), moreCount = values.size - 2)
        }
    }

    data class ValuesSummary(val head: String, val moreCount: Int) {
        val hasMore: Boolean get() = moreCount > 0
    }

    /**
     * Human label for a frequency time window. Returns null when there is no
     * special wording and the plural seconds resource should be used instead.
     */
    fun windowLabelRes(seconds: Int): Int? = when (seconds) {
        60 -> R.string.duration_one_minute
        300 -> R.string.duration_five_minutes
        900 -> R.string.duration_fifteen_minutes
        3600 -> R.string.duration_one_hour
        21600 -> R.string.duration_six_hours
        86400 -> R.string.duration_twentyfour_hours
        else -> null
    }

    /**
     * Human label for a cooldown option. Returns null when the plural seconds
     * resource should be used instead.
     */
    fun cooldownLabelRes(seconds: Int): Int? = when (seconds) {
        60 -> R.string.duration_one_minute
        120 -> R.string.duration_two_minutes
        300 -> R.string.duration_five_minutes
        else -> null
    }
}
