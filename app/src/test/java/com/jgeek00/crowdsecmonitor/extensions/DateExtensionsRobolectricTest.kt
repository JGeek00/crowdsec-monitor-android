package com.jgeek00.crowdsecmonitor.extensions

import com.jgeek00.crowdsecmonitor.MainDispatcherRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

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
}
