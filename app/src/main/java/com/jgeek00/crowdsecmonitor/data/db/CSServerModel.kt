package com.jgeek00.crowdsecmonitor.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "CSServers")
data class CSServerModel(
    @PrimaryKey val id: UUID = UUID.randomUUID(),
    val name: String,
    val http: String,
    val domain: String,
    val path: String?,
    val port: Int?,
    val authMethod: String,
    val basicUser: String?,
    val basicPassword: String?,
    val bearerToken: String?,
    val defaultServer: Boolean? = false,
    val customHeaders: List<Pair<String, String>>? = null
)