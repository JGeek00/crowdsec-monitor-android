package com.jgeek00.crowdsecmonitor.data.models

object Enums {
    enum class ConnectionMethod(val value: String) {
        HTTP("http"),
        HTTPS("https")
    }

    enum class AuthMethod(val value: String) {
        NONE("none"),
        BASIC("basic"),
        BEARER("bearer")
    }
}
