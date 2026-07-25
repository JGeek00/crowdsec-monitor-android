package com.jgeek00.crowdsecmonitor.extensions

import com.jgeek00.crowdsecmonitor.MainDispatcherRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit

@RunWith(RobolectricTestRunner::class)
class DateExtensionsRobolectricTest {

    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    private val validIso = "2026-07-24T12:30:45Z"
    private val invalidString = "not-a-date"

    @Test
    fun `toRelativeDay returns text for valid date`() {
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

    @Test
    fun `toRelativeDay returns today for current date`() {
        val context = RuntimeEnvironment.getApplication()
        val now = Instant.now().toString()
        val result = now.toRelativeDay(context)
        assertEquals("Today", result)
    }

    @Test
    fun `toRelativeDay returns yesterday for yesterday`() {
        val context = RuntimeEnvironment.getApplication()
        val yesterday = Instant.now().minus(1, ChronoUnit.DAYS).toString()
        val result = yesterday.toRelativeDay(context)
        assertEquals("Yesterday", result)
    }

    @Test
    fun `toRelativeDay returns formatted date for older date`() {
        val context = RuntimeEnvironment.getApplication()
        val oldDate = Instant.now().minus(10, ChronoUnit.DAYS).toString()
        val result = oldDate.toRelativeDay(context)
        assertNotNull(result)
        // should be formatted as dd-MM-yyyy
        assertTrue(result.matches(Regex("\\d{2}-\\d{2}-\\d{4}")))
    }
}
