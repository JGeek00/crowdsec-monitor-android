package com.jgeek00.crowdsecmonitor.data.api

import android.util.Base64
import com.jgeek00.crowdsecmonitor.data.db.CSServerModel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import okhttp3.*
import okio.ByteString
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

sealed class WebSocketMessage {
    data class Text(val text: String) : WebSocketMessage()
    data class Data(val bytes: ByteArray) : WebSocketMessage()
}

enum class WebSocketState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED
}

sealed class WebSocketClientError(message: String) : Exception(message) {
    class NotConnected : WebSocketClientError("WebSocket is not connected")
    class AlreadyConnected : WebSocketClientError("WebSocket is already connected")
    class EncodingError : WebSocketClientError("Failed to encode message as JSON")
    class DecodingError : WebSocketClientError("Failed to decode WebSocket message")
}

class WebSocketClient(private val server: CSServerModel) {

    private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }

    private val client: OkHttpClient by lazy {
        getUnsafeOkHttpClient()
            // send websocket pings automatically every 30s to detect silent drops
            .pingInterval(30, TimeUnit.SECONDS)
            .build()
    }

    private var webSocket: WebSocket? = null

    @Volatile
    var state: WebSocketState = WebSocketState.DISCONNECTED
        private set

    var onMessage: ((WebSocketMessage) -> Unit)? = null
    var onConnect: (() -> Unit)? = null
    var onDisconnect: ((code: Int, reason: String?) -> Unit)? = null
    var onError: ((Throwable) -> Unit)? = null

    private fun buildUrl(): String {
        val portStr = if ((server.port ?: 0) > 0) ":${server.port}" else ""
        val pathStr = server.path?.let { if (it.startsWith("/")) it else "/$it" } ?: ""

        val scheme = when (server.http.lowercase()) {
            "https", "wss" -> "wss"
            else -> "ws"
        }

        return "$scheme://${server.domain}$portStr$pathStr"
    }

    private fun configureRequest(endpoint: String): Request {
        val url = if (endpoint.startsWith("/")) endpoint else "/$endpoint"
        val full = buildUrl().trimEnd('/') + url
        val builder = Request.Builder().url(full)

        when (server.authMethod) {
            "basic" -> {
                val credentials = "${server.basicUser}:${server.basicPassword}"
                val base64 = Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)
                builder.addHeader("Authorization", "Basic $base64")
            }
            "bearer" -> {
                server.bearerToken?.let { builder.addHeader("Authorization", "Bearer $it") }
            }
        }

        server.customHeaders?.forEach { (key, value) ->
            builder.addHeader(key, value)
        }

        return builder.build()
    }

    // Public connect (single websocket instance)
    fun connect(endpoint: String) {
        if (state != WebSocketState.DISCONNECTED) return
        state = WebSocketState.CONNECTING

        val request = configureRequest(endpoint)
        webSocket = client.newWebSocket(request, internalListener())
        // OkHttp establishes connection asynchronously; consider it connected after created
        state = WebSocketState.CONNECTED
        onConnect?.invoke()
    }

    fun disconnect(code: Int = 1000, reason: String? = null) {
        if (state == WebSocketState.DISCONNECTED) return
        webSocket?.close(code, reason)
        webSocket = null
        state = WebSocketState.DISCONNECTED
    }

    suspend fun send(text: String) {
        if (state != WebSocketState.CONNECTED || webSocket == null) throw WebSocketClientError.NotConnected()
        val ok = webSocket!!.send(text)
        if (!ok) throw WebSocketClientError.NotConnected()
    }

    suspend fun send(data: ByteArray) {
        if (state != WebSocketState.CONNECTED || webSocket == null) throw WebSocketClientError.NotConnected()
        val ok = webSocket!!.send(ByteString.of(*data))
        if (!ok) throw WebSocketClientError.NotConnected()
    }

    suspend fun <T> sendEncodable(value: T) {
        try {
            val text = json.encodeToString(value as Any)
            send(text)
        } catch (e: Exception) {
            throw WebSocketClientError.EncodingError()
        }
    }

    // Application-level ping (sends a small JSON). OkHttp's ping control frames are handled
    // automatically by pingInterval on the client.
    suspend fun ping() {
        if (state != WebSocketState.CONNECTED || webSocket == null) throw WebSocketClientError.NotConnected()
        // send a lightweight application ping
        val pingPayload = "{\"type\":\"ping\"}"
        webSocket!!.send(pingPayload)
    }

    // Stream incoming JSON messages decoded as T. Creates an independent websocket per stream.
    internal inline fun <reified T> stream(endpoint: String): Flow<T> {
        return callbackFlow {
            val request = configureRequest(endpoint)
            val listener = object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    // nothing
                }

                override fun onMessage(webSocket: WebSocket, text: String) {
                    try {
                        val decoded = json.decodeFromString<T>(text)
                        trySend(decoded).isSuccess
                    } catch (e: Exception) {
                        close(e)
                    }
                }

                override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                    try {
                        val decoded = json.decodeFromString<T>(bytes.utf8())
                        trySend(decoded).isSuccess
                    } catch (e: Exception) {
                        close(e)
                    }
                }

                override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                    webSocket.close(code, reason)
                    close()
                }

                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                    close()
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    close(t)
                }
            }

            val ws = client.newWebSocket(request, listener)

            awaitClose {
                ws.close(1000, "stream closed")
            }
        }
    }

    private fun internalListener(): WebSocketListener = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            // already handled in connect
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            onMessage?.invoke(WebSocketMessage.Text(text))
        }

        override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
            onMessage?.invoke(WebSocketMessage.Data(bytes.toByteArray()))
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            onDisconnect?.invoke(code, reason)
            webSocket.close(code, reason)
            state = WebSocketState.DISCONNECTED
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            onDisconnect?.invoke(code, reason)
            state = WebSocketState.DISCONNECTED
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            onError?.invoke(t)
            onDisconnect?.invoke(1006, t.localizedMessage)
            state = WebSocketState.DISCONNECTED
        }
    }

    // Don't validate SSL certificates (same behaviour as HttpClient)
    private fun getUnsafeOkHttpClient(): OkHttpClient.Builder {
        try {
            val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
            })

            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, SecureRandom())

            val builder = OkHttpClient.Builder()
            builder.sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
            builder.hostnameVerifier { _, _ -> true }

            return builder
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }
}


