package com.jgeek00.crowdsecmonitor

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

/**
 * In-process WebSocket server backed by OkHttp [MockWebServer].
 *
 * Binds to an ephemeral loopback port so tests can drive a real [WebSocketClient]
 * lifecycle (connect / send / receive / disconnect / stream) without any external
 * network. Each [enqueueUpgrade] serves one incoming client connection; call it again
 * for tests that open more than one WebSocket against the same server.
 */
class MockWebSocketServer {

    private val server = MockWebServer()
    private val openSignal = LinkedBlockingQueue<Unit>(1)

    @Volatile
    private var serverSocket: WebSocket? = null

    val hostName: String get() = server.hostName
    val port: Int get() = server.port

    fun start() {
        server.start()
        enqueueUpgrade()
    }

    /** Queue a WebSocket handshake response for the next incoming connection. */
    fun enqueueUpgrade() {
        server.enqueue(
            MockResponse().withWebSocketUpgrade(
                object : WebSocketListener() {
                    override fun onOpen(webSocket: WebSocket, response: Response) {
                        serverSocket = webSocket
                        openSignal.offer(Unit)
                    }
                }
            )
        )
    }

    /** Block until a client has completed the WebSocket handshake with this server. */
    fun awaitOpen(timeoutMs: Long = 2000L) {
        if (openSignal.poll(timeoutMs, TimeUnit.MILLISECONDS) == null) {
            error("WebSocket server did not accept a client within ${timeoutMs}ms")
        }
    }

    fun send(text: String) {
        serverSocket?.send(text)
    }

    fun send(bytes: ByteArray) {
        serverSocket?.send(ByteString.of(*bytes))
    }

    fun close(code: Int = 1000, reason: String? = null) {
        serverSocket?.close(code, reason)
    }

    /** Pop the last recorded handshake request (e.g. to assert auth headers). Blocks up to [timeoutMs]. */
    fun takeRequest(timeoutMs: Long = 2000L): RecordedRequest? =
        server.takeRequest(timeoutMs, TimeUnit.MILLISECONDS)

    fun stop() {
        runCatching { serverSocket?.close(1001, "stop") }
        serverSocket = null
        openSignal.clear()
        // ponytail: MockWebServer.shutdown() can throw "Gave up waiting for queue to
        // shut down" when a WebSocket is still mid-close; the test body already
        // passed, so swallow that cleanup-only AssertionError. Other failures propagate.
        try {
            server.shutdown()
        } catch (e: AssertionError) {
            if (!e.message.isNullOrEmpty() && e.message!!.contains("shut down")) {
                return
            }
            throw e
        }
    }
}
