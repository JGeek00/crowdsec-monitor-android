package com.jgeek00.crowdsecmonitor.constants

import org.junit.Assert.assertEquals
import org.junit.Test

class URLsTest {

    @Test
    fun `crowdsecHubScenario returns author and name path when name is present`() {
        val result = URLs.crowdsecHubScenario("crowdsec/ssh-bf")
        assertEquals("https://hub.crowdsec.net/author/crowdsec/configurations/ssh-bf", result)
    }

    @Test
    fun `crowdsecHubScenario returns base hub URL when no separator`() {
        val result = URLs.crowdsecHubScenario("no-separator")
        assertEquals("https://hub.crowdsec.net", result)
    }

    @Test
    fun `crowdsecHubScenario returns base hub URL for empty string`() {
        val result = URLs.crowdsecHubScenario("")
        assertEquals("https://hub.crowdsec.net", result)
    }

    @Test
    fun `crowdsecHubScenario handles name after slash that is blank`() {
        val result = URLs.crowdsecHubScenario("author/")
        assertEquals("https://hub.crowdsec.net", result)
    }

    @Test
    fun `crowdsecHubScenario handles complex scenario name`() {
        val result = URLs.crowdsecHubScenario("crowdsec/nginx-bf")
        assertEquals("https://hub.crowdsec.net/author/crowdsec/configurations/nginx-bf", result)
    }
}
