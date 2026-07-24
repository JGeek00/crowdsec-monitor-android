package com.jgeek00.crowdsecmonitor.fixtures

// Top-level JSON string constants for MockWebServer tests.
// These delegate to TestFixtures for source-of-truth.

val testDecisionsListResponse: String get() = TestFixtures.decisionsListJson
val testDecisionItemResponse: String get() = TestFixtures.decisionItemJson
val testDecisionsByIPResponse: String get() = TestFixtures.decisionsByIPJson
val testDecisionsByIPDetailResponse: String get() = TestFixtures.decisionsByIPDetailJson
val testEmptyResponse: String get() = TestFixtures.emptyResponseJson
val testAlertsListResponse: String get() = TestFixtures.alertsListJson
val testAlertDetailsResponse: String get() = TestFixtures.alertDetailsJson
val testAllowlistsListResponse: String get() = TestFixtures.allowlistsListJson
val testAllowlistsCheckIPsResponse: String get() = TestFixtures.allowlistsCheckIPsJson
val testBlocklistsListResponse: String get() = TestFixtures.blocklistsListJson
val testBlocklistDataResponse: String get() = TestFixtures.blocklistDataJson
val testBlocklistIpsResponse: String get() = TestFixtures.blocklistIpsJson
val testRefreshBlocklistsResponse: String get() = TestFixtures.refreshBlocklistsJson
val testBlocklistsCheckIPsResponse: String get() = TestFixtures.blocklistsCheckIPsJson
val testBlocklistsCheckDomainResponse: String get() = TestFixtures.blocklistsCheckDomainJson
val testStatisticsResponse: String get() = TestFixtures.statisticsResponseJson
val testTopCountriesResponse: String get() = TestFixtures.topCountriesJson
val testTopIpOwnersResponse: String get() = TestFixtures.topIpOwnersJson
val testTopScenariosResponse: String get() = TestFixtures.topScenariosJson
val testTopTargetsResponse: String get() = TestFixtures.topTargetsJson
val testStatusResponse: String get() = TestFixtures.statusResponseJson
val testApiStatusJson: String get() = TestFixtures.apiStatusJson
