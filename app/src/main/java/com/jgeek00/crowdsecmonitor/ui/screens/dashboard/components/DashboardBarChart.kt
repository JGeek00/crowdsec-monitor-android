package com.jgeek00.crowdsecmonitor.ui.screens.dashboard.components

import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jgeek00.crowdsecmonitor.R
import com.jgeek00.crowdsecmonitor.data.models.ActivityHistory
import ir.ehsannarmani.compose_charts.ColumnChart
import ir.ehsannarmani.compose_charts.models.AnimationMode
import ir.ehsannarmani.compose_charts.models.BarProperties
import ir.ehsannarmani.compose_charts.models.Bars
import ir.ehsannarmani.compose_charts.models.GridProperties
import ir.ehsannarmani.compose_charts.models.HorizontalIndicatorProperties
import ir.ehsannarmani.compose_charts.models.IndicatorCount
import ir.ehsannarmani.compose_charts.models.LabelHelperProperties
import ir.ehsannarmani.compose_charts.models.LabelProperties
import ir.ehsannarmani.compose_charts.models.PopupProperties
import java.time.LocalDate
import java.time.format.TextStyle as JavaTextStyle
import java.util.Locale

private val alertColor = Color(0xFFFF9800)
private val decisionColor = Color(0xFFF44336)

@Composable
fun DashboardBarChart(activityHistory: List<ActivityHistory>) {
    if (activityHistory.isEmpty()) return

    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val labelTextStyle = TextStyle(color = onSurfaceVariant, fontSize = 11.sp)

    val bars = remember(activityHistory) {
        activityHistory.takeLast(7).map { entry ->
            val dayLabel = try {
                LocalDate.parse(entry.date)
                    .dayOfWeek
                    .getDisplayName(JavaTextStyle.SHORT, Locale.getDefault())
            } catch (_: Exception) {
                entry.date.takeLast(2)
            }
            Bars(
                label = dayLabel,
                values = listOf(
                    Bars.Data(
                        value = entry.amountAlerts.toDouble(),
                        color = SolidColor(alertColor)
                    ),
                    Bars.Data(
                        value = entry.amountDecisions.toDouble(),
                        color = SolidColor(decisionColor)
                    )
                )
            )
        }
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                LegendItem(color = alertColor, label = stringResource(R.string.alerts))
                LegendItem(color = decisionColor, label = stringResource(R.string.decisions))
            }

            Spacer(modifier = Modifier.height(16.dp))

            ColumnChart(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                data = bars,
                barProperties = BarProperties(
                    thickness = 14.dp,
                    spacing = 4.dp,
                    cornerRadius = Bars.Data.Radius.Circular(4.dp)
                ),
                labelHelperProperties = LabelHelperProperties(enabled = false),
                labelProperties = LabelProperties(
                    enabled = true,
                    textStyle = labelTextStyle
                ),
                indicatorProperties = HorizontalIndicatorProperties(
                    enabled = true,
                    textStyle = labelTextStyle,
                    count = IndicatorCount.CountBased(count = 4),
                    contentBuilder = { it.toInt().toString() }
                ),
                gridProperties = GridProperties(
                    xAxisProperties = GridProperties.AxisProperties(enabled = false),
                    yAxisProperties = GridProperties.AxisProperties(
                        lineCount = 4,
                        color = SolidColor(onSurfaceVariant.copy(alpha = 0.15f))
                    )
                ),
                animationMode = AnimationMode.Together(),
                animationSpec = tween(500),
                animationDelay = 200,
                popupProperties = PopupProperties(
                    contentBuilder = { it.value.toInt().toString() },
                    textStyle = TextStyle(
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    containerColor = MaterialTheme.colorScheme.background,
                    contentHorizontalPadding = 12.dp,
                    contentVerticalPadding = 6.dp,
                )
            )
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium
        )
    }
}
