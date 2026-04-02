package com.jgeek00.crowdsecmonitor.constants

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

object Enums {
    @Serializable
    enum class DecisionType(val value: String) {
        @SerialName("ban") BAN("ban"),
        @SerialName("captcha") CAPTCHA("captcha")
    }

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

    enum class SectionHeaderPaddingTop {
        NONE,
        SMALL,
        NORMAL,
    }

    enum class ListType {
        BLOCKLIST,
        ALLOWLIST
    }
}