package com.jgeek00.crowdsecmonitor.ui.screens.dashboard.components

import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jgeek00.crowdsecmonitor.utils.DashboardItemDataForView
import ir.ehsannarmani.compose_charts.PieChart
import ir.ehsannarmani.compose_charts.models.LabelHelperProperties
import ir.ehsannarmani.compose_charts.models.Pie

private val STROKE_WIDTH = 36.dp

@Composable
fun DashboardPieChart(
    data: List<DashboardItemDataForView>,
    onSliceClick: (index: Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) return

    val pieData = remember(data) {
        data.map { item ->
            Pie(
                data = item.percentage * 100,
                color = item.color,
                selectedColor = item.color
            )
        }
    }

    PieChart(
        modifier = modifier.size(200.dp),
        data = pieData,
        onPieClick = { clickedPie ->
            val index = pieData.indexOf(clickedPie)
            if (index >= 0) onSliceClick(index)
        },
        style = Pie.Style.Stroke(width = STROKE_WIDTH),
        labelHelperProperties = LabelHelperProperties(enabled = false),
        colorAnimEnterSpec = tween(500),
        colorAnimExitSpec = tween(300),
    )
}
