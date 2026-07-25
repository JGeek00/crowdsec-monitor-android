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
class AllowlistsApiClientIntegrationTest {

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
    fun `fetchAllowlists returns allowlists`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(TestFixtures.allowlistsListJson))
        val result = apiClient.allowlists.fetchAllowlists()
        assertTrue(result.successful)
        assertEquals(1, result.body.length)
    }

    @Test
    fun `fetchAllowlists throws on 500`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(500))
        try {
            apiClient.allowlists.fetchAllowlists()
            fail("Expected exception")
        } catch (_: HttpClientException) {
        }
    }

    @Test
    fun `checkIps checks IPs against allowlists`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(TestFixtures.allowlistsCheckIPsJson))
        val request = AllowlistsCheckIPsRequest(ips = listOf("1.2.3.4"))
        val result = apiClient.allowlists.checkIps(request)
        assertTrue(result.successful)
    }

    @Test
    fun `checkIps throws on 500`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(500))
        try {
            val request = AllowlistsCheckIPsRequest(ips = listOf("1.2.3.4"))
            apiClient.allowlists.checkIps(request)
            fail("Expected exception")
        } catch (_: HttpClientException) {
        }
    }
}
