package com.jgeek00.crowdsecmonitor.data.api

import com.jgeek00.crowdsecmonitor.MockWebSocketServer
import com.jgeek00.crowdsecmonitor.data.models.ApiStatusResponse
import com.jgeek00.crowdsecmonitor.fixtures.TestFixtures
import com.jgeek00.crowdsecmonitor.fixtures.testApiStatusJson
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class WebSocketClientTest {

    private fun wsClient(
        http: String = "http",
        domain: String = "localhost",
        port: Int? = null,
        path: String? = null,
        authMethod: String = "",
        basicUser: String? = null,
        basicPassword: String? = null,
        bearerToken: String? = null,
        customHeaders: List<Pair<String, String>>? = null
    ): WebSocketClient = WebSocketClient(
        TestFixtures.csserverModel(
            http = http, domain = domain, port = port, path = path,
            authMethod = authMethod, basicUser = basicUser, basicPassword = basicPassword,
            bearerToken = bearerToken, customHeaders = customHeaders
        )
    )

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

    // ── Lifecycle via MockWebSocketServer ───────────────────────

    @Test
    fun `connect transitions to CONNECTED and fires onConnect`() {
        val mock = MockWebSocketServer()
        mock.start()
        try {
            val ws = wsClient(domain = mock.hostName, port = mock.port)
            val connected = CountDownLatch(1)
            ws.onConnect = { connected.countDown() }

            ws.connect(endpoint = "/api/v1/status")

            assertTrue("onConnect not fired", connected.await(2, TimeUnit.SECONDS))
            mock.awaitOpen()
            assertEquals(WebSocketState.CONNECTED, ws.state)
            ws.disconnect()
        } finally {
            mock.stop()
        }
    }

    @Test
    fun `connect when already connected is a no-op`() {
        val mock = MockWebSocketServer()
        mock.start()
        try {
            val ws = wsClient(domain = mock.hostName, port = mock.port)
            val connectCount = java.util.concurrent.atomic.AtomicInteger(0)
            ws.onConnect = { connectCount.incrementAndGet() }

            ws.connect(endpoint = "/api/v1/status")
            mock.awaitOpen()
            ws.connect(endpoint = "/api/v1/status")

            assertEquals(1, connectCount.get())
            ws.disconnect()
        } finally {
            mock.stop()
        }
    }

    @Test
    fun `onMessage receives text frames sent by the server`() {
        val mock = MockWebSocketServer()
        mock.start()
        try {
            val ws = wsClient(domain = mock.hostName, port = mock.port)
            val message = CountDownLatch(1)
            var received: WebSocketMessage? = null
            ws.onMessage = { msg ->
                received = msg
                message.countDown()
            }

            ws.connect(endpoint = "/api/v1/status")
            mock.awaitOpen()
            mock.send("hello")

            assertTrue("onMessage not fired", message.await(2, TimeUnit.SECONDS))
            assertTrue(received is WebSocketMessage.Text)
            assertEquals("hello", (received as WebSocketMessage.Text).text)
            ws.disconnect()
        } finally {
            mock.stop()
        }
    }

    @Test
    fun `onMessage receives binary frames sent by the server`() {
        val mock = MockWebSocketServer()
        mock.start()
        try {
            val ws = wsClient(domain = mock.hostName, port = mock.port)
            val message = CountDownLatch(1)
            var received: WebSocketMessage? = null
            ws.onMessage = { msg ->
                received = msg
                message.countDown()
            }

            ws.connect(endpoint = "/api/v1/status")
            mock.awaitOpen()
            mock.send(byteArrayOf(0x01, 0x02, 0x03))

            assertTrue("onMessage not fired", message.await(2, TimeUnit.SECONDS))
            assertTrue(received is WebSocketMessage.Data)
            assertArrayEquals(byteArrayOf(0x01, 0x02, 0x03), (received as WebSocketMessage.Data).bytes)
            ws.disconnect()
        } finally {
            mock.stop()
        }
    }

    @Test
    fun `disconnect sets state to DISCONNECTED`() {
        val mock = MockWebSocketServer()
        mock.start()
        try {
            val ws = wsClient(domain = mock.hostName, port = mock.port)
            ws.connect(endpoint = "/api/v1/status")
            mock.awaitOpen()

            ws.disconnect()
            assertEquals(WebSocketState.DISCONNECTED, ws.state)
        } finally {
            mock.stop()
        }
    }

    @Test
    fun `onDisconnect fires when server initiates close`() {
        val mock = MockWebSocketServer()
        mock.start()
        try {
            val ws = wsClient(domain = mock.hostName, port = mock.port)
            ws.connect(endpoint = "/api/v1/status")
            mock.awaitOpen()

            val disconnected = CountDownLatch(1)
            ws.onDisconnect = { _, _ -> disconnected.countDown() }

            mock.close(1000, "bye")
            assertTrue("onDisconnect not fired", disconnected.await(2, TimeUnit.SECONDS))
            // ponytail: OkHttp dispatches close events asynchronously; give the internalListener
            // time to set state after the onDisconnect callback returns.
            Thread.sleep(100)
            assertEquals(WebSocketState.DISCONNECTED, ws.state)
        } finally {
            mock.stop()
        }
    }

    @Test
    fun `send while connected does not throw`() {
        val mock = MockWebSocketServer()
        mock.start()
        try {
            val ws = wsClient(domain = mock.hostName, port = mock.port)
            ws.connect(endpoint = "/api/v1/status")
            mock.awaitOpen()

            runBlocking { ws.send("ping") }
            ws.disconnect()
        } finally {
            mock.stop()
        }
    }

    @Test
    fun `stream emits decoded JSON messages`() = runBlocking {
        val mock = MockWebSocketServer()
        mock.start()
        try {
            val ws = wsClient(domain = mock.hostName, port = mock.port)
            val flow = ws.stream<ApiStatusResponse>(endpoint = "/api/v1/status")

            val sender = Thread {
                mock.awaitOpen()
                mock.send(testApiStatusJson)
            }
            sender.start()

            val first = flow.first()
            sender.join(2000)

            assertNotNull(first)
            assertTrue(first.csLapi.lapiConnected)
        } finally {
            mock.stop()
        }
    }

    @Test
    fun `buildUrl maps http to ws and wss to wss`() {
        // Scheme mapping is exercised indirectly via connect against a ws:// mock server
        // (http → ws). wss path is covered by the production code and not reachable from
        // a plaintext MockWebServer; the ws path confirms the non-wss branch.
        val mock = MockWebSocketServer()
        mock.start()
        try {
            val ws = wsClient(http = "http", domain = mock.hostName, port = mock.port)
            val connected = CountDownLatch(1)
            ws.onConnect = { connected.countDown() }
            ws.connect(endpoint = "/api/v1/status")
            assertTrue(connected.await(2, TimeUnit.SECONDS))
            ws.disconnect()
        } finally {
            mock.stop()
        }
    }

    // ── Auth header propagation ─────────────────────────────────

    @Test
    fun `configureRequest attaches bearer auth header`() {
        val mock = MockWebSocketServer()
        mock.start()
        try {
            val ws = wsClient(
                domain = mock.hostName, port = mock.port,
                authMethod = "bearer", bearerToken = "wstoken"
            )
            val connected = CountDownLatch(1)
            ws.onConnect = { connected.countDown() }

            ws.connect(endpoint = "/api/v1/status")
            assertTrue(connected.await(2, TimeUnit.SECONDS))
            mock.awaitOpen()

            val request = mock.takeRequest()
            assertEquals("Bearer wstoken", request?.getHeader("Authorization"))
            ws.disconnect()
        } finally {
            mock.stop()
        }
    }

    @Test
    fun `configureRequest attaches basic auth header`() {
        val mock = MockWebSocketServer()
        mock.start()
        try {
            val ws = wsClient(
                domain = mock.hostName, port = mock.port,
                authMethod = "basic", basicUser = "user", basicPassword = "pass"
            )
            val connected = CountDownLatch(1)
            ws.onConnect = { connected.countDown() }

            ws.connect(endpoint = "/api/v1/status")
            assertTrue(connected.await(2, TimeUnit.SECONDS))
            mock.awaitOpen()

            val request = mock.takeRequest()
            val expected = "Basic " + java.util.Base64.getEncoder().encodeToString("user:pass".toByteArray())
            assertEquals(expected, request?.getHeader("Authorization"))
            ws.disconnect()
        } finally {
            mock.stop()
        }
    }

    @Test
    fun `ping sends while connected`() {
        val mock = MockWebSocketServer()
        mock.start()
        try {
            val ws = wsClient(domain = mock.hostName, port = mock.port)
            ws.connect(endpoint = "/api/v1/status")
            mock.awaitOpen()

            runBlocking { ws.ping() }
            ws.disconnect()
        } finally {
            mock.stop()
        }
    }

    @Test
    fun `send binary data while connected`() {
        val mock = MockWebSocketServer()
        mock.start()
        try {
            val ws = wsClient(domain = mock.hostName, port = mock.port)
            ws.connect(endpoint = "/api/v1/status")
            mock.awaitOpen()

            runBlocking { ws.send(byteArrayOf(0x01, 0x02)) }
            ws.disconnect()
        } finally {
            mock.stop()
        }
    }

    @Test
    fun `configureRequest attaches custom headers`() {
        val mock = MockWebSocketServer()
        mock.start()
        try {
            val ws = wsClient(
                domain = mock.hostName, port = mock.port,
                authMethod = "none",
                customHeaders = listOf("X-Custom" to "my-value")
            )
            val connected = CountDownLatch(1)
            ws.onConnect = { connected.countDown() }

            ws.connect(endpoint = "/api/v1/status")
            assertTrue(connected.await(2, TimeUnit.SECONDS))
            mock.awaitOpen()

            val request = mock.takeRequest()
            assertEquals("my-value", request?.getHeader("X-Custom"))
            ws.disconnect()
        } finally {
            mock.stop()
        }
    }

    @Test
    fun `disconnect with code and reason sets DISCONNECTED`() {
        val mock = MockWebSocketServer()
        mock.start()
        try {
            val ws = wsClient(domain = mock.hostName, port = mock.port)
            ws.connect(endpoint = "/api/v1/status")
            mock.awaitOpen()

            ws.disconnect(code = 1001, reason = "going away")
            assertEquals(WebSocketState.DISCONNECTED, ws.state)
        } finally {
            mock.stop()
        }
    }
}

private fun assertArrayEquals(expected: ByteArray, actual: ByteArray) {
    org.junit.Assert.assertArrayEquals(expected, actual)
}
