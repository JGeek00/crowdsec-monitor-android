package com.jgeek00.crowdsecmonitor.data.models

import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

class NotificationChannelModelsTest {

    // Mirrors the shared Json configuration used by HttpClient for the wire.
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    // MARK: - Channel decoding

    @Test
    fun `channel decodes snake_case wire fields and closed type enum`() {
        val wire = """
            {
              "id": 1,
              "name": "my ntfy",
              "type": "ntfy",
              "supportsAlertInfo": true,
              "config": {"url": "https://ntfy.sh/mytopic", "priority": 5},
              "created_at": "2026-01-01T00:00:00Z",
              "updated_at": "2026-01-02T00:00:00Z"
            }
        """.trimIndent()

        val channel = json.decodeFromString<UserNotificationChannel>(wire)

        assertEquals(1, channel.id)
        assertEquals("my ntfy", channel.name)
        assertEquals(NotificationChannelType.NTFY, channel.type)
        assertEquals(true, channel.supportsAlertInfo)
        assertEquals("https://ntfy.sh/mytopic", (channel.config["url"] as JsonPrimitive).content)
        assertEquals("2026-01-01T00:00:00Z", channel.createdAt)
        assertEquals("2026-01-02T00:00:00Z", channel.updatedAt)
    }

    @Test
    fun `unknown channel type throws like iOS closed enum`() {
        val wire = """
            {
              "id": 1,
              "name": "x",
              "type": "discord",
              "supportsAlertInfo": false,
              "config": {}
            }
        """.trimIndent()

        assertThrows(SerializationException::class.java) {
            json.decodeFromString<UserNotificationChannel>(wire)
        }
    }

    @Test
    fun `unknown config keys survive round-trip`() {
        val config = buildJsonObject {
            put("url", JsonPrimitive("https://ntfy.sh/mytopic"))
            put("priority", JsonPrimitive(5))
            put("futureField", buildJsonObject { put("nested", JsonPrimitive(true)) })
        }
        val channel = UserNotificationChannel(
            id = 1,
            name = "my ntfy",
            type = NotificationChannelType.NTFY,
            supportsAlertInfo = false,
            config = json.decodeFromString<Map<String, kotlinx.serialization.json.JsonElement>>(config.toString())
        )

        val encoded = json.encodeToString(channel)
        val decoded = json.decodeFromString<UserNotificationChannel>(encoded)

        assertEquals(channel, decoded)
        assertEquals(
            "{\"url\":\"https://ntfy.sh/mytopic\",\"priority\":5,\"futureField\":{\"nested\":true}}",
            json.encodeToString(decoded.config)
        )
    }

    // MARK: - Responses

    @Test
    fun `channel test response decodes`() {
        val wire = """
            {"data": {"channelId": 3, "ok": false, "detail": "connection refused"}}
        """.trimIndent()

        val response = json.decodeFromString<ChannelTestResponse>(wire)

        assertEquals(3, response.data.channelId)
        assertEquals(false, response.data.ok)
        assertEquals("connection refused", response.data.detail)
    }

    @Test
    fun `channel test response decodes inline test without channelId`() {
        val wire = """{"data": {"ok": true}}"""

        val response = json.decodeFromString<ChannelTestResponse>(wire)

        assertNull(response.data.channelId)
        assertEquals(true, response.data.ok)
        assertNull(response.data.detail)
    }

    // MARK: - Omit-vs-null request bodies

    @Test
    fun `create channel request sends full config`() {
        val request = CreateChannelRequest(
            name = "my ntfy",
            type = "ntfy",
            config = buildJsonObject { put("url", JsonPrimitive("https://ntfy.sh/t")) }
        )

        val encoded = json.encodeToString(request)
        val obj = json.parseToJsonElement(encoded).jsonObject

        assertEquals("my ntfy", obj["name"]?.jsonPrimitive?.content)
        assertEquals("ntfy", obj["type"]?.jsonPrimitive?.content)
        assertEquals(
            "https://ntfy.sh/t",
            (obj["config"] as? kotlinx.serialization.json.JsonObject)?.get("url")?.jsonPrimitive?.content
        )
    }

    @Test
    fun `update channel request with all nulls encodes empty object`() {
        val request = UpdateChannelRequest()

        val encoded = json.encodeToString(request)

        assertEquals("{}", encoded)
    }

    @Test
    fun `update channel request sends only provided fields`() {
        val request = UpdateChannelRequest(
            name = "renamed",
            config = buildJsonObject { put("url", JsonPrimitive("https://ntfy.sh/other")) }
        )

        val encoded = json.encodeToString(request)
        val obj = json.parseToJsonElement(encoded).jsonObject

        assertEquals("renamed", obj["name"]?.jsonPrimitive?.content)
        assertNull("type must be omitted", obj["type"])
        assertEquals(1, (obj["config"] as? kotlinx.serialization.json.JsonObject)?.size)
    }

    @Test
    fun `test channel request omits message when null`() {
        val encoded = json.encodeToString(TestChannelRequest(message = null))

        assertEquals("{}", encoded)
    }

    @Test
    fun `test channel request sends message when present`() {
        val encoded = json.encodeToString(TestChannelRequest(message = "hello"))

        assertEquals("{\"message\":\"hello\"}", encoded)
    }

    @Test
    fun `test inline channel request omits message when null`() {
        val request = TestInlineChannelRequest(
            type = "ntfy",
            config = buildJsonObject { put("url", JsonPrimitive("https://ntfy.sh/t")) },
            message = null
        )

        val encoded = json.encodeToString(request)
        val obj = json.parseToJsonElement(encoded).jsonObject

        assertEquals("ntfy", obj["type"]?.jsonPrimitive?.content)
        assertNull("message must be omitted, not null", obj["message"])
    }
}
