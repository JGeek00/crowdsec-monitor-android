package com.jgeek00.crowdsecmonitor.integration

import com.jgeek00.crowdsecmonitor.MainDispatcherRule
import com.jgeek00.crowdsecmonitor.data.api.CrowdSecApiClient
import com.jgeek00.crowdsecmonitor.data.models.AlertsRequest
import com.jgeek00.crowdsecmonitor.data.models.AlertsRequestFilters
import com.jgeek00.crowdsecmonitor.data.models.AlertsRequestPagination
import com.jgeek00.crowdsecmonitor.data.models.HttpClientException
import com.jgeek00.crowdsecmonitor.fixtures.TestFixtures
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Rule
import org.junit.Test
class AlertsFlowIntegrationTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var mockWebServer: MockWebServer
    private lateinit var apiClient: CrowdSecApiClient

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
        val server = TestFixtures.csserverModel(
            http = "http",
            domain = mockWebServer.hostName,
            port = mockWebServer.port,
            path = "",
            authMethod = "none"
        )
        apiClient = CrowdSecApiClient(server)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `alerts list success through full stack`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(TestFixtures.alertsListJson))
        val result = apiClient.alerts.fetchAlerts(
            AlertsRequest(
                filters = AlertsRequestFilters(emptyList(), emptyList(), emptyList(), emptyList()),
                pagination = AlertsRequestPagination(offset = 0, limit = 50)
            )
        )
        assertTrue(result.successful)
        assertEquals(1, result.body.items.size)
        assertEquals("uuid-0001", result.body.items[0].uuid)
    }

    @Test
    fun `500 error maps to HttpClientException`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(500))
        try {
            apiClient.alerts.fetchAlerts(
                AlertsRequest(
                    filters = AlertsRequestFilters(emptyList(), emptyList(), emptyList(), emptyList()),
                    pagination = AlertsRequestPagination(offset = 0, limit = 50)
                )
            )
            fail("Expected HttpClientException")
        } catch (_: HttpClientException) {
            // expected
        }
    }

    @Test
    fun `401 maps to Unauthorized`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(401))
        try {
            apiClient.alerts.fetchAlerts(
                AlertsRequest(
                    filters = AlertsRequestFilters(emptyList(), emptyList(), emptyList(), emptyList()),
                    pagination = AlertsRequestPagination(offset = 0, limit = 50)
                )
            )
            fail("Expected Unauthorized")
        } catch (_: HttpClientException.Unauthorized) {
            // expected
        }
    }
}
