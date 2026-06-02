package com.jgeek00.crowdsecmonitor.utils

/**
 * Result of safely splitting a CrowdSec scenario string by "/".
 * Format: "author/scenario_name"
 *
 * @property author — part before "/". If "/" doesn't exist, the full scenario string.
 * @property name — part after "/". Empty string if "/" doesn't exist.
 */
data class ScenarioParts(
    val author: String,
    val name: String
)

/**
 * Splits a scenario string by "/" safely.
 *
 * If "/" exists: author = part before, name = part after.
 * If "/" doesn't exist: author = full scenario, name = "".
 *
 * Uses [limit = 2] so that "author/name/extra" yields author = "author", name = "name/extra".
 */
fun parseScenario(scenario: String): ScenarioParts {
    val parts = scenario.split("/", limit = 2)
    return if (parts.size >= 2) {
        ScenarioParts(author = parts[0], name = parts[1])
    } else {
        ScenarioParts(author = scenario, name = "")
    }
}
