package com.jgeek00.crowdsecmonitor.fixtures

import com.jgeek00.crowdsecmonitor.data.db.CSServerModel
import com.jgeek00.crowdsecmonitor.data.models.*
import java.util.UUID

/**
 * Typed fixture constructors for production data models.
 * All functions return valid populated instances with sensible defaults;
 * callers can override specific fields via named parameters.
 */
object TestFixtures {

    // ── CSServerModel ──────────────────────────────────────────

    fun csserverModel(
        id: UUID = UUID.fromString("00000000-0000-0000-0000-000000000001"),
        name: String = "Test Server",
        http: String = "http",
        domain: String = "localhost",
        port: Int? = 8080,
        path: String? = "",
        authMethod: String = "none",
        basicUser: String? = null,
        basicPassword: String? = null,
        bearerToken: String? = null,
        defaultServer: Boolean? = false,
        customHeaders: List<Pair<String, String>>? = null
    ): CSServerModel = CSServerModel(
        id = id,
        name = name,
        http = http,
        domain = domain,
        port = port,
        path = path,
        authMethod = authMethod,
        basicUser = basicUser,
        basicPassword = basicPassword,
        bearerToken = bearerToken,
        defaultServer = defaultServer,
        customHeaders = customHeaders
    )

    // ── Decisions ───────────────────────────────────────────────

    fun decisionSource(
        asName: String? = "Test AS",
        asNumber: String? = "12345",
        cn: String? = "US",
        ip: String? = "1.2.3.4",
        latitude: Double? = 40.0,
        longitude: Double? = -74.0,
        range: String? = "1.2.3.0/24",
        scope: String = "ip",
        value: String = "1.2.3.4"
    ): DecisionSource = DecisionSource(
        asName = asName, asNumber = asNumber, cn = cn, ip = ip,
        latitude = latitude, longitude = longitude, range = range,
        scope = scope, value = value
    )

    fun decisionsListResponseItem(
        id: Int = 1,
        alertId: Int = 100,
        origin: String = "crowdsec",
        type: String = "ban",
        scope: String = "ip",
        value: String = "1.2.3.4",
        expiration: String = "2026-12-31T23:59:59Z",
        scenario: String = "crowdsec/ssh-bf",
        simulated: Boolean = false,
        source: DecisionSource = decisionSource(),
        crowdsecCreatedAt: String = "2026-07-24T10:00:00Z"
    ): DecisionsListResponseItem = DecisionsListResponseItem(
        id = id, alertId = alertId, origin = origin, type = type,
        scope = scope, value = value, expiration = expiration,
        scenario = scenario, simulated = simulated, source = source,
        crowdsecCreatedAt = crowdsecCreatedAt
    )

    fun decisionsListResponse(
        items: List<DecisionsListResponseItem> = listOf(decisionsListResponseItem()),
        page: Int = 1,
        total: Int = 30,
        countries: List<String> = emptyList(),
        ipOwners: List<String> = emptyList()
    ): DecisionsListResponse = DecisionsListResponse(
        filtering = DecisionsListResponseFiltering(countries = countries, ipOwners = ipOwners),
        items = items,
        pagination = DecisionsListResponsePagination(page = page, amount = items.size, total = total)
    )

    fun decisionsByIPResponseGroup(
        ip: String = "1.2.3.4",
        country: String? = "US",
        owner: String? = "Test Owner",
        asNumber: String? = "12345",
        latitude: Double? = 40.0,
        longitude: Double? = -74.0,
        range: String? = "1.2.3.0/24",
        activeDecisions: Int = 1,
        totalDecisions: Int = 2
    ): DecisionsByIPResponseGroup = DecisionsByIPResponseGroup(
        ip = ip, country = country, owner = owner, asNumber = asNumber,
        latitude = latitude, longitude = longitude, range = range,
        activeDecisions = activeDecisions, totalDecisions = totalDecisions
    )

    fun decisionsByIPResponse(
        groups: List<DecisionsByIPResponseGroup> = listOf(decisionsByIPResponseGroup()),
        page: Int = 1,
        total: Int = 10,
        countries: List<String> = emptyList(),
        ipOwners: List<String> = emptyList()
    ): DecisionsByIPResponse = DecisionsByIPResponse(
        filtering = DecisionsListResponseFiltering(countries = countries, ipOwners = ipOwners),
        groups = groups,
        pagination = DecisionsListResponsePagination(page = page, amount = groups.size, total = total)
    )

    // ── Alerts ──────────────────────────────────────────────────

    fun alertSource(
        asName: String? = "Test AS",
        asNumber: String? = "12345",
        cn: String? = "US",
        ip: String? = "1.2.3.4",
        latitude: Double? = 40.0,
        longitude: Double? = -74.0,
        range: String? = "1.2.3.0/24",
        scope: String = "ip",
        value: String = "1.2.3.4"
    ): AlertSource = AlertSource(
        asName = asName, asNumber = asNumber, cn = cn, ip = ip,
        latitude = latitude, longitude = longitude, range = range,
        scope = scope, value = value
    )

    fun alertsListResponseAlert(
        id: Int = 1,
        uuid: String = "uuid-0001",
        scenario: String = "crowdsec/ssh-bf",
        scenarioVersion: String = "1.0",
        scenarioHash: String = "abc123",
        message: String = "SSH brute force",
        capacity: Int = 10,
        leakspeed: String = "1s",
        simulated: Boolean = false,
        remediation: Boolean = true,
        eventsCount: Int = 5,
        machineId: String = "machine-001",
        source: AlertSource = alertSource(),
        meta: List<AlertItemMeta> = emptyList(),
        events: List<AlertEvent> = emptyList(),
        crowdsecCreatedAt: String = "2026-07-24T10:00:00Z",
        startAt: String = "2026-07-24T09:00:00Z",
        stopAt: String = "2026-07-24T10:00:00Z"
    ): AlertsListResponseAlert = AlertsListResponseAlert(
        id = id, uuid = uuid, scenario = scenario,
        scenarioVersion = scenarioVersion, scenarioHash = scenarioHash,
        message = message, capacity = capacity, leakspeed = leakspeed,
        simulated = simulated, remediation = remediation,
        eventsCount = eventsCount, machineId = machineId,
        source = source, meta = meta, events = events,
        crowdsecCreatedAt = crowdsecCreatedAt,
        startAt = startAt, stopAt = stopAt
    )

    fun alertsListResponse(
        items: List<AlertsListResponseAlert> = listOf(alertsListResponseAlert()),
        page: Int = 1,
        total: Int = 30,
        countries: List<String> = emptyList(),
        scenarios: List<String> = emptyList(),
        ipOwners: List<String> = emptyList(),
        targets: List<String> = emptyList()
    ): AlertsListResponse = AlertsListResponse(
        filtering = AlertsListResponseFiltering(
            countries = countries, scenarios = scenarios,
            ipOwners = ipOwners, targets = targets
        ),
        items = items,
        pagination = AlertsListResponsePagination(page = page, amount = items.size, total = total)
    )

    // ── API Status ──────────────────────────────────────────────

    fun apiStatusResponse(
        lapiConnected: Boolean = true,
        lastSuccessfulSync: String = "2026-07-24T10:00:00Z",
        timestamp: String = "2026-07-24T10:00:00Z",
        version: String = "1.0.0",
        newVersionAvailable: String? = null,
        csBouncerAvailable: Boolean = true,
        processes: List<ApiStatusResponseProcess> = emptyList()
    ): ApiStatusResponse = ApiStatusResponse(
        csLapi = ApiStatusResponseCSLapi(
            lapiConnected = lapiConnected,
            lastSuccessfulSync = lastSuccessfulSync,
            timestamp = timestamp
        ),
        csBouncer = ApiStatusResponseCSBouncer(available = csBouncerAvailable),
        csMonitorApi = ApiStatusResponseCSMonitorApi(
            version = version,
            newVersionAvailable = newVersionAvailable
        ),
        processes = processes
    )

    // ── Blocklists ──────────────────────────────────────────────

    fun blocklistsListResponseItem(
        id: String = "1",
        url: String? = "https://example.com/blocklist.txt",
        name: String = "Test Blocklist",
        enabled: Boolean? = true,
        addedDate: String? = "2026-07-24T10:00:00Z",
        lastRefreshAttempt: String? = "2026-07-24T10:00:00Z",
        lastSuccessfulRefresh: String? = "2026-07-24T10:00:00Z",
        lastRefreshFailed: Boolean? = false,
        countIps: Int = 100,
        type: BlocklistType = BlocklistType.API
    ): BlocklistsListResponseItem = BlocklistsListResponseItem(
        id = id, url = url, name = name, enabled = enabled,
        addedDate = addedDate, lastRefreshAttempt = lastRefreshAttempt,
        lastSuccessfulRefresh = lastSuccessfulRefresh,
        lastRefreshFailed = lastRefreshFailed,
        countIps = countIps, type = type
    )

    fun blocklistsListResponse(
        items: List<BlocklistsListResponseItem> = listOf(blocklistsListResponseItem()),
        page: Int = 1,
        total: Int = 10
    ): BlocklistsListResponse = BlocklistsListResponse(
        items = items,
        pagination = BlocklistsListResponsePagination(page = page, amount = items.size, total = total)
    )

    // ── Allowlists ──────────────────────────────────────────────

    fun allowlistsListResponseAllowlistItem(
        createdAt: String = "2026-07-24T10:00:00Z",
        expiration: String? = null,
        value: String = "1.2.3.4"
    ): AllowlistsListResponseAllowlistItem = AllowlistsListResponseAllowlistItem(
        createdAt = createdAt, expiration = expiration, value = value
    )

    fun allowlistsListResponseAllowlist(
        name: String = "Test Allowlist",
        description: String = "A test allowlist",
        items: List<AllowlistsListResponseAllowlistItem> = listOf(
            allowlistsListResponseAllowlistItem()
        ),
        createdAt: String = "2026-07-24T10:00:00Z",
        updatedAt: String = "2026-07-24T10:00:00Z"
    ): AllowlistsListResponseAllowlist = AllowlistsListResponseAllowlist(
        createdAt = createdAt, description = description,
        items = items, name = name, updatedAt = updatedAt
    )

    fun allowlistsListResponse(
        data: List<AllowlistsListResponseAllowlist> = listOf(allowlistsListResponseAllowlist()),
        length: Int = 1
    ): AllowlistsListResponse = AllowlistsListResponse(data = data, length = length)

    // ── API Error ───────────────────────────────────────────────

    fun apiErrorResponse(
        message: String? = null,
        errors: List<String>? = null
    ): ApiErrorResponse = ApiErrorResponse(message = message, errors = errors)

    // ── Statistics ──────────────────────────────────────────────

    fun statisticsResponse(
        alertsLast24Hours: Int = 150,
        activeDecisions: Int = 500,
        activityHistory: List<ActivityHistory> = listOf(
            ActivityHistory(date = "2026-07-24", amountAlerts = 150, amountDecisions = 500),
            ActivityHistory(date = "2026-07-23", amountAlerts = 120, amountDecisions = 450)
        ),
        topCountries: List<TopCountry> = listOf(
            TopCountry(countryCode = "US", amount = 200),
            TopCountry(countryCode = "CN", amount = 100)
        ),
        topScenarios: List<TopScenario> = listOf(
            TopScenario(scenario = "crowdsec/ssh-bf", amount = 300)
        ),
        topIpOwners: List<TopIpOwner> = listOf(
            TopIpOwner(ipOwner = "AS12345", amount = 400)
        ),
        topTargets: List<TopTarget> = listOf(
            TopTarget(target = "1.2.3.4", amount = 500)
        )
    ): StatisticsResponse = StatisticsResponse(
        alertsLast24Hours = alertsLast24Hours,
        activeDecisions = activeDecisions,
        activityHistory = activityHistory,
        topCountries = topCountries,
        topScenarios = topScenarios,
        topIpOwners = topIpOwners,
        topTargets = topTargets
    )

    // ── HttpResponse / LoadingResult helpers ────────────────────

    fun <T> successResponse(body: T): HttpResponse<T> =
        HttpResponse(successful = true, statusCode = 200, body = body)

    fun <T> errorResponse(statusCode: Int = 500, body: T): HttpResponse<T> =
        HttpResponse(successful = false, statusCode = statusCode, body = body)

    // ── JSON string constants for MockWebServer ─────────────────

    val decisionsListJson: String get() = """{
        "filtering": { "countries": [], "ipOwners": [] },
        "items": [
            {
                "id": 1, "alert_id": 100, "origin": "crowdsec",
                "type": "ban", "scope": "ip", "value": "1.2.3.4",
                "expiration": "2026-12-31T23:59:59Z",
                "scenario": "crowdsec/ssh-bf", "simulated": false,
                "source": {
                    "as_name": "Test AS", "as_number": "12345", "cn": "US",
                    "ip": "1.2.3.4", "latitude": 40.0, "longitude": -74.0,
                    "range": "1.2.3.0/24", "scope": "ip", "value": "1.2.3.4"
                },
                "crowdsec_created_at": "2026-07-24T10:00:00Z"
            }
        ],
        "pagination": { "page": 1, "amount": 1, "total": 30 }
    }"""

    val decisionsByIPJson: String get() = """{
        "filtering": { "countries": [], "ipOwners": [] },
        "groups": [
            {
                "ip": "1.2.3.4", "country": "US", "owner": "Test Owner",
                "as_number": "12345", "latitude": 40.0, "longitude": -74.0,
                "range": "1.2.3.0/24",
                "active_decisions": 1, "total_decisions": 2
            }
        ],
        "pagination": { "page": 1, "amount": 1, "total": 10 }
    }"""

    val alertsListJson: String get() = """{
        "filtering": { "countries": [], "scenarios": [], "ipOwners": [], "targets": [] },
        "items": [
            {
                "id": 1, "uuid": "uuid-0001",
                "scenario": "crowdsec/ssh-bf",
                "scenario_version": "1.0",
                "scenario_hash": "abc123",
                "message": "SSH brute force",
                "capacity": 10, "leakspeed": "1s",
                "simulated": false, "remediation": true,
                "events_count": 5,
                "machine_id": "machine-001",
                "source": {
                    "as_name": "Test AS", "as_number": "12345", "cn": "US",
                    "ip": "1.2.3.4", "latitude": 40.0, "longitude": -74.0,
                    "range": "1.2.3.0/24", "scope": "ip", "value": "1.2.3.4"
                },
                "meta": [], "events": [],
                "crowdsec_created_at": "2026-07-24T10:00:00Z",
                "start_at": "2026-07-24T09:00:00Z",
                "stop_at": "2026-07-24T10:00:00Z"
            }
        ],
        "pagination": { "page": 1, "amount": 1, "total": 30 }
    }"""

    val apiStatusJson: String get() = """{
        "csLapi": {
            "lapiConnected": true,
            "lastSuccessfulSync": "2026-07-24T10:00:00Z",
            "timestamp": "2026-07-24T10:00:00Z"
        },
        "csBouncer": { "available": true },
        "csMonitorApi": { "version": "1.0.0" },
        "processes": []
    }"""

    val apiErrorJson: String get() = """{ "message": "Invalid request" }"""

    val apiErrorWithErrorsJson: String get() = """{ "errors": ["Field required", "Invalid value"] }"""

    val apiErrorEmptyJson: String get() = """{}"""

    val malformedJson: String get() = """{ "invalid": }"""

    // ── Additional fixture JSON strings for API tests ────────────

    val decisionItemJson: String get() = """{
        "id": 1, "alert_id": 100, "origin": "crowdsec",
        "type": "ban", "scope": "ip", "value": "1.2.3.4",
        "expiration": "2026-12-31T23:59:59Z",
        "scenario": "crowdsec/ssh-bf", "simulated": false,
        "source": {
            "as_name": "Test AS", "as_number": "12345", "cn": "US",
            "ip": "1.2.3.4", "latitude": 40.0, "longitude": -74.0,
            "range": "1.2.3.0/24", "scope": "ip", "value": "1.2.3.4"
        },
        "crowdsec_created_at": "2026-07-24T10:00:00Z",
        "alert": {
            "id": 1, "uuid": "uuid-0001",
            "scenario": "crowdsec/ssh-bf",
            "scenario_version": "1.0",
            "scenario_hash": "abc123",
            "message": "SSH brute force",
            "capacity": 10, "leakspeed": "1s",
            "simulated": false, "remediation": true,
            "events_count": 5,
            "machine_id": "machine-001",
            "source": {
                "as_name": "Test AS", "as_number": "12345", "cn": "US",
                "ip": "1.2.3.4", "latitude": 40.0, "longitude": -74.0,
                "range": "1.2.3.0/24", "scope": "ip", "value": "1.2.3.4"
            },
            "meta": [], "events": [],
            "crowdsec_created_at": "2026-07-24T10:00:00Z",
            "start_at": "2026-07-24T09:00:00Z",
            "stop_at": "2026-07-24T10:00:00Z"
        }
    }"""

    val emptyResponseJson: String get() = """{}"""

    val decisionsByIPDetailJson: String get() = """{
        "ip": "1.2.3.4", "country": "US", "owner": "Test Owner",
        "as_number": "12345", "latitude": 40.0, "longitude": -74.0,
        "range": "1.2.3.0/24", "active_decisions": 1, "total_decisions": 2,
        "decisions": [{
            "id": 1, "alert_id": 100, "origin": "crowdsec",
            "type": "ban", "scope": "ip", "value": "1.2.3.4",
            "expiration": "2026-12-31T23:59:59Z",
            "scenario": "crowdsec/ssh-bf", "simulated": false,
            "crowdsec_created_at": "2026-07-24T10:00:00Z"
        }]
    }"""

    val alertDetailsJson: String get() = """{
        "id": 1, "uuid": "uuid-0001",
        "scenario": "crowdsec/ssh-bf",
        "scenario_version": "1.0",
        "scenario_hash": "abc123",
        "message": "SSH brute force",
        "capacity": 10, "leakspeed": "1s",
        "simulated": false, "remediation": true,
        "events_count": 5,
        "machine_id": "machine-001",
        "source": {
            "as_name": "Test AS", "as_number": "12345", "cn": "US",
            "ip": "1.2.3.4", "latitude": 40.0, "longitude": -74.0,
            "range": "1.2.3.0/24", "scope": "ip", "value": "1.2.3.4"
        },
        "meta": [], "events": [],
        "crowdsec_created_at": "2026-07-24T10:00:00Z",
        "start_at": "2026-07-24T09:00:00Z",
        "stop_at": "2026-07-24T10:00:00Z",
        "decisions": []
    }"""

    val allowlistsListJson: String get() = """{
        "data": [{
            "name": "Test Allowlist",
            "description": "A test allowlist",
            "items": [{
                "created_at": "2026-07-24T10:00:00Z",
                "value": "1.2.3.4"
            }],
            "created_at": "2026-07-24T10:00:00Z",
            "updated_at": "2026-07-24T10:00:00Z"
        }],
        "length": 1
    }"""

    val allowlistsCheckIPsJson: String get() = """{
        "results": [{"ip": "1.2.3.4", "allowlist": "test-allowlist"}]
    }"""

    val blocklistsListJson: String get() = """{
        "items": [{
            "id": "1",
            "url": "https://example.com/blocklist.txt",
            "name": "Test Blocklist",
            "enabled": true,
            "added_date": "2026-07-24T10:00:00Z",
            "last_refresh_attempt": "2026-07-24T10:00:00Z",
            "last_successful_refresh": "2026-07-24T10:00:00Z",
            "last_refresh_failed": false,
            "count_ips": 100,
            "type": "api"
        }],
        "pagination": { "page": 1, "amount": 1, "total": 10 }
    }"""

    val blocklistDataJson: String get() = """{
        "data": {
            "id": "1", "url": "https://example.com/blocklist.txt",
            "name": "Test Blocklist", "enabled": true,
            "added_date": "2026-07-24T10:00:00Z",
            "last_refresh_attempt": "2026-07-24T10:00:00Z",
            "last_successful_refresh": "2026-07-24T10:00:00Z",
            "last_refresh_failed": false,
            "count_ips": 100, "type": "api",
            "blocklistIps": ["1.2.3.4"]
        }
    }"""

    val blocklistIpsJson: String get() = """{
        "data": ["1.2.3.4", "5.6.7.8"],
        "total": 2,
        "limit": 100,
        "offset": 0
    }"""

    val refreshBlocklistsJson: String get() = """{
        "message": "Refresh completed"
    }"""

    val blocklistsCheckIPsJson: String get() = """{
        "results": [{"ip": "1.2.3.4", "blocklists": ["1"]}]
    }"""

    val blocklistsCheckDomainJson: String get() = """{
        "domain": "example.com",
        "ips": [{"ip": "1.2.3.4", "blocklists": ["1"]}]
    }"""

    val statisticsResponseJson: String get() = """{
        "alertsLast24Hours": 150,
        "activeDecisions": 500,
        "activityHistory": [
            { "date": "2026-07-24", "amountAlerts": 150, "amountDecisions": 500 },
            { "date": "2026-07-23", "amountAlerts": 120, "amountDecisions": 450 }
        ],
        "topCountries": [
            { "countryCode": "US", "amount": 200 },
            { "countryCode": "CN", "amount": 100 }
        ],
        "topScenarios": [
            { "scenario": "crowdsec/ssh-bf", "amount": 300 }
        ],
        "topIpOwners": [
            { "ipOwner": "AS12345", "amount": 400 }
        ],
        "topTargets": [
            { "target": "1.2.3.4", "amount": 500 }
        ]
    }"""

    val topCountriesJson: String get() = """[
        { "countryCode": "US", "amount": 200 },
        { "countryCode": "CN", "amount": 100 }
    ]"""

    val topIpOwnersJson: String get() = """[
        { "ipOwner": "AS12345", "amount": 400 }
    ]"""

    val topScenariosJson: String get() = """[
        { "scenario": "crowdsec/ssh-bf", "amount": 300 }
    ]"""

    val topTargetsJson: String get() = """[
        { "target": "1.2.3.4", "amount": 500 }
    ]"""

    val statusResponseJson: String get() = """{
        "success": true,
        "message": "API is running"
    }"""
}
