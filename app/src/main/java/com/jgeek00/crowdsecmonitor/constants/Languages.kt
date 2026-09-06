package com.jgeek00.crowdsecmonitor.constants

object Languages {
    data class Entry(val tag: String, val name: String)

    val available = listOf(
        Entry("de", "Deutsch"),
        Entry("en", "English"),
        Entry("es", "Español")
    ).sortedBy { it.name }

    // ponytail: single matching rule for the language section (exact or region variant like es-ES).
    fun displayNameFor(tag: String?): String? =
        tag?.takeIf { it.isNotEmpty() }?.let { t ->
            available.find { t.startsWith(it.tag) }?.name ?: t
        }
}
