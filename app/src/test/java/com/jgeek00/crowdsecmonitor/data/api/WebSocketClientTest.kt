package com.jgeek00.crowdsecmonitor.data.api

import com.jgeek00.crowdsecmonitor.fixtures.TestFixtures
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith

class WebSocketClientTest {

    private fun wsClient(
        http: String = "http",
        domain: String = "localhost",
        port: Int? = null,
        path: String? = null,
        authMethod: String = "",
        basicUser: String? = null,
        basicPassword: String? = null,
        bearerToken: String? = null
    ): WebSocketClient = WebSocketClient(TestFixtures.csserverModel(
        http = http, domain = domain, port = port, path = path,
        authMethod = authMethod, basicUser = basicUser, basicPassword = basicPassword,
        bearerToken = bearerToken, customHeaders = null
    ))

    @Test
    fun `initial state is DISCONNECTED`() {
        val ws = wsClient()
        assertEquals(WebSocketState.DISCONNECTED, ws.state)
    }

    @Test
    fun `send throws NotConnected when not connected`() {
        val ws = wsClient()
        assertThrows(WebSocketClientError.NotConnected::class.java) {
            runBlocking { ws.send("hello") }
        }
    }

    @Test
    fun `sendEncodable throws EncodingError when type has no serializer`() {
        val ws = wsClient()
        assertThrows(WebSocketClientError.EncodingError::class.java) {
            runBlocking { ws.sendEncodable(mapOf("key" to "value")) }
        }
    }

    @Test
    fun `ping throws NotConnected when not connected`() {
        val ws = wsClient()
        assertThrows(WebSocketClientError.NotConnected::class.java) {
            runBlocking { ws.ping() }
        }
    }

    @Test
    fun `disconnect when disconnected is no-op`() {
        val ws = wsClient()
        ws.disconnect()
        assertEquals(WebSocketState.DISCONNECTED, ws.state)
    }
}
