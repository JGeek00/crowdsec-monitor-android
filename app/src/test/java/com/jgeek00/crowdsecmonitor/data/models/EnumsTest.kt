package com.jgeek00.crowdsecmonitor.data.models

import com.jgeek00.crowdsecmonitor.constants.Enums
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class EnumsTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `DecisionType BAN serializes to ban`() {
        val result = json.encodeToString(Enums.DecisionType.BAN)
        assertEquals("\"ban\"", result)
    }

    @Test
    fun `DecisionType CAPTCHA serializes to captcha`() {
        val result = json.encodeToString(Enums.DecisionType.CAPTCHA)
        assertEquals("\"captcha\"", result)
    }

    @Test
    fun `DecisionType deserializes from ban`() {
        val result = json.decodeFromString<Enums.DecisionType>("\"ban\"")
        assertEquals(Enums.DecisionType.BAN, result)
    }

    @Test
    fun `DecisionType deserializes from captcha`() {
        val result = json.decodeFromString<Enums.DecisionType>("\"captcha\"")
        assertEquals(Enums.DecisionType.CAPTCHA, result)
    }

    @Test
    fun `AuthMethod values map correctly`() {
        assertEquals("none", Enums.AuthMethod.NONE.value)
        assertEquals("basic", Enums.AuthMethod.BASIC.value)
        assertEquals("bearer", Enums.AuthMethod.BEARER.value)
    }

    @Test
    fun `ConnectionMethod values map correctly`() {
        assertEquals("http", Enums.ConnectionMethod.HTTP.value)
        assertEquals("https", Enums.ConnectionMethod.HTTPS.value)
    }
}
