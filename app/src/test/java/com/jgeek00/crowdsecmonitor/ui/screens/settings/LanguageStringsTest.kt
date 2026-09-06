package com.jgeek00.crowdsecmonitor.ui.screens.settings

import com.jgeek00.crowdsecmonitor.R
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class LanguageStringsTest {

    private fun string(resId: Int): String =
        RuntimeEnvironment.getApplication().getString(resId)

    @Test
    fun `language strings resolve in default locale`() {
        RuntimeEnvironment.setQualifiers("+en")
        assertEquals("Language", string(R.string.language_section))
        assertEquals("System default", string(R.string.language_system_default))
        assertEquals("General", string(R.string.general))
    }

    @Test
    fun `language strings resolve in Spanish`() {
        RuntimeEnvironment.setQualifiers("+es")
        assertEquals("Idioma", string(R.string.language_section))
        assertEquals("Predeterminado del sistema", string(R.string.language_system_default))
        assertEquals("General", string(R.string.general))
    }

    @Test
    fun `language strings resolve in German`() {
        RuntimeEnvironment.setQualifiers("+de")
        assertEquals("Sprache", string(R.string.language_section))
        assertEquals("Systemvorgabe", string(R.string.language_system_default))
        assertEquals("Allgemein", string(R.string.general))
    }
}
