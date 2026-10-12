package com.jgeek00.crowdsecmonitor.data.api

import com.jgeek00.crowdsecmonitor.data.models.CreateNotificationRequest
import com.jgeek00.crowdsecmonitor.data.models.HttpClientException
import com.jgeek00.crowdsecmonitor.data.models.NotificationConditionNode
import com.jgeek00.crowdsecmonitor.data.models.NotificationConditionValue
import com.jgeek00.crowdsecmonitor.data.models.NotificationThreshold
import com.jgeek00.crowdsecmonitor.data.models.ToggleNotificationRequest
import com.jgeek00.crowdsecmonitor.data.models.UpdateNotificationRequest
import com.jgeek00.crowdsecmonitor.fixtures.TestFixtures
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class NotificationsApiClientTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var client: NotificationsApiClient

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
        val server = TestFixtures.csserverModel(
            http = "http", domain = mockWebServer.hostName, port = mockWebServer.port, path = null,
            authMethod = "", basicUser = null, basicPassword = null, bearerToken = null, customHeaders = null
        )
        client = NotificationsApiClient(HttpClient(server))
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    private fun notificationDetailJson(): String {
        return """
            {
              "data": {
                "id": 1,
                "name": "ssh alerts",
                "enabled": true,
                "condition": {"type":"leaf","field":"scenario","operator":"equals","value":"crowdsec/ssh-bf"},
                "message": "Alert",
                "includeAlertInfo": false,
                "channel_ids": [1]
              }
            }
        """.trimIndent()
    }

    @Test
    fun `fetchNotifications returns success`() {
        runBlocking {
            mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("""{"data": []}"""))

            val result = client.fetchNotifications()

            assertTrue(result.successful)
            assertEquals(200, result.statusCode)
            assertEquals(0, result.body.data.size)
            assertEquals("/api/v1/notifications", mockWebServer.takeRequest().path)
        }
    }

    @Test
    fun `createNotification sends create body and returns detail`() {
        runBlocking {
            mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(notificationDetailJson()))

            val request = CreateNotificationRequest(
                name = "ssh alerts",
                description = null,
                enabled = true,
                condition = NotificationConditionNode.Leaf(
                    field = "scenario",
                    operator = "equals",
                    value = NotificationConditionValue.Single("crowdsec/ssh-bf")
                ),
                threshold = NotificationThreshold(count = 3, windowSeconds = 10),
                message = "Alert",
                includeAlertInfo = false,
                channelIds = listOf(1)
            )
            val result = client.createNotification(request)

            assertTrue(result.successful)
            assertEquals(1, result.body.data.id)

            val recorded = mockWebServer.takeRequest()
            assertEquals("/api/v1/notifications", recorded.path)
            assertEquals("POST", recorded.method)

            val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }
            val body = json.parseToJsonElement(recorded.body.readUtf8()).jsonObject
            assertEquals("ssh alerts", body["name"]?.jsonPrimitive?.content)
            assertEquals(true, body["enabled"]?.jsonPrimitive?.content.toBoolean())
            assertNull("description must be omitted, not null", body["description"])
            assertEquals(3, (body["threshold"] as? kotlinx.serialization.json.JsonObject)
                ?.get("count")?.jsonPrimitive?.content?.toInt())
        }
    }

    @Test
    fun `fetchNotification hits detail endpoint`() {
        runBlocking {
            mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(notificationDetailJson()))

            val result = client.fetchNotification(7)

            assertTrue(result.successful)
            assertEquals(1, result.body.data.id)
            assertEquals("/api/v1/notifications/7", mockWebServer.takeRequest().path)
        }
    }

    @Test
    fun `updateNotification sends partial omit-null body`() {
        runBlocking {
            mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(notificationDetailJson()))

            val result = client.updateNotification(
                7,
                UpdateNotificationRequest(enabled = false, message = "new message")
            )

            assertTrue(result.successful)

            val recorded = mockWebServer.takeRequest()
            assertEquals("/api/v1/notifications/7", recorded.path)
            assertEquals("PUT", recorded.method)

            val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }
            val body = json.parseToJsonElement(recorded.body.readUtf8()).jsonObject
            assertEquals(false, body["enabled"]?.jsonPrimitive?.content.toBoolean())
            assertEquals("new message", body["message"]?.jsonPrimitive?.content)
            assertNull("name must be omitted", body["name"])
            assertNull("condition must be omitted", body["condition"])
        }
    }

    @Test
    fun `deleteNotification hits delete endpoint and returns message`() {
        runBlocking {
            mockWebServer.enqueue(
                MockResponse().setResponseCode(200).setBody("""{"message": "deleted"}""")
            )

            val result = client.deleteNotification(7)

            assertTrue(result.successful)
            assertEquals("deleted", result.body.message)

            val recorded = mockWebServer.takeRequest()
            assertEquals("/api/v1/notifications/7", recorded.path)
            assertEquals("DELETE", recorded.method)
        }
    }

    @Test
    fun `toggleNotification posts enabled body`() {
        runBlocking {
            mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(notificationDetailJson()))

            val result = client.toggleNotification(7, ToggleNotificationRequest(enabled = true))

            assertTrue(result.successful)

            val recorded = mockWebServer.takeRequest()
            assertEquals("/api/v1/notifications/7/enabled", recorded.path)
            assertEquals("POST", recorded.method)
            assertEquals("""{"enabled":true}""", recorded.body.readUtf8())
        }
    }

    @Test
    fun `fetchHistory hits history endpoint`() {
        runBlocking {
            mockWebServer.enqueue(
                MockResponse().setResponseCode(200).setBody(
                    """
                    {
                      "data": [
                        {
                          "id": "h1",
                          "notification_id": 1,
                          "notification_name": "ssh alerts",
                          "message": "Alert fired",
                          "triggered_at": "2026-01-01T00:00:00Z",
                          "channels": [{"type": "ntfy", "ok": true}]
                        }
                      ],
                      "total": 1
                    }
                    """.trimIndent()
                )
            )

            val result = client.fetchHistory()

            assertTrue(result.successful)
            assertEquals(1, result.body.total)
            assertEquals("h1", result.body.data.first().id)
            assertEquals("/api/v1/notifications/history", mockWebServer.takeRequest().path)
        }
    }

    @Test
    fun `fetchNotifications throws Unauthorized on 401`() {
        assertThrows(HttpClientException.Unauthorized::class.java) {
            runBlocking {
                mockWebServer.enqueue(MockResponse().setResponseCode(401))
                client.fetchNotifications()
            }
        }
    }

    @Test
    fun `fetchNotification throws HttpError on 404`() {
        runBlocking {
            mockWebServer.enqueue(MockResponse().setResponseCode(404))
            try {
                client.fetchNotification(999)
            } catch (e: HttpClientException.HttpError) {
                assertEquals(404, e.statusCode)
            }
        }
    }
}
