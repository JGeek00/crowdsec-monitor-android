package com.jgeek00.crowdsecmonitor.constants

import com.jgeek00.crowdsecmonitor.utils.parseScenario

object URLs {
    const val API_PACKAGE = "https://github.com/jgeek00/crowdsec-monitor-api/releases"
    const val API_REPO = "https://github.com/JGeek00/crowdsec-monitor-api"
    const val APP_DETAILS = "https://apps.jgeek00.com/2f1zi66jongz9ix"
    const val REST_OF_APPS = "https://apps.jgeek00.com"
    const val PAYPAL = "https://www.paypal.com/donate/?hosted_button_id=T63UK6AVL3MG8"

    fun crowdsecHubScenario(scenario: String): String {
        val parts = parseScenario(scenario)
        return if (parts.name.isNotBlank()) {
            "https://hub.crowdsec.net/author/${parts.author}/configurations/${parts.name}"
        } else {
            "https://hub.crowdsec.net"
        }
    }
}
