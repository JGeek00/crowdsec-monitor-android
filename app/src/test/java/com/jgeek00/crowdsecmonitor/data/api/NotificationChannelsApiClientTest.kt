package com.jgeek00.crowdsecmonitor.data.api

import com.jgeek00.crowdsecmonitor.data.models.CreateChannelRequest
import com.jgeek00.crowdsecmonitor.data.models.HttpClientException
import com.jgeek00.crowdsecmonitor.data.models.NotificationChannelType
import com.jgeek00.crowdsecmonitor.data.models.TestChannelRequest
import com.jgeek00.crowdsecmonitor.data.models.TestInlineChannelRequest
import com.jgeek00.crowdsecmonitor.data.models.UpdateChannelRequest
import com.jgeek00.crowdsecmonitor.fixtures.TestFixtures
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
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

class NotificationChannelsApiClientTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var client: NotificationChannelsApiClient

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
        val server = TestFixtures.csserverModel(
            http = "http", domain = mockWebServer.hostName, port = mockWebServer.port, path = null,
            authMethod = "", basicUser = null, basicPassword = null, bearerToken = null, customHeaders = null
        )
        client = NotificationChannelsApiClient(HttpClient(server))
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    private fun channelDetailJson(): String {
        return """
            {
              "data": {
                "id": 1,
                "name": "my ntfy",
                "type": "ntfy",
                "supportsAlertInfo": true,
                "config": {"url": "https://ntfy.sh/mytopic"}
              }
            }
        """.trimIndent()
    }

    private fun testResponseJson(): String {
        return """{"data": {"channelId": 1, "ok": true}}"""
    }

    @Test
    fun `fetchChannels returns success`() {
        runBlocking {
            mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(channelDetailListJson()))

            val result = client.fetchChannels()

            assertTrue(result.successful)
            assertEquals(200, result.statusCode)
            assertEquals(1, result.body.data.size)
            assertEquals(NotificationChannelType.NTFY, result.body.data.first().type)
            assertEquals("/api/v1/notification-channels", mockWebServer.takeRequest().path)
        }
    }

    private fun channelDetailListJson(): String {
        return """
            {
              "data": [
                {
                  "id": 1,
                  "name": "my ntfy",
                  "type": "ntfy",
                  "supportsAlertInfo": true,
                  "config": {"url": "https://ntfy.sh/mytopic"}
                }
              ]
            }
        """.trimIndent()
    }

    @Test
    fun `fetchProviders returns providers list`() {
        runBlocking {
            mockWebServer.enqueue(
                MockResponse().setResponseCode(200).setBody(providersJson())
            )

            val result = client.fetchProviders()

            assertTrue(result.successful)
            assertEquals(2, result.body.version)
            assertEquals(1, result.body.providers.size)
            assertEquals("ntfy", result.body.providers.first().type)
            assertEquals("/api/v1/notification-channels/providers", mockWebServer.takeRequest().path)
        }
    }

    private fun providersJson(): String {
        return """
            {
              "version": 2,
              "providers": [
                {
                  "type": "ntfy",
                  "icon": "ntfy",
                  "labelKey": "providers.ntfy.label",
                  "supportsTest": true,
                  "supportsAlertInfo": true,
                  "sections": [{"key": "server", "labelKey": "providers.ntfy.section.server"}],
                  "fields": [
                    {
                      "key": "url",
                      "section": "server",
                      "labelKey": "providers.ntfy.url.label",
                      "type": "url",
                      "required": true,
                      "default": "https://ntfy.sh"
                    }
                  ]
                }
              ]
            }
        """.trimIndent()
    }

    @Test
    fun `createChannel posts body and returns detail`() {
        runBlocking {
            mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(channelDetailJson()))

            val request = CreateChannelRequest(
                name = "my ntfy",
                type = "ntfy",
                config = mapOf("url" to JsonPrimitive("https://ntfy.sh/mytopic"))
            )
            val result = client.createChannel(request)

            assertTrue(result.successful)
            assertEquals(1, result.body.data.id)

            val recorded = mockWebServer.takeRequest()
            assertEquals("/api/v1/notification-channels", recorded.path)
            assertEquals("POST", recorded.method)

            val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }
            val body = json.parseToJsonElement(recorded.body.readUtf8()).jsonObject
            assertEquals("my ntfy", body["name"]?.jsonPrimitive?.content)
            assertEquals("ntfy", body["type"]?.jsonPrimitive?.content)
            assertEquals(
                "https://ntfy.sh/mytopic",
                (body["config"] as? JsonObject)?.get("url")?.jsonPrimitive?.content
            )
        }
    }

    @Test
    fun `fetchChannel hits detail endpoint`() {
        runBlocking {
            mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(channelDetailJson()))

            val result = client.fetchChannel(3)

            assertTrue(result.successful)
            assertEquals(1, result.body.data.id)
            assertEquals("/api/v1/notification-channels/3", mockWebServer.takeRequest().path)
        }
    }

    @Test
    fun `updateChannel sends partial omit-null body`() {
        runBlocking {
            mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(channelDetailJson()))

            val result = client.updateChannel(
                3,
                UpdateChannelRequest(name = "renamed")
            )

            assertTrue(result.successful)

            val recorded = mockWebServer.takeRequest()
            assertEquals("/api/v1/notification-channels/3", recorded.path)
            assertEquals("PUT", recorded.method)

            val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }
            val body = json.parseToJsonElement(recorded.body.readUtf8()).jsonObject
            assertEquals("renamed", body["name"]?.jsonPrimitive?.content)
            assertNull("type must be omitted", body["type"])
            assertNull("config must be omitted", body["config"])
        }
    }

    @Test
    fun `deleteChannel hits delete endpoint and returns message`() {
        runBlocking {
            mockWebServer.enqueue(
                MockResponse().setResponseCode(200).setBody("""{"message": "deleted"}""")
            )

            val result = client.deleteChannel(3)

            assertTrue(result.successful)
            assertEquals("deleted", result.body.message)

            val recorded = mockWebServer.takeRequest()
            assertEquals("/api/v1/notification-channels/3", recorded.path)
            assertEquals("DELETE", recorded.method)
        }
    }

    @Test
    fun `testChannel posts to test endpoint with optional message`() {
        runBlocking {
            mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(testResponseJson()))

            val result = client.testChannel(3, TestChannelRequest(message = "hello"))

            assertTrue(result.successful)
            assertEquals(1, result.body.data.channelId)
            assertEquals(true, result.body.data.ok)

            val recorded = mockWebServer.takeRequest()
            assertEquals("/api/v1/notification-channels/3/test", recorded.path)
            assertEquals("POST", recorded.method)
            assertEquals("""{"message":"hello"}""", recorded.body.readUtf8())
        }
    }

    @Test
    fun `testInlineChannel posts to inline test endpoint`() {
        runBlocking {
            mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(testResponseJson()))

            val request = TestInlineChannelRequest(
                type = "ntfy",
                config = mapOf("url" to JsonPrimitive("https://ntfy.sh/t")),
                message = null
            )
            val result = client.testInlineChannel(request)

            assertTrue(result.successful)

            val recorded = mockWebServer.takeRequest()
            assertEquals("/api/v1/notification-channels/test", recorded.path)
            assertEquals("POST", recorded.method)

            val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }
            val body = json.parseToJsonElement(recorded.body.readUtf8()).jsonObject
            assertEquals("ntfy", body["type"]?.jsonPrimitive?.content)
            assertNull("message must be omitted, not null", body["message"])
        }
    }

    @Test
    fun `fetchChannels throws Unauthorized on 401`() {
        assertThrows(HttpClientException.Unauthorized::class.java) {
            runBlocking {
                mockWebServer.enqueue(MockResponse().setResponseCode(401))
                client.fetchChannels()
            }
        }
    }

    @Test
    fun `deleteChannel surfaces 409 as HttpError for channel-in-use`() {
        runBlocking {
            mockWebServer.enqueue(MockResponse().setResponseCode(409))
            try {
                client.deleteChannel(3)
            } catch (e: HttpClientException.HttpError) {
                assertEquals(409, e.statusCode)
            }
        }
    }
}
