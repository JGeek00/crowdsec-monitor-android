package com.jgeek00.crowdsecmonitor.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DecisionsByIPDetailResponse(
    @SerialName("ip") val ip: String,
    @SerialName("country") val country: String? = null,
    @SerialName("owner") val owner: String? = null,
    @SerialName("as_number") val asNumber: String? = null,
    @SerialName("latitude") val latitude: Double? = null,
    @SerialName("longitude") val longitude: Double? = null,
    @SerialName("range") val range: String? = null,
    @SerialName("active_decisions") val activeDecisions: Int,
    @SerialName("total_decisions") val totalDecisions: Int,
    @SerialName("decisions") val decisions: List<DecisionsByIPDetailResponseDecision>
)

@Serializable
data class DecisionsByIPDetailResponseDecision(
    @SerialName("id") val id: Int,
    @SerialName("alert_id") val alertId: Int,
    @SerialName("origin") val origin: String,
    @SerialName("type") val type: String,
    @SerialName("scope") val scope: String,
    @SerialName("value") val value: String,
    @SerialName("expiration") val expiration: String,
    @SerialName("scenario") val scenario: String,
    @SerialName("simulated") val simulated: Boolean,
    @SerialName("crowdsec_created_at") val crowdsecCreatedAt: String
)
