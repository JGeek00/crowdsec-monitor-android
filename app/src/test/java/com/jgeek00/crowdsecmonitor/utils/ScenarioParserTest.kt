package com.jgeek00.crowdsecmonitor.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class ScenarioParserTest {

    @Test
    fun `parseScenario returns author and name when separator present`() {
        val result = parseScenario("crowdsec/ssh-bf")
        assertEquals("crowdsec", result.author)
        assertEquals("ssh-bf", result.name)
    }

    @Test
    fun `parseScenario returns full string as author when no separator`() {
        val result = parseScenario("no-separator")
        assertEquals("no-separator", result.author)
        assertEquals("", result.name)
    }

    @Test
    fun `parseScenario returns empty strings for empty input`() {
        val result = parseScenario("")
        assertEquals("", result.author)
        assertEquals("", result.name)
    }

    @Test
    fun `parseScenario splits at first separator only with limit 2`() {
        val result = parseScenario("author/name/extra")
        assertEquals("author", result.author)
        assertEquals("name/extra", result.name)
    }

    @Test
    fun `parseScenario handles single character input`() {
        val result = parseScenario("a")
        assertEquals("a", result.author)
        assertEquals("", result.name)
    }

    @Test
    fun `parseScenario handles trailing separator`() {
        val result = parseScenario("author/")
        assertEquals("author", result.author)
        assertEquals("", result.name)
    }
}
