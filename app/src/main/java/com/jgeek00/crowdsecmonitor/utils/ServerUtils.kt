package com.jgeek00.crowdsecmonitor.utils

import com.jgeek00.crowdsecmonitor.data.db.CSServerModel

fun buildServerUrl(server: CSServerModel): String {
    val port = server.port?.let { ":$it" } ?: ""
    val path = server.path?.takeIf { it.isNotEmpty() } ?: ""
    return "${server.http}://${server.domain}$port$path"
}

