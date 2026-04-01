package com.jgeek00.crowdsecmonitor.data.models

import com.jgeek00.crowdsecmonitor.constants.Enums
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateDecisionRequest(
    @SerialName("ip") val ip: String,
    @SerialName("duration") val duration: String,
    @SerialName("type") val type: Enums.DecisionType,
    @SerialName("reason") val reason: String
)

