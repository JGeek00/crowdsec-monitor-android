package com.jgeek00.crowdsecmonitor.data.api

import com.jgeek00.crowdsecmonitor.MainDispatcherRule
import com.jgeek00.crowdsecmonitor.data.models.*
import com.jgeek00.crowdsecmonitor.fixtures.TestFixtures
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Rule
import org.junit.Test
/**
 * Tests for error catch paths in API clients:
 * - SerializationException -> DecodingError
 * - IOException -> NetworkError
 */
class ApiClientErrorPathsTest {

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

    // ── DecisionsApiClient error paths ──────────────────────────

    @Test
    fun `decisions fetchDecisions malformed JSON throws DecodingError`() = runTest {
        mockWebServer.enqueue(okhttp3.mockwebserver.MockResponse().setResponseCode(200).setBody(TestFixtures.malformedJson))
        try {
            apiClient.decisions.fetchDecisions(
                DecisionsRequest(
                    filters = DecisionsRequestFilters(onlyActive = true, groupByIP = null),
                    pagination = DecisionsRequestPagination(offset = 0, limit = 50)
                )
            )
            fail("Expected DecodingError")
        } catch (e: HttpClientException) {
            assertTrue("Expected DecodingError got ${e::class.simpleName}", e is HttpClientException.DecodingError)
        }
    }

    @Test
    fun `decisions fetchDecisionDetails malformed JSON throws DecodingError`() = runTest {
        mockWebServer.enqueue(okhttp3.mockwebserver.MockResponse().setResponseCode(200).setBody(TestFixtures.malformedJson))
        try {
            apiClient.decisions.fetchDecisionDetails(1)
            fail("Expected DecodingError")
        } catch (e: HttpClientException) {
            assertTrue("Expected DecodingError got ${e::class.simpleName}", e is HttpClientException.DecodingError)
        }
    }

    @Test
    fun `decisions network error throws NetworkError`() = runTest {
        val badClient = CrowdSecApiClient(
            TestFixtures.csserverModel(
                http = "http", domain = "localhost", port = 1, path = "", authMethod = "none"
            )
        )
        try {
            badClient.decisions.fetchDecisions(
                DecisionsRequest(
                    filters = DecisionsRequestFilters(onlyActive = true, groupByIP = null),
                    pagination = DecisionsRequestPagination(offset = 0, limit = 50)
                )
            )
            fail("Expected NetworkError")
        } catch (e: HttpClientException) {
            assertTrue("Expected NetworkError got ${e::class.simpleName}", e is HttpClientException.NetworkError)
        }
    }

    // ── AlertsApiClient error paths ────────────────────────────

    @Test
    fun `alerts fetchAlerts malformed JSON throws DecodingError`() = runTest {
        mockWebServer.enqueue(okhttp3.mockwebserver.MockResponse().setResponseCode(200).setBody(TestFixtures.malformedJson))
        try {
            apiClient.alerts.fetchAlerts(
                AlertsRequest(
                    filters = AlertsRequestFilters(emptyList(), emptyList(), emptyList(), emptyList()),
                    pagination = AlertsRequestPagination(offset = 0, limit = 50)
                )
            )
            fail("Expected DecodingError")
        } catch (e: HttpClientException) {
            assertTrue("Expected DecodingError got ${e::class.simpleName}", e is HttpClientException.DecodingError)
        }
    }

    @Test
    fun `alerts fetchAlertDetails malformed JSON throws DecodingError`() = runTest {
        mockWebServer.enqueue(okhttp3.mockwebserver.MockResponse().setResponseCode(200).setBody(TestFixtures.malformedJson))
        try {
            apiClient.alerts.fetchAlertDetails(1)
            fail("Expected DecodingError")
        } catch (e: HttpClientException) {
            assertTrue("Expected DecodingError got ${e::class.simpleName}", e is HttpClientException.DecodingError)
        }
    }

    @Test
    fun `alerts deleteAlert malformed JSON throws DecodingError`() = runTest {
        mockWebServer.enqueue(okhttp3.mockwebserver.MockResponse().setResponseCode(200).setBody(TestFixtures.malformedJson))
        try {
            apiClient.alerts.deleteAlert(1)
            fail("Expected DecodingError")
        } catch (e: HttpClientException) {
            assertTrue("Expected DecodingError got ${e::class.simpleName}", e is HttpClientException.DecodingError)
        }
    }

    @Test
    fun `alerts network error throws NetworkError`() = runTest {
        val badClient = CrowdSecApiClient(
            TestFixtures.csserverModel(
                http = "http", domain = "localhost", port = 1, path = "", authMethod = "none"
            )
        )
        try {
            badClient.alerts.fetchAlerts(
                AlertsRequest(
                    filters = AlertsRequestFilters(emptyList(), emptyList(), emptyList(), emptyList()),
                    pagination = AlertsRequestPagination(offset = 0, limit = 50)
                )
            )
            fail("Expected NetworkError")
        } catch (e: HttpClientException) {
            assertTrue("Expected NetworkError got ${e::class.simpleName}", e is HttpClientException.NetworkError)
        }
    }

    // ── BlocklistsApiClient error paths ────────────────────────

    @Test
    fun `blocklists fetchBlocklists malformed JSON throws DecodingError`() = runTest {
        mockWebServer.enqueue(okhttp3.mockwebserver.MockResponse().setResponseCode(200).setBody(TestFixtures.malformedJson))
        try {
            apiClient.blocklists.fetchBlocklists()
            fail("Expected DecodingError")
        } catch (e: HttpClientException) {
            assertTrue("Expected DecodingError got ${e::class.simpleName}", e is HttpClientException.DecodingError)
        }
    }

    @Test
    fun `blocklists fetchBlocklistData malformed JSON throws DecodingError`() = runTest {
        mockWebServer.enqueue(okhttp3.mockwebserver.MockResponse().setResponseCode(200).setBody(TestFixtures.malformedJson))
        try {
            apiClient.blocklists.fetchBlocklistData("1")
            fail("Expected DecodingError")
        } catch (e: HttpClientException) {
            assertTrue("Expected DecodingError got ${e::class.simpleName}", e is HttpClientException.DecodingError)
        }
    }

    @Test
    fun `blocklists addBlocklist malformed JSON throws DecodingError`() = runTest {
        mockWebServer.enqueue(okhttp3.mockwebserver.MockResponse().setResponseCode(200).setBody(TestFixtures.malformedJson))
        try {
            val request = AddBlocklistRequest(name = "test", url = "https://example.com")
            apiClient.blocklists.addBlocklist(request)
            fail("Expected DecodingError")
        } catch (e: HttpClientException) {
            assertTrue("Expected DecodingError got ${e::class.simpleName}", e is HttpClientException.DecodingError)
        }
    }

    @Test
    fun `blocklists network error throws NetworkError`() = runTest {
        val badClient = CrowdSecApiClient(
            TestFixtures.csserverModel(
                http = "http", domain = "localhost", port = 1, path = "", authMethod = "none"
            )
        )
        try {
            badClient.blocklists.fetchBlocklists()
            fail("Expected NetworkError")
        } catch (e: HttpClientException) {
            assertTrue("Expected NetworkError got ${e::class.simpleName}", e is HttpClientException.NetworkError)
        }
    }

    // ── AllowlistsApiClient error paths ────────────────────────

    @Test
    fun `allowlists fetchAllowlists malformed JSON throws DecodingError`() = runTest {
        mockWebServer.enqueue(okhttp3.mockwebserver.MockResponse().setResponseCode(200).setBody(TestFixtures.malformedJson))
        try {
            apiClient.allowlists.fetchAllowlists()
            fail("Expected DecodingError")
        } catch (e: HttpClientException) {
            assertTrue("Expected DecodingError got ${e::class.simpleName}", e is HttpClientException.DecodingError)
        }
    }

    @Test
    fun `allowlists checkIps malformed JSON throws DecodingError`() = runTest {
        mockWebServer.enqueue(okhttp3.mockwebserver.MockResponse().setResponseCode(200).setBody(TestFixtures.malformedJson))
        try {
            apiClient.allowlists.checkIps(AllowlistsCheckIPsRequest(ips = listOf("1.2.3.4")))
            fail("Expected DecodingError")
        } catch (e: HttpClientException) {
            assertTrue("Expected DecodingError got ${e::class.simpleName}", e is HttpClientException.DecodingError)
        }
    }

    @Test
    fun `allowlists network error throws NetworkError`() = runTest {
        val badClient = CrowdSecApiClient(
            TestFixtures.csserverModel(
                http = "http", domain = "localhost", port = 1, path = "", authMethod = "none"
            )
        )
        try {
            badClient.allowlists.fetchAllowlists()
            fail("Expected NetworkError")
        } catch (e: HttpClientException) {
            assertTrue("Expected NetworkError got ${e::class.simpleName}", e is HttpClientException.NetworkError)
        }
    }

    // ── Statistics error paths ──────────────────────────────────

    @Test
    fun `statistics fetchStatistics malformed JSON throws DecodingError`() = runTest {
        mockWebServer.enqueue(okhttp3.mockwebserver.MockResponse().setResponseCode(200).setBody(TestFixtures.malformedJson))
        try {
            apiClient.statistics.fetchStatistics()
            fail("Expected DecodingError")
        } catch (e: HttpClientException) {
            assertTrue("Expected DecodingError got ${e::class.simpleName}", e is HttpClientException.DecodingError)
        }
    }

    @Test
    fun `countries statistics malformed JSON throws DecodingError`() = runTest {
        mockWebServer.enqueue(okhttp3.mockwebserver.MockResponse().setResponseCode(200).setBody(TestFixtures.malformedJson))
        try {
            apiClient.statistics.countries.fetchCountriesStatistics()
            fail("Expected DecodingError")
        } catch (e: HttpClientException) {
            assertTrue("Expected DecodingError got ${e::class.simpleName}", e is HttpClientException.DecodingError)
        }
    }

    @Test
    fun `ipOwners statistics malformed JSON throws DecodingError`() = runTest {
        mockWebServer.enqueue(okhttp3.mockwebserver.MockResponse().setResponseCode(200).setBody(TestFixtures.malformedJson))
        try {
            apiClient.statistics.ipOwners.fetchIpOwnersStatistics()
            fail("Expected DecodingError")
        } catch (e: HttpClientException) {
            assertTrue("Expected DecodingError got ${e::class.simpleName}", e is HttpClientException.DecodingError)
        }
    }

    @Test
    fun `scenarios statistics malformed JSON throws DecodingError`() = runTest {
        mockWebServer.enqueue(okhttp3.mockwebserver.MockResponse().setResponseCode(200).setBody(TestFixtures.malformedJson))
        try {
            apiClient.statistics.scenarios.fetchScenariosStatistics()
            fail("Expected DecodingError")
        } catch (e: HttpClientException) {
            assertTrue("Expected DecodingError got ${e::class.simpleName}", e is HttpClientException.DecodingError)
        }
    }

    @Test
    fun `targets statistics malformed JSON throws DecodingError`() = runTest {
        mockWebServer.enqueue(okhttp3.mockwebserver.MockResponse().setResponseCode(200).setBody(TestFixtures.malformedJson))
        try {
            apiClient.statistics.targets.fetchTargetsStatistics()
            fail("Expected DecodingError")
        } catch (e: HttpClientException) {
            assertTrue("Expected DecodingError got ${e::class.simpleName}", e is HttpClientException.DecodingError)
        }
    }

    @Test
    fun `statistics network error throws NetworkError`() = runTest {
        val badClient = CrowdSecApiClient(
            TestFixtures.csserverModel(
                http = "http", domain = "localhost", port = 1, path = "", authMethod = "none"
            )
        )
        try {
            badClient.statistics.fetchStatistics()
            fail("Expected NetworkError")
        } catch (e: HttpClientException) {
            assertTrue("Expected NetworkError got ${e::class.simpleName}", e is HttpClientException.NetworkError)
        }
    }

    // ── More DecisionsApiClient error paths ────────────────────

    @Test
    fun `decisions fetchDecisionDetails network error`() = runTest {
        val badClient = CrowdSecApiClient(
            TestFixtures.csserverModel(
                http = "http", domain = "localhost", port = 1, path = "", authMethod = "none"
            )
        )
        try {
            badClient.decisions.fetchDecisionDetails(1)
            fail("Expected NetworkError")
        } catch (e: HttpClientException) {
            assertTrue("Expected NetworkError got ${e::class.simpleName}", e is HttpClientException.NetworkError)
        }
    }

    @Test
    fun `decisions fetchDecisionsByIP malformed JSON`() = runTest {
        mockWebServer.enqueue(okhttp3.mockwebserver.MockResponse().setResponseCode(200).setBody(TestFixtures.malformedJson))
        try {
            apiClient.decisions.fetchDecisionsByIP(onlyActive = null, offset = 0, limit = 50)
            fail("Expected DecodingError")
        } catch (e: HttpClientException) {
            assertTrue("Expected DecodingError got ${e::class.simpleName}", e is HttpClientException.DecodingError)
        }
    }

    @Test
    fun `decisions fetchDecisionsByIPDetail malformed JSON`() = runTest {
        mockWebServer.enqueue(okhttp3.mockwebserver.MockResponse().setResponseCode(200).setBody(TestFixtures.malformedJson))
        try {
            apiClient.decisions.fetchDecisionsByIPDetail(ip = "1.2.3.4", onlyActive = null)
            fail("Expected DecodingError")
        } catch (e: HttpClientException) {
            assertTrue("Expected DecodingError got ${e::class.simpleName}", e is HttpClientException.DecodingError)
        }
    }

    @Test
    fun `decisions createDecision network error`() = runTest {
        val badClient = CrowdSecApiClient(
            TestFixtures.csserverModel(
                http = "http", domain = "localhost", port = 1, path = "", authMethod = "none"
            )
        )
        try {
            badClient.decisions.createDecision(
                com.jgeek00.crowdsecmonitor.data.models.CreateDecisionRequest(
                    ip = "1.2.3.4", duration = "24h", type = com.jgeek00.crowdsecmonitor.constants.Enums.DecisionType.BAN, reason = "test"
                )
            )
            fail("Expected NetworkError")
        } catch (e: HttpClientException) {
            assertTrue("Expected NetworkError got ${e::class.simpleName}", e is HttpClientException.NetworkError)
        }
    }

    @Test
    fun `decisions deleteDecision network error`() = runTest {
        val badClient = CrowdSecApiClient(
            TestFixtures.csserverModel(
                http = "http", domain = "localhost", port = 1, path = "", authMethod = "none"
            )
        )
        try {
            badClient.decisions.deleteDecision(1)
            fail("Expected NetworkError")
        } catch (e: HttpClientException) {
            assertTrue("Expected NetworkError got ${e::class.simpleName}", e is HttpClientException.NetworkError)
        }
    }

    @Test
    fun `decisions fetchDecisionDetails malformed JSON`() = runTest {
        mockWebServer.enqueue(okhttp3.mockwebserver.MockResponse().setResponseCode(200).setBody(TestFixtures.malformedJson))
        try {
            apiClient.decisions.fetchDecisionDetails(1)
            fail("Expected DecodingError")
        } catch (e: HttpClientException) {
            assertTrue("Expected DecodingError got ${e::class.simpleName}", e is HttpClientException.DecodingError)
        }
    }

    @Test
    fun `decisions fetchDecisionsByIP network error`() = runTest {
        val badClient = CrowdSecApiClient(
            TestFixtures.csserverModel(
                http = "http", domain = "localhost", port = 1, path = "", authMethod = "none"
            )
        )
        try {
            badClient.decisions.fetchDecisionsByIP(onlyActive = null, offset = 0, limit = 50)
            fail("Expected NetworkError")
        } catch (e: HttpClientException) {
            assertTrue("Expected NetworkError got ${e::class.simpleName}", e is HttpClientException.NetworkError)
        }
    }

    @Test
    fun `decisions fetchDecisionsByIPDetail network error`() = runTest {
        val badClient = CrowdSecApiClient(
            TestFixtures.csserverModel(
                http = "http", domain = "localhost", port = 1, path = "", authMethod = "none"
            )
        )
        try {
            badClient.decisions.fetchDecisionsByIPDetail(ip = "1.2.3.4", onlyActive = null)
            fail("Expected NetworkError")
        } catch (e: HttpClientException) {
            assertTrue("Expected NetworkError got ${e::class.simpleName}", e is HttpClientException.NetworkError)
        }
    }

    @Test
    fun `decisions createDecision malformed JSON`() = runTest {
        mockWebServer.enqueue(okhttp3.mockwebserver.MockResponse().setResponseCode(200).setBody(TestFixtures.malformedJson))
        try {
            apiClient.decisions.createDecision(
                com.jgeek00.crowdsecmonitor.data.models.CreateDecisionRequest(
                    ip = "1.2.3.4", duration = "24h", type = com.jgeek00.crowdsecmonitor.constants.Enums.DecisionType.BAN, reason = "test"
                )
            )
            fail("Expected DecodingError")
        } catch (e: HttpClientException) {
            assertTrue("Expected DecodingError got ${e::class.simpleName}", e is HttpClientException.DecodingError)
        }
    }

    @Test
    fun `decisions deleteDecision malformed JSON`() = runTest {
        mockWebServer.enqueue(okhttp3.mockwebserver.MockResponse().setResponseCode(200).setBody(TestFixtures.malformedJson))
        try {
            apiClient.decisions.deleteDecision(1)
            fail("Expected DecodingError")
        } catch (e: HttpClientException) {
            assertTrue("Expected DecodingError got ${e::class.simpleName}", e is HttpClientException.DecodingError)
        }
    }

    // ── More AlertsApiClient error paths ───────────────────────

    @Test
    fun `alerts fetchAlertDetails network error`() = runTest {
        val badClient = CrowdSecApiClient(
            TestFixtures.csserverModel(
                http = "http", domain = "localhost", port = 1, path = "", authMethod = "none"
            )
        )
        try {
            badClient.alerts.fetchAlertDetails(1)
            fail("Expected NetworkError")
        } catch (e: HttpClientException) {
            assertTrue("Expected NetworkError got ${e::class.simpleName}", e is HttpClientException.NetworkError)
        }
    }

    @Test
    fun `alerts deleteAlert network error`() = runTest {
        val badClient = CrowdSecApiClient(
            TestFixtures.csserverModel(
                http = "http", domain = "localhost", port = 1, path = "", authMethod = "none"
            )
        )
        try {
            badClient.alerts.deleteAlert(1)
            fail("Expected NetworkError")
        } catch (e: HttpClientException) {
            assertTrue("Expected NetworkError got ${e::class.simpleName}", e is HttpClientException.NetworkError)
        }
    }

    // ── More BlocklistsApiClient error paths ───────────────────

    @Test
    fun `blocklists fetchBlocklistData malformed JSON`() = runTest {
        mockWebServer.enqueue(okhttp3.mockwebserver.MockResponse().setResponseCode(200).setBody(TestFixtures.malformedJson))
        try {
            apiClient.blocklists.fetchBlocklistData("1")
            fail("Expected DecodingError")
        } catch (e: HttpClientException) {
            assertTrue("Expected DecodingError got ${e::class.simpleName}", e is HttpClientException.DecodingError)
        }
    }

    @Test
    fun `blocklists fetchBlocklistIps malformed JSON`() = runTest {
        mockWebServer.enqueue(okhttp3.mockwebserver.MockResponse().setResponseCode(200).setBody(TestFixtures.malformedJson))
        try {
            apiClient.blocklists.fetchBlocklistIps("1")
            fail("Expected DecodingError")
        } catch (e: HttpClientException) {
            assertTrue("Expected DecodingError got ${e::class.simpleName}", e is HttpClientException.DecodingError)
        }
    }

    @Test
    fun `blocklists deleteBlocklist malformed JSON`() = runTest {
        mockWebServer.enqueue(okhttp3.mockwebserver.MockResponse().setResponseCode(200).setBody(TestFixtures.malformedJson))
        try {
            apiClient.blocklists.deleteBlocklist("1")
            fail("Expected DecodingError")
        } catch (e: HttpClientException) {
            assertTrue("Expected DecodingError got ${e::class.simpleName}", e is HttpClientException.DecodingError)
        }
    }

    @Test
    fun `blocklists toggleBlocklist malformed JSON`() = runTest {
        mockWebServer.enqueue(okhttp3.mockwebserver.MockResponse().setResponseCode(200).setBody(TestFixtures.malformedJson))
        try {
            apiClient.blocklists.toggleBlocklist("1", com.jgeek00.crowdsecmonitor.data.models.ToggleBlocklistRequest(enabled = true))
            fail("Expected DecodingError")
        } catch (e: HttpClientException) {
            assertTrue("Expected DecodingError got ${e::class.simpleName}", e is HttpClientException.DecodingError)
        }
    }

    @Test
    fun `blocklists refreshAllBlocklists network error`() = runTest {
        val badClient = CrowdSecApiClient(
            TestFixtures.csserverModel(
                http = "http", domain = "localhost", port = 1, path = "", authMethod = "none"
            )
        )
        try {
            badClient.blocklists.refreshAllBlocklists()
            fail("Expected NetworkError")
        } catch (e: HttpClientException) {
            assertTrue("Expected NetworkError got ${e::class.simpleName}", e is HttpClientException.NetworkError)
        }
    }

    @Test
    fun `blocklists refreshBlocklist network error`() = runTest {
        val badClient = CrowdSecApiClient(
            TestFixtures.csserverModel(
                http = "http", domain = "localhost", port = 1, path = "", authMethod = "none"
            )
        )
        try {
            badClient.blocklists.refreshBlocklist("1")
            fail("Expected NetworkError")
        } catch (e: HttpClientException) {
            assertTrue("Expected NetworkError got ${e::class.simpleName}", e is HttpClientException.NetworkError)
        }
    }

    @Test
    fun `blocklists addBlocklist network error`() = runTest {
        val badClient = CrowdSecApiClient(
            TestFixtures.csserverModel(
                http = "http", domain = "localhost", port = 1, path = "", authMethod = "none"
            )
        )
        try {
            badClient.blocklists.addBlocklist(com.jgeek00.crowdsecmonitor.data.models.AddBlocklistRequest(name = "test", url = "https://example.com"))
            fail("Expected NetworkError")
        } catch (e: HttpClientException) {
            assertTrue("Expected NetworkError got ${e::class.simpleName}", e is HttpClientException.NetworkError)
        }
    }

    @Test
    fun `blocklists fetchBlocklistData network error`() = runTest {
        val badClient = CrowdSecApiClient(
            TestFixtures.csserverModel(
                http = "http", domain = "localhost", port = 1, path = "", authMethod = "none"
            )
        )
        try {
            badClient.blocklists.fetchBlocklistData("1")
            fail("Expected NetworkError")
        } catch (e: HttpClientException) {
            assertTrue("Expected NetworkError got ${e::class.simpleName}", e is HttpClientException.NetworkError)
        }
    }

    @Test
    fun `blocklists fetchBlocklistIps network error`() = runTest {
        val badClient = CrowdSecApiClient(
            TestFixtures.csserverModel(
                http = "http", domain = "localhost", port = 1, path = "", authMethod = "none"
            )
        )
        try {
            badClient.blocklists.fetchBlocklistIps("1")
            fail("Expected NetworkError")
        } catch (e: HttpClientException) {
            assertTrue("Expected NetworkError got ${e::class.simpleName}", e is HttpClientException.NetworkError)
        }
    }

    @Test
    fun `blocklists toggleBlocklist network error`() = runTest {
        val badClient = CrowdSecApiClient(
            TestFixtures.csserverModel(
                http = "http", domain = "localhost", port = 1, path = "", authMethod = "none"
            )
        )
        try {
            badClient.blocklists.toggleBlocklist("1", com.jgeek00.crowdsecmonitor.data.models.ToggleBlocklistRequest(enabled = true))
            fail("Expected NetworkError")
        } catch (e: HttpClientException) {
            assertTrue("Expected NetworkError got ${e::class.simpleName}", e is HttpClientException.NetworkError)
        }
    }

    @Test
    fun `blocklists deleteBlocklist network error`() = runTest {
        val badClient = CrowdSecApiClient(
            TestFixtures.csserverModel(
                http = "http", domain = "localhost", port = 1, path = "", authMethod = "none"
            )
        )
        try {
            badClient.blocklists.deleteBlocklist("1")
            fail("Expected NetworkError")
        } catch (e: HttpClientException) {
            assertTrue("Expected NetworkError got ${e::class.simpleName}", e is HttpClientException.NetworkError)
        }
    }

    @Test
    fun `blocklists checkIps network error`() = runTest {
        val badClient = CrowdSecApiClient(
            TestFixtures.csserverModel(
                http = "http", domain = "localhost", port = 1, path = "", authMethod = "none"
            )
        )
        try {
            badClient.blocklists.checkIps(com.jgeek00.crowdsecmonitor.data.models.BlocklistsCheckIPsRequest(ips = listOf("1.2.3.4")))
            fail("Expected NetworkError")
        } catch (e: HttpClientException) {
            assertTrue("Expected NetworkError got ${e::class.simpleName}", e is HttpClientException.NetworkError)
        }
    }

    @Test
    fun `blocklists checkDomain network error`() = runTest {
        val badClient = CrowdSecApiClient(
            TestFixtures.csserverModel(
                http = "http", domain = "localhost", port = 1, path = "", authMethod = "none"
            )
        )
        try {
            badClient.blocklists.checkDomain(com.jgeek00.crowdsecmonitor.data.models.BlocklistsCheckDomainRequest(domain = "example.com"))
            fail("Expected NetworkError")
        } catch (e: HttpClientException) {
            assertTrue("Expected NetworkError got ${e::class.simpleName}", e is HttpClientException.NetworkError)
        }
    }

    // ── More AllowlistsApiClient error paths ───────────────────

    @Test
    fun `blocklists addBlocklist malformed JSON`() = runTest {
        mockWebServer.enqueue(okhttp3.mockwebserver.MockResponse().setResponseCode(200).setBody(TestFixtures.malformedJson))
        try {
            apiClient.blocklists.addBlocklist(com.jgeek00.crowdsecmonitor.data.models.AddBlocklistRequest(name = "test", url = "https://example.com"))
            fail("Expected DecodingError")
        } catch (e: HttpClientException) {
            assertTrue("Expected DecodingError got ${e::class.simpleName}", e is HttpClientException.DecodingError)
        }
    }

    @Test
    fun `blocklists refreshAllBlocklists malformed JSON`() = runTest {
        mockWebServer.enqueue(okhttp3.mockwebserver.MockResponse().setResponseCode(200).setBody(TestFixtures.malformedJson))
        try {
            apiClient.blocklists.refreshAllBlocklists()
            fail("Expected DecodingError")
        } catch (e: HttpClientException) {
            assertTrue("Expected DecodingError got ${e::class.simpleName}", e is HttpClientException.DecodingError)
        }
    }

    @Test
    fun `blocklists refreshBlocklist malformed JSON`() = runTest {
        mockWebServer.enqueue(okhttp3.mockwebserver.MockResponse().setResponseCode(200).setBody(TestFixtures.malformedJson))
        try {
            apiClient.blocklists.refreshBlocklist("1")
            fail("Expected DecodingError")
        } catch (e: HttpClientException) {
            assertTrue("Expected DecodingError got ${e::class.simpleName}", e is HttpClientException.DecodingError)
        }
    }

    @Test
    fun `blocklists checkIps malformed JSON`() = runTest {
        mockWebServer.enqueue(okhttp3.mockwebserver.MockResponse().setResponseCode(200).setBody(TestFixtures.malformedJson))
        try {
            apiClient.blocklists.checkIps(com.jgeek00.crowdsecmonitor.data.models.BlocklistsCheckIPsRequest(ips = listOf("1.2.3.4")))
            fail("Expected DecodingError")
        } catch (e: HttpClientException) {
            assertTrue("Expected DecodingError got ${e::class.simpleName}", e is HttpClientException.DecodingError)
        }
    }

    @Test
    fun `blocklists checkDomain malformed JSON`() = runTest {
        mockWebServer.enqueue(okhttp3.mockwebserver.MockResponse().setResponseCode(200).setBody(TestFixtures.malformedJson))
        try {
            apiClient.blocklists.checkDomain(com.jgeek00.crowdsecmonitor.data.models.BlocklistsCheckDomainRequest(domain = "example.com"))
            fail("Expected DecodingError")
        } catch (e: HttpClientException) {
            assertTrue("Expected DecodingError got ${e::class.simpleName}", e is HttpClientException.DecodingError)
        }
    }

    @Test
    fun `allowlists checkIps network error`() = runTest {
        val badClient = CrowdSecApiClient(
            TestFixtures.csserverModel(
                http = "http", domain = "localhost", port = 1, path = "", authMethod = "none"
            )
        )
        try {
            badClient.allowlists.checkIps(com.jgeek00.crowdsecmonitor.data.models.AllowlistsCheckIPsRequest(ips = listOf("1.2.3.4")))
            fail("Expected NetworkError")
        } catch (e: HttpClientException) {
            assertTrue("Expected NetworkError got ${e::class.simpleName}", e is HttpClientException.NetworkError)
        }
    }
}
