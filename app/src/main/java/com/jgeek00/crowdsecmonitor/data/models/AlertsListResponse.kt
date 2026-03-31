package com.jgeek00.crowdsecmonitor.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AlertsListResponse(
    @SerialName("filtering") val filtering: AlertsListResponseFiltering,
    @SerialName("items") val items: List<AlertsListResponseAlert>,
    @SerialName("pagination") val pagination: AlertsListResponsePagination
)

@Serializable
data class AlertsListResponseFiltering(
    @SerialName("countries") val countries: List<String>,
    @SerialName("scenarios") val scenarios: List<String>,
    @SerialName("ipOwners") val ipOwners: List<String>,
    @SerialName("targets") val targets: List<String>
)

@Serializable
data class AlertsListResponseAlert(
    @SerialName("id") val id: Int,
    @SerialName("uuid") val uuid: String,
    @SerialName("scenario") val scenario: String,
    @SerialName("scenario_version") val scenarioVersion: String,
    @SerialName("scenario_hash") val scenarioHash: String,
    @SerialName("message") val message: String,
    @SerialName("capacity") val capacity: Int,
    @SerialName("leakspeed") val leakspeed: String,
    @SerialName("simulated") val simulated: Boolean,
    @SerialName("remediation") val remediation: Boolean,
    @SerialName("events_count") val eventsCount: Int,
    @SerialName("machine_id") val machineId: String,
    @SerialName("source") val source: AlertSource,
    @SerialName("meta") val meta: List<AlertItemMeta>,
    @SerialName("events") val events: List<AlertEvent>,
    @SerialName("crowdsec_created_at") val crowdsecCreatedAt: String,
    @SerialName("start_at") val startAt: String,
    @SerialName("stop_at") val stopAt: String
)

@Serializable
data class AlertItemMeta(
    @SerialName("key") val key: String,
    @SerialName("value") val value: List<String>
)

@Serializable
data class AlertEvent(
    @SerialName("meta") val meta: List<AlertEventMeta>,
    @SerialName("timestamp") val timestamp: String
)

@Serializable
data class AlertEventMeta(
    @SerialName("key") val key: String,
    @SerialName("value") val value: List<String>
)

@Serializable
data class AlertSource(
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
data class AlertsListResponsePagination(
    @SerialName("page") val page: Int,
    @SerialName("amount") val amount: Int,
    @SerialName("total") val total: Int
)
