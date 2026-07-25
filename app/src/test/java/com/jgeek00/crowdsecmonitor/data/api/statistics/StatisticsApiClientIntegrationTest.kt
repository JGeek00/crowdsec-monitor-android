package com.jgeek00.crowdsecmonitor.data.api.statistics

import com.jgeek00.crowdsecmonitor.MainDispatcherRule
import com.jgeek00.crowdsecmonitor.data.api.CrowdSecApiClient
import com.jgeek00.crowdsecmonitor.data.models.HttpClientException
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
class StatisticsApiClientIntegrationTest {

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
    fun `fetchStatistics returns statistics`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(TestFixtures.statisticsResponseJson))
        val result = apiClient.statistics.fetchStatistics()
        assertTrue(result.successful)
        assertEquals(150, result.body.alertsLast24Hours)
    }

    @Test
    fun `fetchStatistics with amount and since`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(TestFixtures.statisticsResponseJson))
        val result = apiClient.statistics.fetchStatistics(amount = 7, since = "2026-07-01T00:00:00Z")
        assertTrue(result.successful)
    }

    @Test
    fun `fetchStatistics throws on 500`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(500))
        try {
            apiClient.statistics.fetchStatistics()
            fail("Expected exception")
        } catch (_: HttpClientException) {
        }
    }

    @Test
    fun `fetchCountriesStatistics returns countries`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(TestFixtures.topCountriesJson))
        val result = apiClient.statistics.countries.fetchCountriesStatistics()
        assertTrue(result.successful)
        assertEquals(2, result.body.size)
    }

    @Test
    fun `fetchCountriesStatistics throws on 500`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(500))
        try {
            apiClient.statistics.countries.fetchCountriesStatistics()
            fail("Expected exception")
        } catch (_: HttpClientException) {
        }
    }

    @Test
    fun `fetchIpOwnersStatistics returns owners`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(TestFixtures.topIpOwnersJson))
        val result = apiClient.statistics.ipOwners.fetchIpOwnersStatistics()
        assertTrue(result.successful)
        assertEquals(1, result.body.size)
    }

    @Test
    fun `fetchIpOwnersStatistics throws on 500`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(500))
        try {
            apiClient.statistics.ipOwners.fetchIpOwnersStatistics()
            fail("Expected exception")
        } catch (_: HttpClientException) {
        }
    }

    @Test
    fun `fetchScenariosStatistics returns scenarios`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(TestFixtures.topScenariosJson))
        val result = apiClient.statistics.scenarios.fetchScenariosStatistics()
        assertTrue(result.successful)
        assertEquals(1, result.body.size)
    }

    @Test
    fun `fetchScenariosStatistics throws on 500`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(500))
        try {
            apiClient.statistics.scenarios.fetchScenariosStatistics()
            fail("Expected exception")
        } catch (_: HttpClientException) {
        }
    }

    @Test
    fun `fetchTargetsStatistics returns targets`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(TestFixtures.topTargetsJson))
        val result = apiClient.statistics.targets.fetchTargetsStatistics()
        assertTrue(result.successful)
        assertEquals(1, result.body.size)
    }

    @Test
    fun `fetchTargetsStatistics throws on 500`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(500))
        try {
            apiClient.statistics.targets.fetchTargetsStatistics()
            fail("Expected exception")
        } catch (_: HttpClientException) {
        }
    }
}
