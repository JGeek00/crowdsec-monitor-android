package com.jgeek00.crowdsecmonitor.data.api

import com.jgeek00.crowdsecmonitor.data.models.HttpClientException
import com.jgeek00.crowdsecmonitor.fixtures.TestFixtures
import com.jgeek00.crowdsecmonitor.fixtures.testApiStatusJson
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CrowdSecApiClientTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var client: CrowdSecApiClient

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
        val server = TestFixtures.csserverModel(
            http = "http", domain = mockWebServer.hostName, port = mockWebServer.port, path = null,
            authMethod = "", basicUser = null, basicPassword = null, bearerToken = null, customHeaders = null
        )
        client = CrowdSecApiClient(server)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `checkApiStatus returns success`() {
        runBlocking {
            mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(testApiStatusJson))
            val result = client.checkApiStatus()
            assertTrue(result.successful)
            assertEquals(200, result.statusCode)
            assertNotNull(result.body)
        }
    }

    @Test
    fun `checkApiStatus throws Unauthorized on 401`() {
        assertThrows(HttpClientException.Unauthorized::class.java) {
            runBlocking {
                mockWebServer.enqueue(MockResponse().setResponseCode(401))
                client.checkApiStatus()
            }
        }
    }

    @Test
    fun `checkApiStatus throws NetworkError on IO exception`() {
        assertThrows(HttpClientException.NetworkError::class.java) {
            runBlocking {
                mockWebServer.shutdown()
                client.checkApiStatus()
            }
        }
    }
}
