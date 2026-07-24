package com.jgeek00.crowdsecmonitor.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DecisionsByIPResponse(
    @SerialName("filtering") val filtering: DecisionsListResponseFiltering,
    @SerialName("groups") val groups: List<DecisionsByIPResponseGroup>,
    @SerialName("pagination") val pagination: DecisionsListResponsePagination
)

@Serializable
data class DecisionsByIPResponseGroup(
    @SerialName("ip") val ip: String,
    @SerialName("country") val country: String? = null,
    @SerialName("owner") val owner: String? = null,
    @SerialName("as_number") val asNumber: String? = null,
    @SerialName("latitude") val latitude: Double? = null,
    @SerialName("longitude") val longitude: Double? = null,
    @SerialName("range") val range: String? = null,
    @SerialName("active_decisions") val activeDecisions: Int,
    @SerialName("total_decisions") val totalDecisions: Int
)
