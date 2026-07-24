package com.jgeek00.crowdsecmonitor.ui.screens.decisions.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jgeek00.crowdsecmonitor.data.models.DecisionsByIPDetailResponseDecision
import com.jgeek00.crowdsecmonitor.extensions.toFormattedDateTimeCustom
import com.jgeek00.crowdsecmonitor.ui.components.RoundedCornersListTile
import com.jgeek00.crowdsecmonitor.utils.parseScenario

@Composable
fun DecisionItemNoIP(
    index: Int,
    totalListAmount: Int,
    decision: DecisionsByIPDetailResponseDecision,
    disableTimerAnimation: Boolean = false,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null
) {
    val scenarioLabel = remember(decision.scenario) {
        val parts = parseScenario(decision.scenario)
        parts.name.ifBlank { parts.author }
    }

    RoundedCornersListTile(
        index = index,
        totalItems = totalListAmount,
        onClick = onClick,
        onLongClick = onLongClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = scenarioLabel,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = decision.crowdsecCreatedAt.toFormattedDateTimeCustom(),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DecisionTypeChip(decisionType = decision.type)
                DecisionTimer(expiration = decision.expiration, disableAnimation = disableTimerAnimation)
            }
        }
    }
}
