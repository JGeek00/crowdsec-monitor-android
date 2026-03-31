package com.jgeek00.crowdsecmonitor.constants

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

    enum class ThemeMode {
        SYSTEM,
        LIGHT,
        DARK
    }

    enum class DashboardBoxSummaryType {
        ALERTS,
        DECISIONS
    }

    enum class DashboardItemType {
        COUNTRY,
        IP_OWNER,
        SCENARIO,
        TARGET
    }
}