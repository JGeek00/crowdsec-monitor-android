package com.jgeek00.crowdsecmonitor.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiStatusResponse(
    @SerialName("csLapi")
    val csLapi: ApiStatusResponseCSLapi,
    @SerialName("csMonitorApi")
    val csMonitorApi: ApiStatusResponseCSMonitorApi
)

@Serializable
data class ApiStatusResponseCSLapi(
    val lapiConnected: Boolean,
    val lastSuccessfulSync: String? = null,
    val timestamp: String? = null
)

@Serializable
data class ApiStatusResponseCSMonitorApi(
    val version: String,
    val newVersionAvailable: String? = null
)

