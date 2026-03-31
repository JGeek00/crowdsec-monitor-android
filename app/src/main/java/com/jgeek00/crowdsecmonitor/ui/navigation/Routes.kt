package com.jgeek00.crowdsecmonitor.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.FrontHand
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.ui.graphics.vector.ImageVector
import com.jgeek00.crowdsecmonitor.R
import kotlinx.serialization.Serializable

sealed interface Route {
    // Home nested graph
    @Serializable data object HomeGraph : Route
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
    @StringRes val label: Int,
    val icon: ImageVector
)

val topLevelRoutesNoServer = listOf(
    TopLevelRoute(Route.HomeGraph, R.string.home, Icons.Rounded.Home),
    TopLevelRoute(Route.SettingsGraph, R.string.settings, Icons.Rounded.Settings),
)

val topLevelRoutesWithServer = listOf(
    TopLevelRoute(Route.DashboardGraph, R.string.dashboard, Icons.Rounded.Dashboard),
    TopLevelRoute(Route.AlertsGraph, R.string.alerts, Icons.Rounded.Warning),
    TopLevelRoute(Route.DecisionsGraph, R.string.decisions, Icons.Rounded.FrontHand),
    TopLevelRoute(Route.SettingsGraph, R.string.settings, Icons.Rounded.Settings),
)
