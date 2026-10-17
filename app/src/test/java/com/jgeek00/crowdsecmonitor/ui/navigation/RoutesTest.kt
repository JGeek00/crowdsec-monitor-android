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

    @Test
    fun `settings graph exposes the notifications entry and the details destinations`() {
        val entry = Route.Notifications
        val notificationDetails = Route.NotificationDetails(id = 7)
        val channelDetails = Route.ChannelDetails(id = 9)

        assertEquals(Route.Notifications, entry)
        assertEquals(7, notificationDetails.id)
        assertEquals(9, channelDetails.id)

        val distinct = setOf(entry, Route.NotificationDetails(1), Route.ChannelDetails(1))
        assertEquals(3, distinct.size)
    }

    @Test
    fun `details routes with different ids are not equal`() {
        assertTrue(Route.NotificationDetails(1) != Route.NotificationDetails(2))
        assertTrue(Route.ChannelDetails(1) != Route.ChannelDetails(2))
    }
}
