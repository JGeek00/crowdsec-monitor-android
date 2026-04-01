package com.jgeek00.crowdsecmonitor.constants

object URLs {
    const val API_PACKAGE_URL = "https://github.com/jgeek00/cs-monitor-api/releases"

    fun crowdsecHubScenario(scenario: String): String {
        val parts = scenario.split("/")
        return if (parts.size >= 2) {
            "https://hub.crowdsec.net/author/${parts[0]}/configurations/${parts[1]}"
        } else {
            "https://hub.crowdsec.net"
        }
    }
}
