package com.jgeek00.crowdsecmonitor.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BlocklistsCheckDomainResponse(
    @SerialName("domain") val domain: String,
    @SerialName("ips") val ips: List<BlocklistsCheckDomainResponseIp>
)

@Serializable
data class BlocklistsCheckDomainResponseIp(
    @SerialName("ip") val ip: String,
    @SerialName("blocklists") val blocklists: List<String>
)

