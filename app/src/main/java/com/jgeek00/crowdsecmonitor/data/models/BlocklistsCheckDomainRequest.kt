package com.jgeek00.crowdsecmonitor.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BlocklistsCheckDomainRequest(
    @SerialName("domain") val domain: String
)

