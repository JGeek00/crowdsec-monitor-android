package com.jgeek00.crowdsecmonitor.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiStatusResponse(
    @SerialName("csLapi")
    val csLapi: ApiStatusResponseCSLapi,
    @SerialName("csBouncer")
    val csBouncer: ApiStatusResponseCSBouncer,
    @SerialName("csMonitorApi")
    val csMonitorApi: ApiStatusResponseCSMonitorApi,
    val processes: List<ApiStatusResponseProcess>
)

@Serializable
data class ApiStatusResponseCSBouncer(
    val available: Boolean
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

@Serializable
data class ApiStatusResponseProcess(
    val id: String,
    val beginDatetime: String,
    val endDatetime: String? = null,
    val successful: Boolean? = null,
    val error: String? = null,
    val blocklistImport: ApiStatusResponseProcessBlocklist? = null,
    val blocklistEnable: ApiStatusResponseProcessBlocklist? = null,
    val blocklistDisable: ApiStatusResponseProcessBlocklistIps? = null,
    val blocklistDelete: ApiStatusResponseProcessBlocklistIps? = null,
    val blocklistRefresh: ApiStatusResponseProcessBlocklistRefresh? = null
)

@Serializable
enum class ApiStatusResponseProcessBlocklistFieldStatus {
    @SerialName("pending") PENDING,
    @SerialName("running") RUNNING,
    @SerialName("successful") SUCCESSFUL,
    @SerialName("failed") FAILED
}

@Serializable
enum class ApiStatusResponseProcessBlocklistStep {
    @SerialName("fetch") FETCH,
    @SerialName("parse") PARSE,
    @SerialName("import") IMPORT
}

@Serializable
data class ApiStatusResponseProcessBlocklistProgress(
    val totalIps: Int,
    val processedIps: Int
)

@Serializable
data class ApiStatusResponseProcessBlocklistIps(
    val blocklistId: Int,
    val blocklistName: String,
    val blocklistIps: Int,
    val ipsToDelete: Int,
    val processedIps: Int
)

@Serializable
data class ApiStatusResponseProcessBlocklist(
    val blocklistId: Int,
    val blocklistName: String,
    val step: ApiStatusResponseProcessBlocklistStep,
    val fetched: ApiStatusResponseProcessBlocklistFieldStatus,
    val parsed: ApiStatusResponseProcessBlocklistFieldStatus,
    val imported: ApiStatusResponseProcessBlocklistFieldStatus,
    val processIps: ApiStatusResponseProcessBlocklistProgress
)

@Serializable
data class ApiStatusResponseProcessBlocklistRefresh(
    val totalBlocklists: Int,
    val processedBlocklists: Int,
    val successful: Int,
    val failed: Int
)

