package com.jgeek00.crowdsecmonitor.constants

object Languages {
    data class Entry(val tag: String, val name: String)

    val available = listOf(
        Entry("de", "Deutsch"),
        Entry("en", "English"),
        Entry("es", "Español")
    ).sortedBy { it.name }
}
