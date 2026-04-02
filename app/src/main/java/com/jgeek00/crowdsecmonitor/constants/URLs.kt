package com.jgeek00.crowdsecmonitor.constants

object URLs {
    const val API_PACKAGE = "https://github.com/jgeek00/cs-monitor-api/releases"
    const val APP_DETAILS = "https://apps.jgeek00.com/2f1zi66jongz9ix"
    const val REST_OF_APPS = "https://apps.jgeek00.com"
    const val PAYPAL = "https://www.paypal.com/donate/?hosted_button_id=T63UK6AVL3MG8"

    fun crowdsecHubScenario(scenario: String): String {
        val parts = scenario.split("/")
        return if (parts.size >= 2) {
            "https://hub.crowdsec.net/author/${parts[0]}/configurations/${parts[1]}"
        } else {
            "https://hub.crowdsec.net"
        }
    }
}
