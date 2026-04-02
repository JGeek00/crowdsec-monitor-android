package com.jgeek00.crowdsecmonitor.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BlocklistsCheckIPsRequest(
    @SerialName("ips") val ips: List<String>
)

