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
class BlocklistsApiClientIntegrationTest {

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
    fun `fetchBlocklists with params`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(TestFixtures.blocklistsListJson))
        val result = apiClient.blocklists.fetchBlocklists(BlocklistsRequest(limit = 10, offset = 0))
        assertTrue(result.successful)
        assertEquals(1, result.body.items.size)
    }

    @Test
    fun `fetchBlocklists without params`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(TestFixtures.blocklistsListJson))
        val result = apiClient.blocklists.fetchBlocklists()
        assertTrue(result.successful)
    }

    @Test
    fun `fetchBlocklists throws on 500`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(500))
        try {
            apiClient.blocklists.fetchBlocklists()
            fail("Expected exception")
        } catch (_: HttpClientException) {
        }
    }

    @Test
    fun `fetchBlocklistData returns blocklist data`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(TestFixtures.blocklistDataJson))
        val result = apiClient.blocklists.fetchBlocklistData("1")
        assertTrue(result.successful)
        assertEquals("1", result.body.data.id)
    }

    @Test
    fun `fetchBlocklistData throws on 500`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(500))
        try {
            apiClient.blocklists.fetchBlocklistData("1")
            fail("Expected exception")
        } catch (_: HttpClientException) {
        }
    }

    @Test
    fun `fetchBlocklistIps returns IPs`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(TestFixtures.blocklistIpsJson))
        val result = apiClient.blocklists.fetchBlocklistIps("1")
        assertTrue(result.successful)
        assertEquals(2, result.body.total)
    }

    @Test
    fun `fetchBlocklistIps throws on 500`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(500))
        try {
            apiClient.blocklists.fetchBlocklistIps("1")
            fail("Expected exception")
        } catch (_: HttpClientException) {
        }
    }

    @Test
    fun `addBlocklist creates blocklist`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(TestFixtures.emptyResponseJson))
        val request = AddBlocklistRequest(name = "test", url = "https://example.com/list.txt")
        val result = apiClient.blocklists.addBlocklist(request)
        assertTrue(result.successful)
    }

    @Test
    fun `addBlocklist throws on 500`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(500))
        try {
            val request = AddBlocklistRequest(name = "test", url = "https://example.com/list.txt")
            apiClient.blocklists.addBlocklist(request)
            fail("Expected exception")
        } catch (_: HttpClientException) {
        }
    }

    @Test
    fun `toggleBlocklist enables and disables`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(TestFixtures.emptyResponseJson))
        val result = apiClient.blocklists.toggleBlocklist("1", ToggleBlocklistRequest(enabled = true))
        assertTrue(result.successful)
    }

    @Test
    fun `toggleBlocklist throws on 500`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(500))
        try {
            apiClient.blocklists.toggleBlocklist("1", ToggleBlocklistRequest(enabled = true))
            fail("Expected exception")
        } catch (_: HttpClientException) {
        }
    }

    @Test
    fun `deleteBlocklist removes blocklist`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(TestFixtures.emptyResponseJson))
        val result = apiClient.blocklists.deleteBlocklist("1")
        assertTrue(result.successful)
    }

    @Test
    fun `refreshAllBlocklists triggers refresh`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(TestFixtures.refreshBlocklistsJson))
        val result = apiClient.blocklists.refreshAllBlocklists()
        assertTrue(result.successful)
    }

    @Test
    fun `refreshAllBlocklists throws on 500`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(500))
        try {
            apiClient.blocklists.refreshAllBlocklists()
            fail("Expected exception")
        } catch (_: HttpClientException) {
        }
    }

    @Test
    fun `refreshBlocklist refreshes single blocklist`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(TestFixtures.refreshBlocklistsJson))
        val result = apiClient.blocklists.refreshBlocklist("1")
        assertTrue(result.successful)
    }

    @Test
    fun `refreshBlocklist throws on 500`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(500))
        try {
            apiClient.blocklists.refreshBlocklist("1")
            fail("Expected exception")
        } catch (_: HttpClientException) {
        }
    }

    @Test
    fun `checkIps checks IPs against blocklists`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(TestFixtures.blocklistsCheckIPsJson))
        val request = BlocklistsCheckIPsRequest(ips = listOf("1.2.3.4"))
        val result = apiClient.blocklists.checkIps(request)
        assertTrue(result.successful)
    }

    @Test
    fun `checkIps throws on 500`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(500))
        try {
            apiClient.blocklists.checkIps(BlocklistsCheckIPsRequest(ips = listOf("1.2.3.4")))
            fail("Expected exception")
        } catch (_: HttpClientException) {
        }
    }

    @Test
    fun `checkDomain checks domain against blocklists`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(TestFixtures.blocklistsCheckDomainJson))
        val request = BlocklistsCheckDomainRequest(domain = "example.com")
        val result = apiClient.blocklists.checkDomain(request)
        assertTrue(result.successful)
    }

    @Test
    fun `checkDomain throws on 500`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(500))
        try {
            apiClient.blocklists.checkDomain(BlocklistsCheckDomainRequest(domain = "example.com"))
            fail("Expected exception")
        } catch (_: HttpClientException) {
        }
    }

    @Test
    fun `fetchBlocklists throws Unauthorized on 401`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(401))
        try {
            apiClient.blocklists.fetchBlocklists()
            fail("Expected exception")
        } catch (_: HttpClientException.Unauthorized) {
        }
    }

    @Test
    fun `addBlocklist throws Unauthorized on 401`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(401))
        try {
            apiClient.blocklists.addBlocklist(AddBlocklistRequest(name = "test", url = "https://example.com"))
            fail("Expected exception")
        } catch (_: HttpClientException.Unauthorized) {
        }
    }

    @Test
    fun `fetchBlocklists with null body throws`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(200))
        try {
            apiClient.blocklists.fetchBlocklists()
            fail("Expected exception")
        } catch (_: HttpClientException) {
        }
    }

    @Test
    fun `fetchBlocklists returns success with null requestParams`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(TestFixtures.blocklistsListJson))
        val result = apiClient.blocklists.fetchBlocklists(null)
        assertTrue(result.successful)
    }

    @Test
    fun `addBlocklist with 200 empty body throws`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(200))
        try {
            apiClient.blocklists.addBlocklist(AddBlocklistRequest(name = "test", url = "https://example.com"))
            fail("Expected exception")
        } catch (_: HttpClientException) {
        }
    }

    @Test
    fun `deleteBlocklist with 200 empty body throws`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(200))
        try {
            apiClient.blocklists.deleteBlocklist("1")
            fail("Expected exception")
        } catch (_: HttpClientException) {
        }
    }

    @Test
    fun `refreshAllBlocklists with 200 empty body throws`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(200))
        try {
            apiClient.blocklists.refreshAllBlocklists()
            fail("Expected exception")
        } catch (_: HttpClientException) {
        }
    }

    @Test
    fun `refreshBlocklist with 200 empty body throws`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(200))
        try {
            apiClient.blocklists.refreshBlocklist("1")
            fail("Expected exception")
        } catch (_: HttpClientException) {
        }
    }

    @Test
    fun `checkIps with 200 empty body throws`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(200))
        try {
            apiClient.blocklists.checkIps(BlocklistsCheckIPsRequest(ips = listOf("1.2.3.4")))
            fail("Expected exception")
        } catch (_: HttpClientException) {
        }
    }

    @Test
    fun `checkDomain with 200 empty body throws`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(200))
        try {
            apiClient.blocklists.checkDomain(BlocklistsCheckDomainRequest(domain = "example.com"))
            fail("Expected exception")
        } catch (_: HttpClientException) {
        }
    }
}
