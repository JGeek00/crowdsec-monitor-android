package com.jgeek00.crowdsecmonitor.utils

import com.jgeek00.crowdsecmonitor.data.db.CSServerModel
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.UUID

class ServerUtilsTest {

    @Test
    fun `buildServerUrl returns http domain when port is null`() {
        val server = CSServerModel(
            id = UUID.randomUUID(), name = "Test", http = "http",
            domain = "example.com", port = null, path = null,
            authMethod = "none", basicUser = null, basicPassword = null, bearerToken = null
        )
        assertEquals("http://example.com", buildServerUrl(server))
    }

    @Test
    fun `buildServerUrl includes port 0 as colon-zero`() {
        val server = CSServerModel(
            id = UUID.randomUUID(), name = "Test", http = "http",
            domain = "example.com", port = 0, path = null,
            authMethod = "none", basicUser = null, basicPassword = null, bearerToken = null
        )
        // ponytail: actual behavior — port=0 is rendered as ":0", not omitted
        assertEquals("http://example.com:0", buildServerUrl(server))
    }

    @Test
    fun `buildServerUrl includes port when specified`() {
        val server = CSServerModel(
            id = UUID.randomUUID(), name = "Test", http = "http",
            domain = "example.com", port = 8080, path = "",
            authMethod = "none", basicUser = null, basicPassword = null, bearerToken = null
        )
        assertEquals("http://example.com:8080", buildServerUrl(server))
    }

    @Test
    fun `buildServerUrl includes path when specified`() {
        val server = CSServerModel(
            id = UUID.randomUUID(), name = "Test", http = "http",
            domain = "example.com", port = 8080, path = "/api/v1",
            authMethod = "none", basicUser = null, basicPassword = null, bearerToken = null
        )
        assertEquals("http://example.com:8080/api/v1", buildServerUrl(server))
    }

    @Test
    fun `buildServerUrl uses https scheme`() {
        val server = CSServerModel(
            id = UUID.randomUUID(), name = "Test", http = "https",
            domain = "example.com", port = null, path = null,
            authMethod = "none", basicUser = null, basicPassword = null, bearerToken = null
        )
        assertEquals("https://example.com", buildServerUrl(server))
    }

    @Test
    fun `buildServerUrl handles path without leading content`() {
        val server = CSServerModel(
            id = UUID.randomUUID(), name = "Test", http = "http",
            domain = "example.com", port = 443, path = "/",
            authMethod = "none", basicUser = null, basicPassword = null, bearerToken = null
        )
        assertEquals("http://example.com:443/", buildServerUrl(server))
    }
}
