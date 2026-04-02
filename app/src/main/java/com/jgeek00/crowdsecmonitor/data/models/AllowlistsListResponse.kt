package com.jgeek00.crowdsecmonitor.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AllowlistsListResponse(
    @SerialName("data") val data: List<AllowlistsListResponseAllowlist>,
    @SerialName("length") val length: Int
)

@Serializable
data class AllowlistsListResponseAllowlist(
    @SerialName("created_at") val createdAt: String,
    @SerialName("description") val description: String,
    @SerialName("items") val items: List<AllowlistsListResponseAllowlistItem>,
    @SerialName("name") val name: String,
    @SerialName("updated_at") val updatedAt: String
)

@Serializable
data class AllowlistsListResponseAllowlistItem(
    @SerialName("created_at") val createdAt: String,
    @SerialName("expiration") val expiration: String? = null,
    @SerialName("value") val value: String
)

