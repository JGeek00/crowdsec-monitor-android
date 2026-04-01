package com.jgeek00.crowdsecmonitor.data.models

data class DecisionsRequest(
    var filters: DecisionsRequestFilters,
    var pagination: DecisionsRequestPagination
)

data class DecisionsRequestFilters(
    var onlyActive: Boolean? = null
)

data class DecisionsRequestPagination(
    var offset: Int,
    var limit: Int
)

