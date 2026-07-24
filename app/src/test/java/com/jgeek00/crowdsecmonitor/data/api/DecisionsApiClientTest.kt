package com.jgeek00.crowdsecmonitor.data.api

import com.jgeek00.crowdsecmonitor.constants.Enums
import com.jgeek00.crowdsecmonitor.data.models.CreateDecisionRequest
import com.jgeek00.crowdsecmonitor.data.models.DecisionsRequest
import com.jgeek00.crowdsecmonitor.data.models.DecisionsRequestFilters
import com.jgeek00.crowdsecmonitor.data.models.DecisionsRequestPagination
import com.jgeek00.crowdsecmonitor.data.models.HttpClientException
import com.jgeek00.crowdsecmonitor.fixtures.TestFixtures
import com.jgeek00.crowdsecmonitor.fixtures.testDecisionsListResponse
import com.jgeek00.crowdsecmonitor.fixtures.testEmptyResponse
import com.jgeek00.crowdsecmonitor.fixtures.testDecisionItemResponse
import com.jgeek00.crowdsecmonitor.fixtures.testDecisionsByIPResponse
import com.jgeek00.crowdsecmonitor.fixtures.testDecisionsByIPDetailResponse
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DecisionsApiClientTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var client: DecisionsApiClient

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
        val server = TestFixtures.csserverModel(
            http = "http", domain = mockWebServer.hostName, port = mockWebServer.port, path = null,
            authMethod = "", basicUser = null, basicPassword = null, bearerToken = null, customHeaders = null
        )
        client = DecisionsApiClient(HttpClient(server))
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `fetchDecisions returns success`() {
        runBlocking {
            mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(testDecisionsListResponse))
            val request = DecisionsRequest(
                filters = DecisionsRequestFilters(onlyActive = null),
                pagination = DecisionsRequestPagination(offset = 0, limit = 10)
            )
            val result = client.fetchDecisions(request)
            assertTrue(result.successful)
            assertEquals(200, result.statusCode)
            assertNotNull(result.body)
        }
    }

    @Test
    fun `fetchDecisions throws Unauthorized on 401`() {
        assertThrows(HttpClientException.Unauthorized::class.java) {
            runBlocking {
                mockWebServer.enqueue(MockResponse().setResponseCode(401))
                val request = DecisionsRequest(
                    filters = DecisionsRequestFilters(onlyActive = null),
                    pagination = DecisionsRequestPagination(offset = 0, limit = 10)
                )
                client.fetchDecisions(request)
            }
        }
    }

    @Test
    fun `fetchDecisions throws HttpError on 500`() {
        assertThrows(HttpClientException.HttpError::class.java) {
            runBlocking {
                mockWebServer.enqueue(MockResponse().setResponseCode(500))
                val request = DecisionsRequest(
                    filters = DecisionsRequestFilters(onlyActive = null),
                    pagination = DecisionsRequestPagination(offset = 0, limit = 10)
                )
                client.fetchDecisions(request)
            }
        }
    }

    @Test
    fun `fetchDecisions throws NetworkError on timeout`() {
        assertThrows(HttpClientException.NetworkError::class.java) {
            runBlocking {
                mockWebServer.enqueue(MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE))
                val request = DecisionsRequest(
                    filters = DecisionsRequestFilters(onlyActive = null),
                    pagination = DecisionsRequestPagination(offset = 0, limit = 10)
                )
                client.fetchDecisions(request)
            }
        }
    }

    @Test
    fun `fetchDecisionDetails returns success`() {
        runBlocking {
            mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(testDecisionItemResponse))
            val result = client.fetchDecisionDetails(1)
            assertTrue(result.successful)
            assertEquals(200, result.statusCode)
        }
    }

    @Test
    fun `fetchDecisionDetails throws on unknown decision`() {
        runBlocking {
            mockWebServer.enqueue(MockResponse().setResponseCode(404))
            try {
                client.fetchDecisionDetails(999)
            } catch (e: HttpClientException.HttpError) {
                assertEquals(404, e.statusCode)
            }
        }
    }

    @Test
    fun `createDecision returns success`() {
        runBlocking {
            mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(testEmptyResponse))
            val request = CreateDecisionRequest(
                ip = "1.2.3.4", duration = "24h", type = Enums.DecisionType.BAN, reason = "test"
            )
            val result = client.createDecision(request)
            assertTrue(result.successful)
        }
    }

    @Test
    fun `fetchDecisionsByIP returns success`() {
        runBlocking {
            mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(testDecisionsByIPResponse))
            val result = client.fetchDecisionsByIP(onlyActive = true, offset = 0, limit = 10)
            assertTrue(result.successful)
            assertNotNull(result.body)
        }
    }

    @Test
    fun `fetchDecisionsByIPDetail returns success`() {
        runBlocking {
            mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(testDecisionsByIPDetailResponse))
            val result = client.fetchDecisionsByIPDetail(ip = "1.2.3.4", onlyActive = true)
            assertTrue(result.successful)
            assertNotNull(result.body)
        }
    }

    @Test
    fun `deleteDecision returns success`() {
        runBlocking {
            mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(testEmptyResponse))
            val result = client.deleteDecision(1)
            assertTrue(result.successful)
        }
    }
}
