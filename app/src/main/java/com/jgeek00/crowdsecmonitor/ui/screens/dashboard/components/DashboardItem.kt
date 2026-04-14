package com.jgeek00.crowdsecmonitor.ui.screens.dashboard.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jgeek00.crowdsecmonitor.constants.Enums
import com.jgeek00.crowdsecmonitor.ui.components.toFlagEmoji
import uk.co.bocajsolutions.cardshape.Shape
import java.util.Locale
import kotlin.math.roundToInt
import androidx.compose.ui.platform.LocalLocale

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DashboardItem(
    index: Int,
    listLength: Int,
    itemType: Enums.DashboardItemType,
    label: String,
    amount: Int,
    percentage: Double,
    color: Color? = null,
    isHighlighted: Boolean = false,
    modifier: Modifier = Modifier
) {
    val groupedShape = Shape(groupSize = listLength, index = index)

    val rowBackground by animateColorAsState(
        targetValue = if (isHighlighted) MaterialTheme.colorScheme.primaryContainer
                      else MaterialTheme.colorScheme.background,
        animationSpec = tween(durationMillis = 120),
        label = "itemHighlight"
    )

    Card(
        modifier = modifier.padding(bottom = if (index == listLength - 1) 0.dp else 2.dp),
        shape = groupedShape
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(rowBackground)
                .padding(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                if (color != null) {
                    ColorDot(color)
                    Spacer(Modifier.width(8.dp))
                }
                when (itemType) {
                    Enums.DashboardItemType.COUNTRY -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(label.toFlagEmoji())
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = Locale(
                                    "",
                                    label.uppercase()
                                ).getDisplayCountry(LocalLocale.current.platformLocale)
                                    .ifBlank { label.uppercase() },
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Enums.DashboardItemType.IP_OWNER -> {
                        Text(text = label, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }

                    Enums.DashboardItemType.SCENARIO -> {
                        val parts = label.split("/")
                        val type = parts.getOrNull(0)?.trim() ?: label
                        val name = parts.getOrNull(1)?.trim() ?: ""
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = type,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color.Gray)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = name,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Enums.DashboardItemType.TARGET -> {
                        Text(text = label, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$amount",
                    maxLines = 1,
                    overflow = TextOverflow.Visible
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "(${(percentage * 100).roundToInt()}%)",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Visible
                )
            }
        }
    }
}

@Composable
private fun ColorDot(color: Color) {
    Box(
        modifier = Modifier
            .size(12.dp)
            .clip(CircleShape)
            .background(color)
    )
}

