package com.jgeek00.crowdsecmonitor.data.api

import com.jgeek00.crowdsecmonitor.MainDispatcherRule
import com.jgeek00.crowdsecmonitor.data.models.HttpClientException
import com.jgeek00.crowdsecmonitor.data.models.HttpResponse
import com.jgeek00.crowdsecmonitor.fixtures.TestFixtures
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Rule
import org.junit.Test
class CrowdSecApiClientTest {

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
    fun `checkApiStatus returns success`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(TestFixtures.apiStatusJson))

        val result = apiClient.checkApiStatus()

        assertTrue(result.successful)
        assertEquals(200, result.statusCode)
        assertTrue(result.body.csLapi.lapiConnected)
    }

    @Test
    fun `checkApiStatus throws HttpClientException on 500`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(500))

        try {
            apiClient.checkApiStatus()
            fail("Expected HttpClientException")
        } catch (e: HttpClientException) {
            assertTrue(e is HttpClientException.HttpError)
            assertEquals(500, (e as HttpClientException.HttpError).statusCode)
        }
    }

    @Test
    fun `checkApiStatus throws Unauthorized on 401`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(401))

        try {
            apiClient.checkApiStatus()
            fail("Expected Unauthorized")
        } catch (e: HttpClientException) {
            assertTrue(e is HttpClientException.Unauthorized)
        }
    }

    @Test
    fun `checkApiStatus throws NetworkError on IO exception`() = runTest {
        // Use an unreachable address to trigger IOException
        val badClient = CrowdSecApiClient(
            TestFixtures.csserverModel(
                http = "http",
                domain = "localhost",
                port = 1,
                path = "",
                authMethod = "none"
            )
        )

        try {
            badClient.checkApiStatus()
            fail("Expected HttpClientException")
        } catch (e: HttpClientException) {
            assertTrue("Expected NetworkError but got ${e::class.simpleName}", e is HttpClientException.NetworkError)
        }
    }

    @Test
    fun `checkApiStatus throws HttpClientException on 400`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(400).setBody(TestFixtures.apiErrorJson))

        try {
            apiClient.checkApiStatus()
            fail("Expected HttpClientException")
        } catch (e: HttpClientException) {
            assertTrue("Expected HttpClientException but got ${e::class.simpleName}", e is HttpClientException)
        }
    }

    @Test
    fun `statistics property is lazily initialized`() {
        assertNotNull(apiClient.statistics)
        assertNotNull(apiClient.statistics.countries)
        assertNotNull(apiClient.statistics.ipOwners)
        assertNotNull(apiClient.statistics.scenarios)
        assertNotNull(apiClient.statistics.targets)
    }

    @Test
    fun `alerts property is lazily initialized`() {
        assertNotNull(apiClient.alerts)
    }

    @Test
    fun `decisions property is lazily initialized`() {
        assertNotNull(apiClient.decisions)
    }

    @Test
    fun `allowlists property is lazily initialized`() {
        assertNotNull(apiClient.allowlists)
    }

    @Test
    fun `blocklists property is lazily initialized`() {
        assertNotNull(apiClient.blocklists)
    }

    @Test
    fun `disconnectApiStatusStream does not throw`() {
        apiClient.disconnectApiStatusStream()
    }

    @Test
    fun `invalidate does not throw`() = runTest {
        apiClient.invalidate()
    }
}
