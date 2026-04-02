package com.jgeek00.crowdsecmonitor.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AllowlistsCheckIPsResponse(
    @SerialName("results") val results: List<AllowlistsCheckIPsResponseResult>
)

@Serializable
data class AllowlistsCheckIPsResponseResult(
    @SerialName("ip") val ip: String,
    @SerialName("allowlist") val allowlist: String? = null
)

