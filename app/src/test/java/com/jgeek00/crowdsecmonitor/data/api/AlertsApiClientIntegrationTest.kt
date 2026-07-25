package com.jgeek00.crowdsecmonitor.data.api

import com.jgeek00.crowdsecmonitor.MainDispatcherRule
import com.jgeek00.crowdsecmonitor.data.models.*
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
class AlertsApiClientIntegrationTest {

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
    fun `fetchAlerts with filters returns alerts`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(TestFixtures.alertsListJson))
        val result = apiClient.alerts.fetchAlerts(
            AlertsRequest(
                filters = AlertsRequestFilters(
                    countries = listOf("US"),
                    scenarios = listOf("crowdsec/ssh-bf"),
                    ipOwners = emptyList(),
                    targets = emptyList()
                ),
                pagination = AlertsRequestPagination(offset = 0, limit = 50)
            )
        )
        assertTrue(result.successful)
        assertEquals(1, result.body.items.size)
    }

    @Test
    fun `fetchAlerts with empty filters`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(TestFixtures.alertsListJson))
        val result = apiClient.alerts.fetchAlerts(
            AlertsRequest(
                filters = AlertsRequestFilters(
                    countries = emptyList(),
                    scenarios = emptyList(),
                    ipOwners = emptyList(),
                    targets = emptyList()
                ),
                pagination = AlertsRequestPagination(offset = 0, limit = 50)
            )
        )
        assertTrue(result.successful)
    }

    @Test
    fun `fetchAlerts throws on 500`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(500))
        try {
            apiClient.alerts.fetchAlerts(
                AlertsRequest(
                    filters = AlertsRequestFilters(emptyList(), emptyList(), emptyList(), emptyList()),
                    pagination = AlertsRequestPagination(offset = 0, limit = 50)
                )
            )
            fail("Expected exception")
        } catch (_: HttpClientException) {
        }
    }

    @Test
    fun `fetchAlertDetails returns alert`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(TestFixtures.alertDetailsJson))
        val result = apiClient.alerts.fetchAlertDetails(1)
        assertTrue(result.successful)
        assertEquals(1, result.body.id)
    }

    @Test
    fun `fetchAlertDetails throws on 500`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(500))
        try {
            apiClient.alerts.fetchAlertDetails(1)
            fail("Expected exception")
        } catch (_: HttpClientException) {
        }
    }

    @Test
    fun `deleteAlert returns success`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(TestFixtures.emptyResponseJson))
        val result = apiClient.alerts.deleteAlert(1)
        assertTrue(result.successful)
    }

    @Test
    fun `deleteAlert throws on 500`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(500))
        try {
            apiClient.alerts.deleteAlert(1)
            fail("Expected exception")
        } catch (_: HttpClientException) {
        }
    }
}
