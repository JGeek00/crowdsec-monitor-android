package com.jgeek00.crowdsecmonitor.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ToggleBlocklistRequest(
    @SerialName("enabled") val enabled: Boolean
)

