package com.jgeek00.crowdsecmonitor.data.models

data class AlertsRequest(
    var filters: AlertsRequestFilters,
    var pagination: AlertsRequestPagination
)

data class AlertsRequestFilters(
    var countries: List<String> = emptyList(),
    var scenarios: List<String> = emptyList(),
    var ipOwners: List<String> = emptyList(),
    var targets: List<String> = emptyList()
)

data class AlertsRequestPagination(
    var offset: Int,
    var limit: Int
)