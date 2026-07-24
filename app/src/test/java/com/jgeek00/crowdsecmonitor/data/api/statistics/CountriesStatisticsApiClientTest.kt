package com.jgeek00.crowdsecmonitor.data.api.statistics

import com.jgeek00.crowdsecmonitor.data.api.HttpClient
import com.jgeek00.crowdsecmonitor.data.models.HttpClientException
import com.jgeek00.crowdsecmonitor.fixtures.TestFixtures
import com.jgeek00.crowdsecmonitor.fixtures.testTopCountriesResponse
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CountriesStatisticsApiClientTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var client: CountriesStatisticsApiClient

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
        val server = TestFixtures.csserverModel(
            http = "http", domain = mockWebServer.hostName, port = mockWebServer.port, path = null,
            authMethod = "", basicUser = null, basicPassword = null, bearerToken = null, customHeaders = null
        )
        client = CountriesStatisticsApiClient(HttpClient(server))
    }

    @After
    fun tearDown() { mockWebServer.shutdown() }

    @Test
    fun `fetchCountriesStatistics returns success`() {
        runBlocking {
            mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(testTopCountriesResponse))
            val result = client.fetchCountriesStatistics()
            assertTrue(result.successful)
            assertNotNull(result.body)
        }
    }

    @Test
    fun `unauthorized throws`() {
        assertThrows(HttpClientException.Unauthorized::class.java) {
            runBlocking {
                mockWebServer.enqueue(MockResponse().setResponseCode(401))
                client.fetchCountriesStatistics()
            }
        }
    }
}
