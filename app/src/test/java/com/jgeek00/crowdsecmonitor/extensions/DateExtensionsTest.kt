package com.jgeek00.crowdsecmonitor.extensions

import com.jgeek00.crowdsecmonitor.MainDispatcherRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class DateExtensionsTest {

    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    private val validIso = "2026-07-24T12:30:45Z"
    private val invalidString = "not-a-date"

    // ── toFormattedDate ─────────────────────────────────────────

    @Test
    fun `toFormattedDate returns formatted date for valid ISO string`() {
        val result = validIso.toFormattedDate()
        assertNotNull(result)
        assertEquals(false, result.isEmpty())
        assertEquals(false, result == validIso)
    }

    @Test
    fun `toFormattedDate returns original string for invalid input`() {
        val result = invalidString.toFormattedDate()
        assertEquals(invalidString, result)
    }

    // ── toFormattedDateTime ─────────────────────────────────────

    @Test
    fun `toFormattedDateTime returns formatted datetime for valid ISO string`() {
        val result = validIso.toFormattedDateTime()
        assertNotNull(result)
        assertEquals(false, result.isEmpty())
        assertEquals(false, result == validIso)
    }

    @Test
    fun `toFormattedDateTime returns original string for invalid input`() {
        val result = invalidString.toFormattedDateTime()
        assertEquals(invalidString, result)
    }

    // ── toFormattedTime ─────────────────────────────────────────

    @Test
    fun `toFormattedTime returns time string for valid ISO string`() {
        val result = validIso.toFormattedTime()
        assertNotNull(result)
        assertTrue(result.matches(Regex("\\d{2}:\\d{2}:\\d{2}")))
    }

    @Test
    fun `toFormattedTime returns original string for invalid input`() {
        val result = invalidString.toFormattedTime()
        assertEquals(invalidString, result)
    }

    // ── toFormattedDateTimeCustom ────────────────────────────────

    @Test
    fun `toFormattedDateTimeCustom returns custom format for valid ISO string`() {
        val result = validIso.toFormattedDateTimeCustom()
        assertNotNull(result)
        assertEquals(false, result.isEmpty())
        assertEquals(false, result == validIso)
    }

    @Test
    fun `toFormattedDateTimeCustom returns original string for invalid input`() {
        val result = invalidString.toFormattedDateTimeCustom()
        assertEquals(invalidString, result)
    }

    // ── toInstant ────────────────────────────────────────────────

    @Test
    fun `toInstant returns parsed Instant for valid ISO string`() {
        val result = validIso.toInstant()
        assertNotNull(result)
        assertEquals("2026-07-24T12:30:45Z", result.toString())
    }

    @Test
    fun `toInstant returns null for invalid input`() {
        val result = invalidString.toInstant()
        assertNull(result)
    }

    // ── toRelativeDay ───────────────────────────────────────────

    @Test
    fun `toRelativeDay returns text for valid date under Robolectric`() {
        val context = RuntimeEnvironment.getApplication()
        val result = validIso.toRelativeDay(context)
        assertNotNull(result)
    }

    @Test
    fun `toRelativeDay returns original string for invalid input`() {
        val context = RuntimeEnvironment.getApplication()
        val result = invalidString.toRelativeDay(context)
        assertEquals(invalidString, result)
    }
}
