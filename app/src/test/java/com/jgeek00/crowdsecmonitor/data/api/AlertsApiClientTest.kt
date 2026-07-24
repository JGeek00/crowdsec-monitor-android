package com.jgeek00.crowdsecmonitor.data.api

import com.jgeek00.crowdsecmonitor.data.models.AlertsRequest
import com.jgeek00.crowdsecmonitor.data.models.AlertsRequestFilters
import com.jgeek00.crowdsecmonitor.data.models.AlertsRequestPagination
import com.jgeek00.crowdsecmonitor.data.models.HttpClientException
import com.jgeek00.crowdsecmonitor.fixtures.TestFixtures
import com.jgeek00.crowdsecmonitor.fixtures.testAlertsListResponse
import com.jgeek00.crowdsecmonitor.fixtures.testAlertDetailsResponse
import com.jgeek00.crowdsecmonitor.fixtures.testEmptyResponse
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AlertsApiClientTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var client: AlertsApiClient

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
        val server = TestFixtures.csserverModel(
            http = "http", domain = mockWebServer.hostName, port = mockWebServer.port, path = null,
            authMethod = "", basicUser = null, basicPassword = null, bearerToken = null, customHeaders = null
        )
        client = AlertsApiClient(HttpClient(server))
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `fetchAlerts returns success`() {
        runBlocking {
            mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(testAlertsListResponse))
            val request = AlertsRequest(
                filters = AlertsRequestFilters(),
                pagination = AlertsRequestPagination(offset = 0, limit = 10)
            )
            val result = client.fetchAlerts(request)
            assertTrue(result.successful)
            assertEquals(200, result.statusCode)
            assertNotNull(result.body)
        }
    }

    @Test
    fun `fetchAlerts throws Unauthorized on 401`() {
        assertThrows(HttpClientException.Unauthorized::class.java) {
            runBlocking {
                mockWebServer.enqueue(MockResponse().setResponseCode(401))
                val request = AlertsRequest(
                    filters = AlertsRequestFilters(),
                    pagination = AlertsRequestPagination(offset = 0, limit = 10)
                )
                client.fetchAlerts(request)
            }
        }
    }

    @Test
    fun `fetchAlertDetails returns success`() {
        runBlocking {
            mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(testAlertDetailsResponse))
            val result = client.fetchAlertDetails(1)
            assertTrue(result.successful)
            assertNotNull(result.body)
        }
    }

    @Test
    fun `deleteAlert returns success`() {
        runBlocking {
            mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(testEmptyResponse))
            val result = client.deleteAlert(1)
            assertTrue(result.successful)
        }
    }

    @Test
    fun `deleteAlert throws on unknown`() {
        runBlocking {
            mockWebServer.enqueue(MockResponse().setResponseCode(404))
            try {
                client.deleteAlert(999)
            } catch (e: HttpClientException.HttpError) {
                assertEquals(404, e.statusCode)
            }
        }
    }
}
