package com.jgeek00.crowdsecmonitor.integration

import com.jgeek00.crowdsecmonitor.MainDispatcherRule
import com.jgeek00.crowdsecmonitor.data.api.CrowdSecApiClient
import com.jgeek00.crowdsecmonitor.data.models.DecisionsRequest
import com.jgeek00.crowdsecmonitor.data.models.DecisionsRequestFilters
import com.jgeek00.crowdsecmonitor.data.models.DecisionsRequestPagination
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
class DecisionsFlowIntegrationTest {

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
    fun `decisions list success through full stack`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(TestFixtures.decisionsListJson))
        val result = apiClient.decisions.fetchDecisions(
            DecisionsRequest(
                filters = DecisionsRequestFilters(onlyActive = true, groupByIP = null),
                pagination = DecisionsRequestPagination(offset = 0, limit = 50)
            )
        )
        assertTrue(result.successful)
        assertEquals(1, result.body.items.size)
        assertEquals("1.2.3.4", result.body.items[0].value)
    }

    @Test
    fun `500 error maps to HttpClientException`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(500))
        try {
            apiClient.decisions.fetchDecisions(
                DecisionsRequest(
                    filters = DecisionsRequestFilters(onlyActive = true, groupByIP = null),
                    pagination = DecisionsRequestPagination(offset = 0, limit = 50)
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
            apiClient.decisions.fetchDecisions(
                DecisionsRequest(
                    filters = DecisionsRequestFilters(onlyActive = true, groupByIP = null),
                    pagination = DecisionsRequestPagination(offset = 0, limit = 50)
                )
            )
            fail("Expected Unauthorized")
        } catch (_: HttpClientException.Unauthorized) {
            // expected
        }
    }
}
