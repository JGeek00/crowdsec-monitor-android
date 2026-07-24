package com.jgeek00.crowdsecmonitor.data.api.statistics

import com.jgeek00.crowdsecmonitor.data.api.HttpClient
import com.jgeek00.crowdsecmonitor.fixtures.TestFixtures
import com.jgeek00.crowdsecmonitor.fixtures.testTopIpOwnersResponse
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class IpOwnersStatisticsApiClientTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var client: IpOwnersStatisticsApiClient

    @Before
    fun setUp() {
        mockWebServer = MockWebServer().apply { start() }
        val server = TestFixtures.csserverModel(
            http = "http", domain = mockWebServer.hostName, port = mockWebServer.port, path = null,
            authMethod = "", basicUser = null, basicPassword = null, bearerToken = null, customHeaders = null
        )
        client = IpOwnersStatisticsApiClient(HttpClient(server))
    }

    @After
    fun tearDown() { mockWebServer.shutdown() }

    @Test
    fun `fetchIpOwnersStatistics returns success`() {
        runBlocking {
            mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(testTopIpOwnersResponse))
            val result = client.fetchIpOwnersStatistics()
            assertTrue(result.successful)
            assertNotNull(result.body)
        }
    }
}
