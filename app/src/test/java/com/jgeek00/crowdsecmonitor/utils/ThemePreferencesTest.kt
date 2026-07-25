package com.jgeek00.crowdsecmonitor.utils

import android.content.SharedPreferences
import com.jgeek00.crowdsecmonitor.constants.Enums
import com.jgeek00.crowdsecmonitor.constants.StorageKeys
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Test

class ThemePreferencesTest {

    @Test
    fun `readThemeMode returns SYSTEM when no stored value`() {
        val prefs = mockk<SharedPreferences>(relaxed = true)
        every { prefs.getString(StorageKeys.THEME, null) } returns null

        val result = prefs.readThemeMode()

        assertEquals(Enums.ThemeMode.SYSTEM, result)
    }

    @Test
    fun `readThemeMode returns stored value when valid`() {
        val prefs = mockk<SharedPreferences>(relaxed = true)
        every { prefs.getString(StorageKeys.THEME, null) } returns "LIGHT"

        val result = prefs.readThemeMode()

        assertEquals(Enums.ThemeMode.LIGHT, result)
    }

    @Test
    fun `readThemeMode returns SYSTEM for invalid stored value`() {
        val prefs = mockk<SharedPreferences>(relaxed = true)
        every { prefs.getString(StorageKeys.THEME, null) } returns "INVALID"

        val result = prefs.readThemeMode()

        assertEquals(Enums.ThemeMode.SYSTEM, result)
    }

    @Test
    fun `readThemeMode returns DARK when stored`() {
        val prefs = mockk<SharedPreferences>(relaxed = true)
        every { prefs.getString(StorageKeys.THEME, null) } returns "DARK"

        val result = prefs.readThemeMode()

        assertEquals(Enums.ThemeMode.DARK, result)
    }

    @Test
    fun `writeThemeMode stores theme name`() {
        val editor = mockk<SharedPreferences.Editor>(relaxed = true)
        val prefs = mockk<SharedPreferences>(relaxed = true)
        every { prefs.edit() } returns editor
        every { editor.putString(any(), any()) } returns editor
        every { editor.apply() } returns Unit

        prefs.writeThemeMode(Enums.ThemeMode.LIGHT)

        verify { editor.putString(StorageKeys.THEME, "LIGHT") }
        verify { editor.apply() }
    }

    @Test
    fun `writeThemeMode stores DARK theme`() {
        val editor = mockk<SharedPreferences.Editor>(relaxed = true)
        val prefs = mockk<SharedPreferences>(relaxed = true)
        every { prefs.edit() } returns editor
        every { editor.putString(any(), any()) } returns editor

        prefs.writeThemeMode(Enums.ThemeMode.DARK)

        verify { editor.putString(StorageKeys.THEME, "DARK") }
    }
}
