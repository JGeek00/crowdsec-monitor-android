package com.jgeek00.crowdsecmonitor.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RefreshBlocklistsResponse(
    @SerialName("message") val message: String
)
