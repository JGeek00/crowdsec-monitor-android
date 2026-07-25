package com.jgeek00.crowdsecmonitor.extensions

import com.jgeek00.crowdsecmonitor.MainDispatcherRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class DateExtensionsFullTest {

    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    private val validIso = "2026-07-24T12:30:45Z"
    private val invalidString = "not-a-date"

    @Test
    fun `toFormattedDate MEDIUM style`() {
        val result = validIso.toFormattedDate()
        assertNotNull(result)
        assertTrue(result.contains("2026"))
    }

    @Test
    fun `toFormattedDate FULL style`() {
        val result = validIso.toFormattedDate(java.time.format.FormatStyle.FULL)
        assertNotNull(result)
        assertFalse(result.isEmpty())
    }

    @Test
    fun `toFormattedDate returns original for invalid`() {
        assertEquals(invalidString, invalidString.toFormattedDate())
    }

    @Test
    fun `toFormattedDateTime SHORT style`() {
        val result = validIso.toFormattedDateTime(java.time.format.FormatStyle.SHORT)
        assertNotNull(result)
        assertFalse(result.isEmpty())
    }

    @Test
    fun `toFormattedDateTime returns original for invalid`() {
        assertEquals(invalidString, invalidString.toFormattedDateTime())
    }

    @Test
    fun `toFormattedTime returns HHmmss pattern`() {
        val result = validIso.toFormattedTime()
        assertTrue(result.matches(Regex("\\d{2}:\\d{2}:\\d{2}")))
    }

    @Test
    fun `toFormattedTime returns original for invalid`() {
        assertEquals(invalidString, invalidString.toFormattedTime())
    }

    @Test
    fun `toFormattedDateTimeCustom returns custom format`() {
        val result = validIso.toFormattedDateTimeCustom()
        assertNotNull(result)
        assertTrue(result.contains("Jul") || result.contains("24"))
    }

    @Test
    fun `toFormattedDateTimeCustom returns original for invalid`() {
        assertEquals(invalidString, invalidString.toFormattedDateTimeCustom())
    }

    @Test
    fun `toInstant works for valid ISO`() {
        val result = validIso.toInstant()
        assertNotNull(result)
        assertEquals("2026-07-24T12:30:45Z", result.toString())
    }

    @Test
    fun `toInstant returns null for invalid`() {
        assertNull(invalidString.toInstant())
    }

    @Test
    fun `toFormattedTimeOrNull returns time for valid`() {
        val result = validIso.toFormattedTimeOrNull()
        assertNotNull(result)
        assertTrue(result!!.matches(Regex("\\d{2}:\\d{2}:\\d{2}")))
    }

    @Test
    fun `toFormattedTimeOrNull returns null for invalid`() {
        assertNull(invalidString.toFormattedTimeOrNull())
    }
}
