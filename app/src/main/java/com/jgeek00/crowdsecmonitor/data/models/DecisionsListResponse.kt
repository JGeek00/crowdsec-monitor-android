package com.jgeek00.crowdsecmonitor.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DecisionsListResponse(
    @SerialName("filtering") val filtering: DecisionsListResponseFiltering,
    @SerialName("items") val items: List<DecisionsListResponseItem>,
    @SerialName("pagination") val pagination: DecisionsListResponsePagination
)

@Serializable
data class DecisionsListResponseFiltering(
    @SerialName("countries") val countries: List<String>,
    @SerialName("ipOwners") val ipOwners: List<String>
)

@Serializable
data class DecisionsListResponseItem(
    @SerialName("id") val id: Int,
    @SerialName("alert_id") val alertId: Int,
    @SerialName("origin") val origin: String,
    @SerialName("type") val type: String,
    @SerialName("scope") val scope: String,
    @SerialName("value") val value: String,
    @SerialName("expiration") val expiration: String,
    @SerialName("scenario") val scenario: String,
    @SerialName("simulated") val simulated: Boolean,
    @SerialName("source") val source: DecisionSource,
    @SerialName("crowdsec_created_at") val crowdsecCreatedAt: String
)

@Serializable
data class DecisionSource(
    @SerialName("as_name") val asName: String? = null,
    @SerialName("as_number") val asNumber: String? = null,
    @SerialName("cn") val cn: String? = null,
    @SerialName("ip") val ip: String? = null,
    @SerialName("latitude") val latitude: Double? = null,
    @SerialName("longitude") val longitude: Double? = null,
    @SerialName("range") val range: String? = null,
    @SerialName("scope") val scope: String,
    @SerialName("value") val value: String
)

@Serializable
data class DecisionsListResponsePagination(
    @SerialName("page") val page: Int,
    @SerialName("amount") val amount: Int,
    @SerialName("total") val total: Int
)

