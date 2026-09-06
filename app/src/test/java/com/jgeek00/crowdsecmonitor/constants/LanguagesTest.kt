package com.jgeek00.crowdsecmonitor.constants

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LanguagesTest {

    @Test
    fun `available contains the expected languages`() {
        assertEquals(3, Languages.available.size)
        assertTrue(Languages.available.map { it.tag }.containsAll(listOf("de", "en", "es")))
    }

    @Test
    fun `available is sorted by name`() {
        assertEquals(
            listOf("Deutsch", "English", "Español"),
            Languages.available.map { it.name }
        )
    }

    @Test
    fun `available tags are unique and non-blank`() {
        val tags = Languages.available.map { it.tag }
        assertEquals(tags.size, tags.toSet().size)
        assertTrue(tags.all { it.isNotBlank() })
    }

    @Test
    fun `displayNameFor returns null for null or empty tag`() {
        assertNull(Languages.displayNameFor(null))
        assertNull(Languages.displayNameFor(""))
    }

    @Test
    fun `displayNameFor resolves exact tags`() {
        assertEquals("Deutsch", Languages.displayNameFor("de"))
        assertEquals("English", Languages.displayNameFor("en"))
        assertEquals("Español", Languages.displayNameFor("es"))
    }

    @Test
    fun `displayNameFor resolves region variants by prefix`() {
        assertEquals("Español", Languages.displayNameFor("es-ES"))
        assertEquals("English", Languages.displayNameFor("en-US"))
    }

    @Test
    fun `displayNameFor returns raw tag when unknown`() {
        assertEquals("fr", Languages.displayNameFor("fr"))
    }
}
