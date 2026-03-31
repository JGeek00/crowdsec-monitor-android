package com.jgeek00.crowdsecmonitor.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable

sealed interface Route {
    @Serializable data object Home : Route

    // Dashboard nested graph
    @Serializable data object DashboardGraph : Route
    @Serializable data object Dashboard : Route
    @Serializable data class FullListDashboard(val itemType: String) : Route

    // Alerts nested graph
    @Serializable data object AlertsGraph : Route
    @Serializable data object Alerts : Route

    // Decisions nested graph
    @Serializable data object DecisionsGraph : Route
    @Serializable data object Decisions : Route

    // Settings nested graph
    @Serializable data object SettingsGraph : Route
    @Serializable data object Settings : Route
    @Serializable data object AppConfiguration : Route
    @Serializable data object ServerConfiguration : Route
}

data class TopLevelRoute(
    val route: Route,
    val label: String,
    val icon: ImageVector
)

val topLevelRoutesNoServer = listOf(
    TopLevelRoute(Route.Home, "Home", Icons.Default.Home),
    TopLevelRoute(Route.SettingsGraph, "Settings", Icons.Default.Settings),
)

val topLevelRoutesWithServer = listOf(
    TopLevelRoute(Route.DashboardGraph, "Dashboard", Icons.Default.Dashboard),
    TopLevelRoute(Route.AlertsGraph, "Alerts", Icons.Default.Notifications),
    TopLevelRoute(Route.DecisionsGraph, "Decisions", Icons.Default.Warning),
    TopLevelRoute(Route.SettingsGraph, "Settings", Icons.Default.Settings),
)
