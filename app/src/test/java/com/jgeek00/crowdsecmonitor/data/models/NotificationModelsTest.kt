package com.jgeek00.crowdsecmonitor.data.models

import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationModelsTest {

    // Mirrors the shared Json configuration used by HttpClient for the wire.
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    // MARK: - Condition DSL round-trip

    @Test
    fun `leaf condition with single value encodes value as bare string`() {
        val condition: NotificationConditionNode = NotificationConditionNode.Leaf(
            field = "scenario",
            operator = "equals",
            value = NotificationConditionValue.Single("crowdsec/ssh-bf")
        )

        val encoded = json.encodeToString(condition)

        assertEquals(
            "{\"type\":\"leaf\",\"field\":\"scenario\",\"operator\":\"equals\",\"value\":\"crowdsec/ssh-bf\"}",
            encoded
        )
    }

    @Test
    fun `leaf condition with multiple values encodes value as array`() {
        val condition: NotificationConditionNode = NotificationConditionNode.Leaf(
            field = "country",
            operator = "in",
            value = NotificationConditionValue.Multiple(listOf("US", "DE"))
        )

        val encoded = json.encodeToString(condition)

        assertEquals(
            "{\"type\":\"leaf\",\"field\":\"country\",\"operator\":\"in\",\"value\":[\"US\",\"DE\"]}",
            encoded
        )
    }

    @Test
    fun `and or not nesting round-trips`() {
        val condition: NotificationConditionNode = NotificationConditionNode.And(
            listOf(
                NotificationConditionNode.Leaf(
                    field = "scenario",
                    operator = "equals",
                    value = NotificationConditionValue.Single("crowdsec/ssh-bf")
                ),
                NotificationConditionNode.Or(
                    listOf(
                        NotificationConditionNode.Leaf(
                            field = "country",
                            operator = "in",
                            value = NotificationConditionValue.Multiple(listOf("US", "DE"))
                        ),
                        NotificationConditionNode.Not(
                            NotificationConditionNode.Leaf(
                                field = "ipOwner",
                                operator = "contains",
                                value = NotificationConditionValue.Single("Google")
                            )
                        )
                    )
                )
            )
        )

        val encoded = json.encodeToString(condition)
        val decoded = json.decodeFromString<NotificationConditionNode>(encoded)

        assertEquals(condition, decoded)
    }

    @Test
    fun `leaf single value decodes from bare string wire format`() {
        val wire = "{\"type\":\"leaf\",\"field\":\"scenario\",\"operator\":\"equals\",\"value\":\"crowdsec/ssh-bf\"}"

        val decoded = json.decodeFromString<NotificationConditionNode>(wire)

        val leaf = decoded as NotificationConditionNode.Leaf
        assertEquals("scenario", leaf.field)
        assertEquals("equals", leaf.operator)
        assertEquals(NotificationConditionValue.Single("crowdsec/ssh-bf"), leaf.value)
    }

    @Test
    fun `unknown condition type throws like iOS`() {
        val wire = "{\"type\":\"xor\",\"children\":[]}"

        assertThrows(SerializationException::class.java) {
            json.decodeFromString<NotificationConditionNode>(wire)
        }
    }

    @Test
    fun `condition value asArray unwraps both variants`() {
        val single = NotificationConditionValue.Single("a")
        val multiple = NotificationConditionValue.Multiple(listOf("a", "b"))

        assertEquals(listOf("a"), single.asArray)
        assertEquals(listOf("a", "b"), multiple.asArray)
    }

    // MARK: - UserNotification decoding

    @Test
    fun `user notification decodes snake_case wire fields`() {
        val wire = """
            {
              "id": 1,
              "name": "ssh alerts",
              "enabled": true,
              "condition": {"type":"leaf","field":"scenario","operator":"equals","value":"crowdsec/ssh-bf"},
              "message": "Alert",
              "includeAlertInfo": false,
              "channel_ids": [1, 2],
              "created_at": "2026-01-01T00:00:00Z",
              "updated_at": "2026-01-02T00:00:00Z"
            }
        """.trimIndent()

        val notification = json.decodeFromString<UserNotification>(wire)

        assertEquals(1, notification.id)
        assertEquals("ssh alerts", notification.name)
        assertNull(notification.description)
        assertTrue(notification.enabled)
        assertNull(notification.threshold)
        assertEquals(listOf(1, 2), notification.channelIds)
        assertEquals("2026-01-01T00:00:00Z", notification.createdAt)
        assertEquals("2026-01-02T00:00:00Z", notification.updatedAt)
    }

    @Test
    fun `user notification decodes threshold with optional cooldown`() {
        val wire = """
            {
              "id": 1,
              "name": "n",
              "enabled": false,
              "condition": {"type":"and","children":[]},
              "threshold": {"count": 3, "window_seconds": 10, "cooldown_seconds": 60},
              "message": "m",
              "includeAlertInfo": true,
              "channel_ids": []
            }
        """.trimIndent()

        val notification = json.decodeFromString<UserNotification>(wire)

        val threshold = notification.threshold
        assertEquals(3, threshold?.count)
        assertEquals(10, threshold?.windowSeconds)
        assertEquals(60, threshold?.cooldownSeconds)
        assertTrue(notification.includeAlertInfo)
    }

    // MARK: - History

    @Test
    fun `history response decodes snake_case wire fields`() {
        val wire = """
            {
              "data": [
                {
                  "id": "h1",
                  "notification_id": 1,
                  "notification_name": "ssh alerts",
                  "message": "Alert fired",
                  "triggered_at": "2026-01-01T00:00:00Z",
                  "channels": [
                    {"type": "ntfy", "ok": true, "detail": "sent"},
                    {"type": "email", "ok": false}
                  ]
                }
              ],
              "total": 1
            }
        """.trimIndent()

        val response = json.decodeFromString<NotificationHistoryResponse>(wire)

        assertEquals(1, response.total)
        val entry = response.data.first()
        assertEquals("h1", entry.id)
        assertEquals(1, entry.notificationId)
        assertEquals("ssh alerts", entry.notificationName)
        assertEquals(2, entry.channels.size)
        assertTrue(entry.channels.first().ok)
        assertNull(entry.channels.last().detail)
    }

    // MARK: - Omit-vs-null request bodies

    @Test
    fun `create request omits unset optional fields`() {
        val request = CreateNotificationRequest(
            name = "ssh alerts",
            description = null,
            enabled = true,
            condition = NotificationConditionNode.And(emptyList()),
            threshold = null,
            message = "Alert",
            includeAlertInfo = false,
            channelIds = listOf(1)
        )

        val encoded = json.encodeToString(request)
        val obj = json.parseToJsonElement(encoded).jsonObject

        assertEquals("ssh alerts", obj["name"]?.jsonPrimitive?.content)
        assertEquals(true, obj["enabled"]?.jsonPrimitive?.content.toBoolean())
        assertNull("description must be omitted, not null", obj["description"])
        assertNull("threshold must be omitted, not null", obj["threshold"])
        assertEquals(1, obj["channel_ids"]?.jsonArray?.size)
    }

    @Test
    fun `create request threshold omits cooldown when unset`() {
        val request = CreateNotificationRequest(
            name = "n",
            enabled = true,
            condition = NotificationConditionNode.And(emptyList()),
            threshold = NotificationThreshold(count = 3, windowSeconds = 10),
            message = "m",
            includeAlertInfo = false,
            channelIds = emptyList()
        )

        val encoded = json.encodeToString(request)
        val obj = json.parseToJsonElement(encoded).jsonObject
        val threshold = obj["threshold"] as? JsonObject

        assertEquals(3, threshold?.get("count")?.jsonPrimitive?.content?.toInt())
        assertEquals(10, threshold?.get("window_seconds")?.jsonPrimitive?.content?.toInt())
        assertNull("cooldown_seconds must be omitted, not null", threshold?.get("cooldown_seconds"))
    }

    @Test
    fun `update request with all nulls encodes empty object`() {
        val request = UpdateNotificationRequest()

        val encoded = json.encodeToString(request)

        assertEquals("{}", encoded)
    }

    @Test
    fun `update request sends only provided fields`() {
        val request = UpdateNotificationRequest(
            enabled = false,
            message = "new message"
        )

        val encoded = json.encodeToString(request)
        val obj = json.parseToJsonElement(encoded).jsonObject

        assertEquals(false, obj["enabled"]?.jsonPrimitive?.content.toBoolean())
        assertEquals("new message", obj["message"]?.jsonPrimitive?.content)
        assertNull("name must be omitted", obj["name"])
        assertNull("condition must be omitted", obj["condition"])
        assertNull("channel_ids must be omitted", obj["channel_ids"])
    }

    @Test
    fun `toggle request always sends enabled`() {
        val encoded = json.encodeToString(ToggleNotificationRequest(enabled = true))

        assertEquals("{\"enabled\":true}", encoded)
    }
}
