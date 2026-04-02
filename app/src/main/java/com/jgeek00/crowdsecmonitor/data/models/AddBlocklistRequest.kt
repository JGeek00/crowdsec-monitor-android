package com.jgeek00.crowdsecmonitor.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AddBlocklistRequest(
    @SerialName("url") val url: String? = null,
    @SerialName("name") val name: String? = null,
    @SerialName("type") val type: String? = null
)

