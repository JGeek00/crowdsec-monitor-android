package com.jgeek00.crowdsecmonitor.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BlocklistsListResponse(
    @SerialName("items") val items: List<BlocklistsListResponseItem>,
    @SerialName("pagination") val pagination: BlocklistsListResponsePagination
)

@Serializable
data class BlocklistsListResponseItem(
    @SerialName("id") val id: String,
    @SerialName("url") val url: String? = null,
    @SerialName("name") val name: String,
    @SerialName("enabled") val enabled: Boolean? = null,
    @SerialName("added_date") val addedDate: String? = null,
    @SerialName("last_refresh_attempt") val lastRefreshAttempt: String? = null,
    @SerialName("last_successful_refresh") val lastSuccessfulRefresh: String? = null,
    @SerialName("count_ips") val countIps: Int,
    @SerialName("type") val type: BlocklistType
)

@Serializable
enum class BlocklistType {
    @SerialName("api") API,
    @SerialName("cs") CROWDSEC
}

@Serializable
data class BlocklistsListResponsePagination(
    @SerialName("page") val page: Int,
    @SerialName("amount") val amount: Int,
    @SerialName("total") val total: Int
)

