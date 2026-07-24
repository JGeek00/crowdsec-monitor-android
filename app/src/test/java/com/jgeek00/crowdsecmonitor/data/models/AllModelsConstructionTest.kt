package com.jgeek00.crowdsecmonitor.data.models

import com.jgeek00.crowdsecmonitor.constants.Enums
import com.jgeek00.crowdsecmonitor.data.db.CSServerModel
import java.io.IOException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AllModelsConstructionTest {

    @Test
    fun `network models`() {
        val httpResponse = HttpResponse(successful = true, statusCode = 200, body = "ok")
        assertTrue(httpResponse.successful)

        val apiError = ApiErrorResponse(message = "err", errors = listOf("e1"))
        assertEquals("err", apiError.resolvedMessage)

        val empty = EmptyResponse()
        assertNotNull(empty)

        val loading = LoadingResult.Loading
        val success = LoadingResult.Success(42)
        val failure = LoadingResult.Failure(RuntimeException("boom"))
        assertTrue(loading.isLoading)
        assertEquals(42, success.data)
        assertNotNull(failure.error)

        val invalidResponse = HttpClientException.InvalidResponse()
        val unauthorized = HttpClientException.Unauthorized()
        val invalidConn = HttpClientException.InvalidConnectionValues()
        val httpError = HttpClientException.HttpError(statusCode = 500)
        val httpErrorMsg = HttpClientException.HttpErrorWithMessage(statusCode = 404, message = "not found")
        val networkError = HttpClientException.NetworkError(throwable = IOException("timeout"))
        val decodingError = HttpClientException.DecodingError(throwable = RuntimeException("parse fail"))
        assertNotNull(invalidResponse)
        assertNotNull(unauthorized)
        assertNotNull(invalidConn)
        assertNotNull(httpError)
        assertNotNull(httpErrorMsg)
        assertNotNull(networkError)
        assertNotNull(decodingError)
    }

    @Test
    fun `alert list response models`() {
        val filtering = AlertsListResponseFiltering(
            countries = listOf("US"), scenarios = listOf("s1"), ipOwners = listOf("o1"), targets = listOf("t1")
        )
        val source = AlertSource(scope = "ip", value = "1.2.3.4", cn = "US", ip = "1.2.3.4")
        val meta = AlertItemMeta(key = "k", value = listOf("v"))
        val eventMeta = AlertEventMeta(key = "k", value = listOf("v"))
        val event = AlertEvent(meta = listOf(eventMeta), timestamp = "2024-01-01T00:00:00Z")
        val alert = AlertsListResponseAlert(
            id = 1, uuid = "u1", scenario = "s1", scenarioVersion = "1.0",
            scenarioHash = "h1", message = "m1", capacity = 10, leakspeed = "1s",
            simulated = false, remediation = true, eventsCount = 1, machineId = "m1",
            source = source, meta = listOf(meta), events = listOf(event),
            crowdsecCreatedAt = "2024-01-01T00:00:00Z",
            startAt = "2024-01-01T00:00:00Z", stopAt = "2024-01-01T01:00:00Z"
        )
        val pagination = AlertsListResponsePagination(page = 1, amount = 1, total = 50)
        val response = AlertsListResponse(filtering = filtering, items = listOf(alert), pagination = pagination)
        assertEquals("s1", response.items[0].scenario)
    }

    @Test
    fun `alert details response models`() {
        val source = AlertSource(scope = "ip", value = "10.0.0.1")
        val meta = AlertDetailsMeta(key = "k", value = listOf("v"))
        val decision = AlertDetailsDecision(
            id = 1, alertId = 1, origin = "o", type = "ban", scope = "ip",
            value = "10.0.0.1", expiration = "24h", scenario = "s1",
            simulated = false, source = source, crowdsecCreatedAt = "2024-01-01T00:00:00Z"
        )
        val eventMeta = AlertDetailsEventMeta(key = "k", value = listOf("v"))
        val event = AlertDetailsEvent(meta = listOf(eventMeta), timestamp = "2024-01-01T00:00:00Z")
        val response = AlertDetailsResponse(
            id = 1, uuid = "u1", scenario = "s1", scenarioVersion = "1.0",
            scenarioHash = "h1", message = "m1", capacity = 10, leakspeed = "1s",
            simulated = false, remediation = true, eventsCount = 1, machineId = "m1",
            source = source, meta = listOf(meta), events = listOf(event),
            crowdsecCreatedAt = "2024-01-01T00:00:00Z",
            startAt = "2024-01-01T00:00:00Z", stopAt = "2024-01-01T01:00:00Z",
            decisions = listOf(decision)
        )
        assertEquals("s1", response.scenario)
    }

    @Test
    fun `decisions list response models`() {
        val filtering = DecisionsListResponseFiltering(countries = listOf("US"), ipOwners = listOf("o1"))
        val source = DecisionSource(scope = "ip", value = "10.0.0.1")
        val item = DecisionsListResponseItem(
            id = 1, alertId = 1, origin = "o", type = "ban", scope = "ip",
            value = "10.0.0.1", expiration = "24h", scenario = "s1",
            simulated = false, source = source, crowdsecCreatedAt = "2024-01-01T00:00:00Z"
        )
        val pagination = DecisionsListResponsePagination(page = 1, amount = 1, total = 50)
        val response = DecisionsListResponse(filtering = filtering, items = listOf(item), pagination = pagination)
        assertEquals("s1", response.items[0].scenario)
    }

    @Test
    fun `decisions by IP response models`() {
        val filtering = DecisionsListResponseFiltering(countries = emptyList(), ipOwners = emptyList())
        val group = DecisionsByIPResponseGroup(
            ip = "10.0.0.1", activeDecisions = 1, totalDecisions = 2, country = "US"
        )
        val pagination = DecisionsListResponsePagination(page = 1, amount = 1, total = 10)
        val response = DecisionsByIPResponse(filtering = filtering, groups = listOf(group), pagination = pagination)
        assertEquals("10.0.0.1", response.groups[0].ip)
    }

    @Test
    fun `decision item response models`() {
        val source = DecisionSource(scope = "ip", value = "10.0.0.1")
        val meta = DecisionItemMeta(key = "k", value = listOf("v"))
        val event = DecisionItemEvent(meta = listOf(meta), timestamp = "2024-01-01T00:00:00Z")
        val alert = DecisionItemAlert(
            id = 1, uuid = "u1", scenario = "s1", scenarioVersion = "1.0",
            scenarioHash = "h1", message = "m1", capacity = 10, leakspeed = "1s",
            simulated = false, remediation = true, eventsCount = 1, machineId = "m1",
            source = source, meta = listOf(meta), events = listOf(event),
            crowdsecCreatedAt = "2024-01-01T00:00:00Z",
            startAt = "2024-01-01T00:00:00Z", stopAt = "2024-01-01T01:00:00Z"
        )
        val response = DecisionItemResponse(
            id = 1, alertId = 1, origin = "o", type = "ban", scope = "ip",
            value = "10.0.0.1", expiration = "24h", scenario = "s1",
            simulated = false, source = source, crowdsecCreatedAt = "2024-01-01T00:00:00Z",
            alert = alert
        )
        assertEquals("s1", response.scenario)
    }

    @Test
    fun `statistics models`() {
        val activity = ActivityHistory(date = "2024-01-01", amountAlerts = 5, amountDecisions = 3)
        val country = TopCountry(countryCode = "US", amount = 10)
        val ipOwner = TopIpOwner(ipOwner = "owner1", amount = 7)
        val scenario = TopScenario(scenario = "s1", amount = 20)
        val target = TopTarget(target = "t1", amount = 2)
        val response = StatisticsResponse(
            alertsLast24Hours = 10, activeDecisions = 50,
            activityHistory = listOf(activity), topCountries = listOf(country),
            topScenarios = listOf(scenario), topIpOwners = listOf(ipOwner),
            topTargets = listOf(target)
        )
        assertEquals(10, response.alertsLast24Hours)
    }

    @Test
    fun `api status response models`() {
        val stepProgress = ApiStatusResponseProcessBlocklistProgress(totalIps = 100, processedIps = 50)
        val refreshBlocklist = ApiStatusResponseProcessBlocklistRefreshBlocklist(
            number = 1, name = "bl1",
            steps = ApiStatusResponseProcessBlocklistRefreshBlocklistSteps(
                fetch = ApiStatusResponseProcessBlocklistFieldStatus.SUCCESSFUL,
                parse = ApiStatusResponseProcessBlocklistFieldStatus.SUCCESSFUL,
                delete = ApiStatusResponseProcessBlocklistFieldStatus.SUCCESSFUL,
                imported = ApiStatusResponseProcessBlocklistFieldStatus.SUCCESSFUL
            )
        )
        val process = ApiStatusResponseProcess(
            id = "p1", beginDatetime = "2024-01-01T00:00:00Z",
            blocklistImport = ApiStatusResponseProcessBlocklist(
                blocklistId = 1, blocklistName = "bl1",
                step = ApiStatusResponseProcessBlocklistStep.FETCH,
                fetched = ApiStatusResponseProcessBlocklistFieldStatus.SUCCESSFUL,
                parsed = ApiStatusResponseProcessBlocklistFieldStatus.SUCCESSFUL,
                imported = ApiStatusResponseProcessBlocklistFieldStatus.SUCCESSFUL,
                processIps = stepProgress
            ),
            blocklistRefresh = ApiStatusResponseProcessBlocklistRefresh(
                totalBlocklists = 2, currentBlocklist = 1,
                blocklists = listOf(refreshBlocklist), totalIps = 200
            ),
            blocklistSingleRefresh = ApiStatusResponseProcessBlocklistSingleRefresh(
                blocklistId = 1, blocklistName = "bl1",
                step = ApiStatusResponseProcessBlocklistStep.FETCH,
                fetched = ApiStatusResponseProcessBlocklistFieldStatus.SUCCESSFUL,
                parsed = ApiStatusResponseProcessBlocklistFieldStatus.SUCCESSFUL,
                deleted = ApiStatusResponseProcessBlocklistFieldStatus.SUCCESSFUL,
                imported = ApiStatusResponseProcessBlocklistFieldStatus.SUCCESSFUL,
                processIps = stepProgress
            ),
            blocklistEnable = ApiStatusResponseProcessBlocklist(
                blocklistId = 2, blocklistName = "bl2",
                step = ApiStatusResponseProcessBlocklistStep.IMPORT,
                fetched = ApiStatusResponseProcessBlocklistFieldStatus.PENDING,
                parsed = ApiStatusResponseProcessBlocklistFieldStatus.PENDING,
                imported = ApiStatusResponseProcessBlocklistFieldStatus.PENDING,
                processIps = stepProgress
            ),
            blocklistDisable = ApiStatusResponseProcessBlocklistIps(
                blocklistId = 3, blocklistName = "bl3",
                blocklistIps = 100, ipsToDelete = 50, processedIps = 25
            ),
            blocklistDelete = ApiStatusResponseProcessBlocklistIps(
                blocklistId = 4, blocklistName = "bl4",
                blocklistIps = 200, ipsToDelete = 200, processedIps = 200
            )
        )
        val response = ApiStatusResponse(
            csLapi = ApiStatusResponseCSLapi(
                lapiConnected = true, lastSuccessfulSync = "2024-01-01T00:00:00Z",
                timestamp = "2024-01-01T00:00:00Z"
            ),
            csBouncer = ApiStatusResponseCSBouncer(available = true),
            csMonitorApi = ApiStatusResponseCSMonitorApi(version = "1.0.0"),
            processes = listOf(process)
        )
        assertTrue(response.csLapi.lapiConnected)
    }

    @Test
    fun `enum values construction`() {
        assertEquals("ban", Enums.DecisionType.BAN.value)
        assertEquals("captcha", Enums.DecisionType.CAPTCHA.value)
        assertEquals("http", Enums.ConnectionMethod.HTTP.value)
        assertEquals("https", Enums.ConnectionMethod.HTTPS.value)
        assertEquals("none", Enums.AuthMethod.NONE.value)
        assertEquals("basic", Enums.AuthMethod.BASIC.value)
        assertEquals("bearer", Enums.AuthMethod.BEARER.value)
        assertEquals(3, Enums.ThemeMode.values().size)
        assertEquals(2, Enums.DashboardBoxSummaryType.values().size)
        assertEquals(4, Enums.DashboardItemType.values().size)
        assertEquals(3, Enums.SectionHeaderPaddingTop.values().size)
        assertEquals(2, Enums.ListType.values().size)
    }

    @Test
    fun `blocklist data response models`() {
        val data = BlocklistDataResponseData(
            id = "1", name = "bl1", countIps = 10, type = BlocklistType.API,
            url = "https://example.com", enabled = true, addedDate = "2024-01-01",
            blocklistIps = listOf("10.0.0.1", "10.0.0.2")
        )
        val response = BlocklistDataResponse(data = data)
        assertEquals("bl1", response.data.name)
    }

    @Test
    fun `blocklists list response models`() {
        val item = BlocklistsListResponseItem(
            id = "1", name = "bl1", countIps = 10, type = BlocklistType.API
        )
        val pagination = BlocklistsListResponsePagination(page = 1, amount = 1, total = 5)
        val response = BlocklistsListResponse(items = listOf(item), pagination = pagination)
        assertEquals("bl1", response.items[0].name)
    }

    @Test
    fun `blocklist type enum`() {
        assertEquals(BlocklistType.API, BlocklistType.valueOf("API"))
        assertEquals(BlocklistType.CROWDSEC, BlocklistType.valueOf("CROWDSEC"))
    }

    @Test
    fun `blocklist check domain response models`() {
        val ipResult = BlocklistsCheckDomainResponseIp(ip = "10.0.0.1", blocklists = listOf("bl1"))
        val response = BlocklistsCheckDomainResponse(domain = "example.com", ips = listOf(ipResult))
        assertEquals("example.com", response.domain)
    }

    @Test
    fun `blocklist check IPs response models`() {
        val result = BlocklistsCheckIPsResponseResult(ip = "10.0.0.1", blocklists = listOf("bl1"))
        val response = BlocklistsCheckIPsResponse(results = listOf(result))
        assertEquals("10.0.0.1", response.results[0].ip)
    }

    @Test
    fun `allowlist check IPs response models`() {
        val result = AllowlistsCheckIPsResponseResult(ip = "10.0.0.1", allowlist = "wl1")
        val response = AllowlistsCheckIPsResponse(results = listOf(result))
        assertEquals("wl1", response.results[0].allowlist)
    }

    @Test
    fun `allowlists list response models`() {
        val item = AllowlistsListResponseAllowlistItem(
            createdAt = "2024-01-01T00:00:00Z", value = "10.0.0.1", expiration = "24h"
        )
        val allowlist = AllowlistsListResponseAllowlist(
            createdAt = "2024-01-01T00:00:00Z", description = "desc",
            items = listOf(item), name = "wl1", updatedAt = "2024-01-02T00:00:00Z"
        )
        val response = AllowlistsListResponse(data = listOf(allowlist), length = 1)
        assertEquals("wl1", response.data[0].name)
    }

    @Test
    fun `request models`() {
        val alertsReq = AlertsRequest(
            filters = AlertsRequestFilters(countries = listOf("US")),
            pagination = AlertsRequestPagination(offset = 0, limit = 20)
        )
        assertEquals(0, alertsReq.pagination.offset)

        val decisionsReq = DecisionsRequest(
            filters = DecisionsRequestFilters(onlyActive = true, groupByIP = false),
            pagination = DecisionsRequestPagination(offset = 0, limit = 20)
        )
        assertEquals(true, decisionsReq.filters.onlyActive)

        val blocklistsReq = BlocklistsRequest(offset = 0, limit = 10)
        assertEquals(10, blocklistsReq.limit)
    }

    @Test
    fun `create decision request`() {
        val req = CreateDecisionRequest(
            ip = "10.0.0.1", duration = "24h",
            type = Enums.DecisionType.BAN, reason = "ssh brute force"
        )
        assertEquals("10.0.0.1", req.ip)
    }

    @Test
    fun `add blocklist request`() {
        val req = AddBlocklistRequest(url = "https://example.com", name = "bl1", type = "api")
        assertEquals("bl1", req.name)
    }

    @Test
    fun `toggle blocklist request`() {
        val req = ToggleBlocklistRequest(enabled = true)
        assertTrue(req.enabled)
    }

    @Test
    fun `check IPs and domain request models`() {
        val blocklistCheckIPs = BlocklistsCheckIPsRequest(ips = listOf("10.0.0.1"))
        assertEquals(1, blocklistCheckIPs.ips.size)

        val blocklistCheckDomain = BlocklistsCheckDomainRequest(domain = "example.com")
        assertEquals("example.com", blocklistCheckDomain.domain)

        val allowlistCheckIPs = AllowlistsCheckIPsRequest(ips = listOf("10.0.0.1"))
        assertEquals(1, allowlistCheckIPs.ips.size)
    }

    @Test
    fun `refresh blocklists response`() {
        val response = RefreshBlocklistsResponse(message = "refresh started")
        assertEquals("refresh started", response.message)
    }

    @Test
    fun `CSServerModel model`() {
        val model = CSServerModel(
            name = "My Server", http = "https", domain = "api.example.com",
            path = "/api", port = 8080, authMethod = "bearer",
            basicUser = null, basicPassword = null,
            bearerToken = "tok123", defaultServer = true
        )
        assertEquals("My Server", model.name)
    }

    @Test
    fun `blocklist IPs response`() {
        val response = BlocklistIpsResponse(
            data = listOf("10.0.0.1", "10.0.0.2"), total = 2, limit = 50, offset = 0
        )
        assertEquals(2, response.total)
    }

    @Test
    fun `decisions by IP detail response`() {
        val decision = DecisionsByIPDetailResponseDecision(
            id = 1, alertId = 1, origin = "o", type = "ban", scope = "ip",
            value = "10.0.0.1", expiration = "24h", scenario = "s1",
            simulated = false, crowdsecCreatedAt = "2024-01-01T00:00:00Z"
        )
        val response = DecisionsByIPDetailResponse(
            ip = "10.0.0.1", activeDecisions = 1, totalDecisions = 2,
            decisions = listOf(decision), country = "US", owner = "owner1"
        )
        assertEquals("s1", response.decisions[0].scenario)
    }

    @Test
    fun `api status process enum values`() {
        assertTrue(ApiStatusResponseProcessBlocklistFieldStatus.values().isNotEmpty())
        assertTrue(ApiStatusResponseProcessBlocklistStep.values().isNotEmpty())
    }
}
