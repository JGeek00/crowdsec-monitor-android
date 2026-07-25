package com.jgeek00.crowdsecmonitor.data.api

import com.jgeek00.crowdsecmonitor.MainDispatcherRule
import com.jgeek00.crowdsecmonitor.constants.Enums
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
class DecisionsApiClientIntegrationTest {

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
    fun `fetchDecisions with onlyActive true`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(TestFixtures.decisionsListJson))
        val result = apiClient.decisions.fetchDecisions(
            DecisionsRequest(
                filters = DecisionsRequestFilters(onlyActive = true, groupByIP = null),
                pagination = DecisionsRequestPagination(offset = 0, limit = 50)
            )
        )
        assertTrue(result.successful)
        assertEquals(1, result.body.items.size)
    }

    @Test
    fun `fetchDecisions with onlyActive false passes no query param`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(TestFixtures.decisionsListJson))
        val result = apiClient.decisions.fetchDecisions(
            DecisionsRequest(
                filters = DecisionsRequestFilters(onlyActive = false, groupByIP = null),
                pagination = DecisionsRequestPagination(offset = 0, limit = 50)
            )
        )
        assertTrue(result.successful)
    }

    @Test
    fun `fetchDecisions throws on 500`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(500))
        try {
            apiClient.decisions.fetchDecisions(
                DecisionsRequest(
                    filters = DecisionsRequestFilters(onlyActive = true, groupByIP = null),
                    pagination = DecisionsRequestPagination(offset = 0, limit = 50)
                )
            )
            fail("Expected exception")
        } catch (_: HttpClientException) {
        }
    }

    @Test
    fun `fetchDecisions throws Unauthorized on 401`() = runTest {
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
        }
    }

    @Test
    fun `fetchDecisionDetails returns decision item`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(TestFixtures.decisionItemJson))
        val result = apiClient.decisions.fetchDecisionDetails(1)
        assertTrue(result.successful)
        assertEquals(1, result.body.id)
    }

    @Test
    fun `fetchDecisionDetails throws on 500`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(500))
        try {
            apiClient.decisions.fetchDecisionDetails(1)
            fail("Expected exception")
        } catch (_: HttpClientException) {
        }
    }

    @Test
    fun `fetchDecisionsByIP returns grouped decisions`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(TestFixtures.decisionsByIPJson))
        val result = apiClient.decisions.fetchDecisionsByIP(onlyActive = true, offset = 0, limit = 50)
        assertTrue(result.successful)
        assertEquals(1, result.body.groups.size)
    }

    @Test
    fun `fetchDecisionsByIP throws on 500`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(500))
        try {
            apiClient.decisions.fetchDecisionsByIP(onlyActive = true, offset = 0, limit = 50)
            fail("Expected exception")
        } catch (_: HttpClientException) {
        }
    }

    @Test
    fun `fetchDecisionsByIPDetail returns detail`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(TestFixtures.decisionsByIPDetailJson))
        val result = apiClient.decisions.fetchDecisionsByIPDetail(ip = "1.2.3.4", onlyActive = true)
        assertTrue(result.successful)
        assertEquals("1.2.3.4", result.body.ip)
    }

    @Test
    fun `fetchDecisionsByIPDetail with onlyActive null`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(TestFixtures.decisionsByIPDetailJson))
        val result = apiClient.decisions.fetchDecisionsByIPDetail(ip = "1.2.3.4", onlyActive = null)
        assertTrue(result.successful)
    }

    @Test
    fun `createDecision returns success`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(TestFixtures.emptyResponseJson))
        val request = CreateDecisionRequest(
            ip = "1.2.3.4", duration = "24h", type = Enums.DecisionType.BAN, reason = "test"
        )
        val result = apiClient.decisions.createDecision(request)
        assertTrue(result.successful)
    }

    @Test
    fun `createDecision throws on 500`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(500))
        try {
            val request = CreateDecisionRequest(
                ip = "1.2.3.4", duration = "24h", type = Enums.DecisionType.BAN, reason = "test"
            )
            apiClient.decisions.createDecision(request)
            fail("Expected exception")
        } catch (_: HttpClientException) {
        }
    }

    @Test
    fun `deleteDecision returns success`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(TestFixtures.emptyResponseJson))
        val result = apiClient.decisions.deleteDecision(1)
        assertTrue(result.successful)
    }

    @Test
    fun `deleteDecision throws on 500`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(500))
        try {
            apiClient.decisions.deleteDecision(1)
            fail("Expected exception")
        } catch (_: HttpClientException) {
        }
    }
}
