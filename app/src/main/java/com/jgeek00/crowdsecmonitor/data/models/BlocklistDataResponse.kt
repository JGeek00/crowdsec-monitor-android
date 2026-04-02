package com.jgeek00.crowdsecmonitor.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BlocklistDataResponse(
    @SerialName("data") val data: BlocklistDataResponseData
)

@Serializable
data class BlocklistDataResponseData(
    @SerialName("id") val id: Int,
    @SerialName("url") val url: String? = null,
    @SerialName("name") val name: String,
    @SerialName("enabled") val enabled: Boolean? = null,
    @SerialName("added_date") val addedDate: String? = null,
    @SerialName("last_refresh_attempt") val lastRefreshAttempt: String? = null,
    @SerialName("last_successful_refresh") val lastSuccessfulRefresh: String? = null,
    @SerialName("count_ips") val countIps: Int,
    @SerialName("type") val type: BlocklistType,
    @SerialName("blocklistIps") val blocklistIps: List<String>
)

