package com.jgeek00.crowdsecmonitor.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BlocklistsCheckIPsResponse(
    @SerialName("results") val results: List<BlocklistsCheckIPsResponseResult>
)

@Serializable
data class BlocklistsCheckIPsResponseResult(
    @SerialName("ip") val ip: String,
    @SerialName("blocklists") val blocklists: List<String>
)

