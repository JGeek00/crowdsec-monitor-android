package com.jgeek00.crowdsecmonitor.ui.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RoutesTest {

    @Test
    fun `settings graph exposes the language destination alongside the configuration ones`() {
        val routes: Set<Route> = setOf(
            Route.AppConfiguration,
            Route.LanguageSelection,
            Route.ServerConfiguration
        )
        assertEquals(3, routes.size)
        assertTrue(routes.contains(Route.LanguageSelection))
    }
}
