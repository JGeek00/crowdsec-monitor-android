package com.jgeek00.crowdsecmonitor.utils

import androidx.compose.ui.graphics.Color

data class DashboardItemData(
    val item: String,
    val value: Int
)

data class DashboardItemDataForView(
    val item: String,
    val value: Int,
    val percentage: Double,
    val color: Color
)