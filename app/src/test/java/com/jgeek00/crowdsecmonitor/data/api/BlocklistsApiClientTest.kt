package com.jgeek00.crowdsecmonitor.data.api

import com.jgeek00.crowdsecmonitor.data.models.AddBlocklistRequest
import com.jgeek00.crowdsecmonitor.data.models.BlocklistsCheckIPsRequest
import com.jgeek00.crowdsecmonitor.data.models.BlocklistsCheckDomainRequest
import com.jgeek00.crowdsecmonitor.data.models.ToggleBlocklistRequest
import com.jgeek00.crowdsecmonitor.data.models.HttpClientException
import com.jgeek00.crowdsecmonitor.fixtures.TestFixtures
import com.jgeek00.crowdsecmonitor.fixtures.testBlocklistsListResponse
import com.jgeek00.crowdsecmonitor.fixtures.testBlocklistDataResponse
import com.jgeek00.crowdsecmonitor.fixtures.testBlocklistIpsResponse
import com.jgeek00.crowdsecmonitor.fixtures.testEmptyResponse
import com.jgeek00.crowdsecmonitor.fixtures.testRefreshBlocklistsResponse
import com.jgeek00.crowdsecmonitor.fixtures.testBlocklistsCheckIPsResponse
import com.jgeek00.crowdsecmonitor.fixtures.testBlocklistsCheckDomainResponse
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class BlocklistsApiClientTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var client: BlocklistsApiClient

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
        val server = TestFixtures.csserverModel(
            http = "http", domain = mockWebServer.hostName, port = mockWebServer.port, path = null,
            authMethod = "", basicUser = null, basicPassword = null, bearerToken = null, customHeaders = null
        )
        client = BlocklistsApiClient(HttpClient(server))
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `fetchBlocklists returns success`() {
        runBlocking {
            mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(testBlocklistsListResponse))
            val result = client.fetchBlocklists()
            assertTrue(result.successful)
            assertNotNull(result.body)
        }
    }

    @Test
    fun `fetchBlocklists throws Unauthorized on 401`() {
        assertThrows(HttpClientException.Unauthorized::class.java) {
            runBlocking {
                mockWebServer.enqueue(MockResponse().setResponseCode(401))
                client.fetchBlocklists()
            }
        }
    }

    @Test
    fun `fetchBlocklistData returns success`() {
        runBlocking {
            mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(testBlocklistDataResponse))
            val result = client.fetchBlocklistData("test-list")
            assertTrue(result.successful)
            assertNotNull(result.body)
        }
    }

    @Test
    fun `fetchBlocklistIps returns success`() {
        runBlocking {
            mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(testBlocklistIpsResponse))
            val result = client.fetchBlocklistIps("test-list")
            assertTrue(result.successful)
            assertNotNull(result.body)
        }
    }

    @Test
    fun `addBlocklist returns success`() {
        runBlocking {
            mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(testEmptyResponse))
            val request = AddBlocklistRequest(name = "test", url = "https://example.com/list")
            val result = client.addBlocklist(request)
            assertTrue(result.successful)
        }
    }

    @Test
    fun `toggleBlocklist returns success`() {
        runBlocking {
            mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(testEmptyResponse))
            val request = ToggleBlocklistRequest(enabled = false)
            val result = client.toggleBlocklist("test-list", request)
            assertTrue(result.successful)
        }
    }

    @Test
    fun `deleteBlocklist returns success`() {
        runBlocking {
            mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(testEmptyResponse))
            val result = client.deleteBlocklist("test-list")
            assertTrue(result.successful)
        }
    }

    @Test
    fun `refreshAllBlocklists returns success`() {
        runBlocking {
            mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(testRefreshBlocklistsResponse))
            val result = client.refreshAllBlocklists()
            assertTrue(result.successful)
            assertNotNull(result.body)
        }
    }

    @Test
    fun `refreshBlocklist returns success`() {
        runBlocking {
            mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(testRefreshBlocklistsResponse))
            val result = client.refreshBlocklist("test-list")
            assertTrue(result.successful)
            assertNotNull(result.body)
        }
    }

    @Test
    fun `checkIps returns success`() {
        runBlocking {
            mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(testBlocklistsCheckIPsResponse))
            val request = BlocklistsCheckIPsRequest(ips = listOf("1.2.3.4"))
            val result = client.checkIps(request)
            assertTrue(result.successful)
            assertNotNull(result.body)
        }
    }

    @Test
    fun `checkDomain returns success`() {
        runBlocking {
            mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(testBlocklistsCheckDomainResponse))
            val request = BlocklistsCheckDomainRequest(domain = "example.com")
            val result = client.checkDomain(request)
            assertTrue(result.successful)
            assertNotNull(result.body)
        }
    }
}
