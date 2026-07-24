package com.jgeek00.crowdsecmonitor.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class InputFieldStateTest {

    @Test
    fun `initial values are set correctly`() {
        val state = InputFieldState(initialValue = "test", initialError = "error msg", initialEnabled = false)
        assertEquals("test", state.value)
        assertEquals("error msg", state.error)
        assertEquals(false, state.enabled)
    }

    @Test
    fun `default initial values are sensible`() {
        val state = InputFieldState()
        assertEquals("", state.value)
        assertNull(state.error)
        assertTrue(state.enabled)
    }

    @Test
    fun `reset restores defaults`() {
        val state = InputFieldState(initialValue = "test", initialError = "error", initialEnabled = false)
        state.reset()
        assertEquals("", state.value)
        assertNull(state.error)
        assertTrue(state.enabled)
    }

    @Test
    fun `reset works on already default state`() {
        val state = InputFieldState()
        state.reset()
        assertEquals("", state.value)
        assertNull(state.error)
        assertTrue(state.enabled)
    }
}
