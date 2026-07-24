package com.jgeek00.crowdsecmonitor.extensions

import com.jgeek00.crowdsecmonitor.MainDispatcherRule
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class DateExtensionsTest {

    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    private val validIso = "2026-07-24T12:30:45Z"
    private val invalidString = "not-a-date"

    // ── toFormattedDate ─────────────────────────────────────────

    @Test
    fun `toFormattedDate returns formatted date for valid ISO string`() {
        val result = validIso.toFormattedDate()
        assertNotNull(result)
        assertFalse(result.isEmpty())
        assertNotEquals(validIso, result)
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
        assertFalse(result.isEmpty())
        assertNotEquals(validIso, result)
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
        assertFalse(result.isEmpty())
        assertNotEquals(validIso, result)
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

    // ── toFormattedTimeOrNull ────────────────────────────────────

    @Test
    fun `toFormattedTimeOrNull returns time for valid ISO`() {
        val result = validIso.toFormattedTimeOrNull()
        assertNotNull(result)
        assertTrue(result!!.matches(Regex("\\d{2}:\\d{2}:\\d{2}")))
    }

    @Test
    fun `toFormattedTimeOrNull returns null for invalid input`() {
        val result = invalidString.toFormattedTimeOrNull()
        assertNull(result)
    }
}
