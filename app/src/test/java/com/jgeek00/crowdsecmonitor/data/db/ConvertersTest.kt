package com.jgeek00.crowdsecmonitor.data.db

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.UUID

class ConvertersTest {

    private val converters = Converters()

    // ── UUID round-trip ─────────────────────────────────────────

    @Test
    fun `uuidToString and fromString round-trip a UUID`() {
        val original = UUID.randomUUID()
        val string = converters.uuidToString(original)
        val restored = converters.fromString(string)
        assertEquals(original, restored)
    }

    @Test
    fun `uuidToString returns null for null UUID`() {
        assertNull(converters.uuidToString(null))
    }

    @Test
    fun `fromString returns null for null string`() {
        assertNull(converters.fromString(null))
    }

    // ── Header list round-trip ──────────────────────────────────

    @Test
    fun `headerListToString and stringToHeaderList round-trip multiple pairs`() {
        val original = listOf("key1" to "value1", "key2" to "value2")
        val string = converters.headerListToString(original)
        val restored = converters.stringToHeaderList(string)
        assertEquals(original, restored)
    }

    @Test
    fun `headerListToString returns null for null input`() {
        assertNull(converters.headerListToString(null))
    }

    @Test
    fun `headerListToString returns null for empty list`() {
        assertNull(converters.headerListToString(emptyList()))
    }

    @Test
    fun `stringToHeaderList returns null for null input`() {
        assertNull(converters.stringToHeaderList(null))
    }

    @Test
    fun `stringToHeaderList handles single pair`() {
        val result = converters.stringToHeaderList(converters.headerListToString(listOf("k" to "v")))
        assertEquals(listOf("k" to "v"), result)
    }

    @Test
    fun `stringToHeaderList filters out malformed entries`() {
        // A malformed entry has no unit separator (30) between key and value
        // The headerListToString produces "key1" + chr(31) + "value1" + chr(30) + "key2" + chr(31) + "value2"
        // A malformed entry would be something like "keyonly" (no chr(31))
        // We test by constructing a string with one valid and one invalid entry
        val rs = 30.toChar()  // record separator
        val us = 31.toChar()  // unit separator

        // "key1<US>value1<RS>keyonly" — second entry has no unit separator
        val input = "key1${us}value1${rs}keyonly"
        val result = converters.stringToHeaderList(input)
        assertEquals(listOf("key1" to "value1"), result)
    }

    @Test
    fun `stringToHeaderList handles delimiter characters in keys and values`() {
        val original = listOf("ke:y" to "val:ue", "another" to "da:ta")
        val string = converters.headerListToString(original)
        val restored = converters.stringToHeaderList(string)
        assertEquals(original, restored)
    }
}
