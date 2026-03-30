package com.jgeek00.crowdsecmonitor.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.graphics.vector.ImageVector

enum class AppDestinations(
    val label: String,
    val icon: ImageVector,
) {
    HOME("Home", Icons.Default.Home),
    ALERTS("Alerts", Icons.Default.Notifications),
    DECISIONS("Decisions", Icons.Default.Warning),
    SETTINGS("Settings", Icons.Default.Settings),
}
