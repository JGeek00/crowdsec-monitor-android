package com.jgeek00.crowdsecmonitor.ui.screens.settings.notifications

import com.jgeek00.crowdsecmonitor.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ConditionFormattingTest {

    // MARK: - valuesSummaryParts

    @Test
    fun `values summary shows empty list as empty head`() {
        val summary = ConditionFormatting.valuesSummaryParts(emptyList())
        assertEquals("", summary.head)
        assertEquals(0, summary.moreCount)
        assertFalse(summary.hasMore)
    }

    @Test
    fun `values summary shows up to two values in full`() {
        val one = ConditionFormatting.valuesSummaryParts(listOf("ssh:brute-force"))
        assertEquals("ssh:brute-force", one.head)
        assertFalse(one.hasMore)

        val two = ConditionFormatting.valuesSummaryParts(listOf("a", "b"))
        assertEquals("a, b", two.head)
        assertEquals(0, two.moreCount)
        assertFalse(two.hasMore)
    }

    @Test
    fun `values summary truncates to two values plus N more`() {
        val summary = ConditionFormatting.valuesSummaryParts(listOf("a", "b", "c"))
        assertEquals("a, b", summary.head)
        assertEquals(1, summary.moreCount)
        assertTrue(summary.hasMore)

        val five = ConditionFormatting.valuesSummaryParts(listOf("a", "b", "c", "d", "e"))
        assertEquals("a, b", five.head)
        assertEquals(3, five.moreCount)
        assertTrue(five.hasMore)
    }

    // MARK: - windowLabelRes

    @Test
    fun `window labels map to human durations`() {
        assertEquals(R.string.duration_one_minute, ConditionFormatting.windowLabelRes(60))
        assertEquals(R.string.duration_five_minutes, ConditionFormatting.windowLabelRes(300))
        assertEquals(R.string.duration_fifteen_minutes, ConditionFormatting.windowLabelRes(900))
        assertEquals(R.string.duration_one_hour, ConditionFormatting.windowLabelRes(3600))
        assertEquals(R.string.duration_six_hours, ConditionFormatting.windowLabelRes(21600))
        assertEquals(R.string.duration_twentyfour_hours, ConditionFormatting.windowLabelRes(86400))
    }

    @Test
    fun `window labels fall back to plural seconds outside special cases`() {
        assertNull(ConditionFormatting.windowLabelRes(10))
        assertNull(ConditionFormatting.windowLabelRes(59))
        assertNull(ConditionFormatting.windowLabelRes(7200))
    }

    // MARK: - cooldownLabelRes

    @Test
    fun `cooldown labels map to human durations`() {
        assertEquals(R.string.duration_one_minute, ConditionFormatting.cooldownLabelRes(60))
        assertEquals(R.string.duration_two_minutes, ConditionFormatting.cooldownLabelRes(120))
        assertEquals(R.string.duration_five_minutes, ConditionFormatting.cooldownLabelRes(300))
    }

    @Test
    fun `cooldown labels fall back to plural seconds outside special cases`() {
        assertNull(ConditionFormatting.cooldownLabelRes(10))
        assertNull(ConditionFormatting.cooldownLabelRes(20))
        assertNull(ConditionFormatting.cooldownLabelRes(30))
    }
}
