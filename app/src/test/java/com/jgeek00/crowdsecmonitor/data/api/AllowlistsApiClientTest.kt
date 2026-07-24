package com.jgeek00.crowdsecmonitor.data.api

import com.jgeek00.crowdsecmonitor.data.models.AllowlistsCheckIPsRequest
import com.jgeek00.crowdsecmonitor.data.models.HttpClientException
import com.jgeek00.crowdsecmonitor.fixtures.TestFixtures
import com.jgeek00.crowdsecmonitor.fixtures.testAllowlistsListResponse
import com.jgeek00.crowdsecmonitor.fixtures.testAllowlistsCheckIPsResponse
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AllowlistsApiClientTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var client: AllowlistsApiClient

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
        val server = TestFixtures.csserverModel(
            http = "http", domain = mockWebServer.hostName, port = mockWebServer.port, path = null,
            authMethod = "", basicUser = null, basicPassword = null, bearerToken = null, customHeaders = null
        )
        client = AllowlistsApiClient(HttpClient(server))
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `fetchAllowlists returns success`() {
        runBlocking {
            mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(testAllowlistsListResponse))
            val result = client.fetchAllowlists()
            assertTrue(result.successful)
            assertNotNull(result.body)
        }
    }

    @Test
    fun `fetchAllowlists throws Unauthorized on 401`() {
        assertThrows(HttpClientException.Unauthorized::class.java) {
            runBlocking {
                mockWebServer.enqueue(MockResponse().setResponseCode(401))
                client.fetchAllowlists()
            }
        }
    }

    @Test
    fun `checkIps returns success`() {
        runBlocking {
            mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(testAllowlistsCheckIPsResponse))
            val request = AllowlistsCheckIPsRequest(ips = listOf("1.2.3.4"))
            val result = client.checkIps(request)
            assertTrue(result.successful)
            assertNotNull(result.body)
        }
    }
}
