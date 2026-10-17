package com.jgeek00.crowdsecmonitor.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

// Provider definitions served by the backend. Field `type` strings are
// intentionally open (plain String): unknown field types are ignored with safe
// defaults.

@Serializable
data class ProviderFieldCondition(
    val field: String,
    @SerialName("equals") val equalsValue: JsonElement? = null,
    val present: Boolean? = null
)

@Serializable
data class ProviderFieldOption(
    val value: String,
    @SerialName("labelKey") val labelKey: String
)

@Serializable
data class ProviderSection(
    val key: String,
    @SerialName("labelKey") val labelKey: String
)

@Serializable
data class ProviderField(
    val key: String,
    val section: String? = null,
    @SerialName("labelKey") val labelKey: String,
    @SerialName("placeholderKey") val placeholderKey: String? = null,
    val type: String,
    val list: Boolean? = null,
    val required: Boolean? = null,
    val secret: Boolean? = null,
    @SerialName("default") val defaultValue: JsonElement? = null,
    val regex: String? = null,
    @SerialName("minLength") val minLength: Int? = null,
    @SerialName("maxLength") val maxLength: Int? = null,
    val min: Double? = null,
    val max: Double? = null,
    val integer: Boolean? = null,
    val options: List<ProviderFieldOption>? = null,
    @SerialName("requiredIf") val requiredIf: ProviderFieldCondition? = null,
    @SerialName("exclusiveWith") val exclusiveWith: List<String>? = null,
    @SerialName("visibleIf") val visibleIf: ProviderFieldCondition? = null
)

@Serializable
data class NotificationProvider(
    val type: String,
    @SerialName("descriptionKey") val descriptionKey: String? = null,
    val icon: String,
    @SerialName("labelKey") val labelKey: String,
    @SerialName("supportsTest") val supportsTest: Boolean,
    @SerialName("supportsAlertInfo") val supportsAlertInfo: Boolean,
    val sections: List<ProviderSection>? = null,
    val fields: List<ProviderField>
)

@Serializable
data class ProvidersListResponse(
    val version: Int,
    val providers: List<NotificationProvider>
)
