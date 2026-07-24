package com.jgeek00.crowdsecmonitor.data.api.statistics

import com.jgeek00.crowdsecmonitor.data.api.HttpClient
import com.jgeek00.crowdsecmonitor.fixtures.TestFixtures
import com.jgeek00.crowdsecmonitor.fixtures.testTopTargetsResponse
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class TargetsStatisticsApiClientTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var client: TargetsStatisticsApiClient

    @Before
    fun setUp() {
        mockWebServer = MockWebServer().apply { start() }
        val server = TestFixtures.csserverModel(
            http = "http", domain = mockWebServer.hostName, port = mockWebServer.port, path = null,
            authMethod = "", basicUser = null, basicPassword = null, bearerToken = null, customHeaders = null
        )
        client = TargetsStatisticsApiClient(HttpClient(server))
    }

    @After
    fun tearDown() { mockWebServer.shutdown() }

    @Test
    fun `fetchTargetsStatistics returns success`() {
        runBlocking {
            mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(testTopTargetsResponse))
            val result = client.fetchTargetsStatistics()
            assertTrue(result.successful)
            assertNotNull(result.body)
        }
    }
}
