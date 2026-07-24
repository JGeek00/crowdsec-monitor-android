package com.jgeek00.crowdsecmonitor.data.api.statistics

import com.jgeek00.crowdsecmonitor.data.api.HttpClient
import com.jgeek00.crowdsecmonitor.data.models.HttpClientException
import com.jgeek00.crowdsecmonitor.fixtures.TestFixtures
import com.jgeek00.crowdsecmonitor.fixtures.testStatisticsResponse
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class StatisticsApiClientTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var httpClient: HttpClient
    private lateinit var statisticsClient: StatisticsApiClient

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
        val server = TestFixtures.csserverModel(
            http = "http", domain = mockWebServer.hostName, port = mockWebServer.port, path = null,
            authMethod = "", basicUser = null, basicPassword = null, bearerToken = null, customHeaders = null
        )
        httpClient = HttpClient(server)
        statisticsClient = StatisticsApiClient(httpClient)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `fetchStatistics returns success`() {
        runBlocking {
            mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(testStatisticsResponse))
            val result = statisticsClient.fetchStatistics(amount = 10, since = "24h")
            assertTrue(result.successful)
            assertNotNull(result.body)
        }
    }

    @Test
    fun `fetchStatistics throws Unauthorized on 401`() {
        assertThrows(HttpClientException.Unauthorized::class.java) {
            runBlocking {
                mockWebServer.enqueue(MockResponse().setResponseCode(401))
                statisticsClient.fetchStatistics()
            }
        }
    }
}
