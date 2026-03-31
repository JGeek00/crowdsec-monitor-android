package com.jgeek00.crowdsecmonitor.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AlertDetailsResponse(
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
    @SerialName("meta") val meta: List<AlertDetailsMeta>,
    @SerialName("events") val events: List<AlertDetailsEvent>,
    @SerialName("crowdsec_created_at") val crowdsecCreatedAt: String,
    @SerialName("start_at") val startAt: String,
    @SerialName("stop_at") val stopAt: String,
    @SerialName("decisions") val decisions: List<AlertDetailsDecision>
)

@Serializable
data class AlertDetailsMeta(
    @SerialName("key") val key: String,
    @SerialName("value") val value: List<String>
)

@Serializable
data class AlertDetailsDecision(
    @SerialName("id") val id: Int,
    @SerialName("alert_id") val alertId: Int,
    @SerialName("origin") val origin: String,
    @SerialName("type") val type: String,
    @SerialName("scope") val scope: String,
    @SerialName("value") val value: String,
    @SerialName("expiration") val expiration: String,
    @SerialName("scenario") val scenario: String,
    @SerialName("simulated") val simulated: Boolean,
    @SerialName("source") val source: AlertSource,
    @SerialName("crowdsec_created_at") val crowdsecCreatedAt: String
)

@Serializable
data class AlertDetailsEvent(
    @SerialName("meta") val meta: List<AlertDetailsEventMeta>,
    @SerialName("timestamp") val timestamp: String
)

@Serializable
data class AlertDetailsEventMeta(
    @SerialName("key") val key: String,
    @SerialName("value") val value: List<String>
)

