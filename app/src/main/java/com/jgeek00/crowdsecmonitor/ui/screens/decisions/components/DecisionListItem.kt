package com.jgeek00.crowdsecmonitor.ui.screens.decisions.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.HourglassEmpty
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.jgeek00.crowdsecmonitor.R
import com.jgeek00.crowdsecmonitor.data.models.DecisionsListResponseItem
import com.jgeek00.crowdsecmonitor.ui.components.CountryFlag
import com.jgeek00.crowdsecmonitor.ui.components.RoundedCornersListTile
import com.jgeek00.crowdsecmonitor.viewmodel.DecisionsListViewModel
import uk.co.bocajsolutions.cardshape.Shape
import uk.co.bocajsolutions.cardshape.ShapeStyle

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DecisionListItem(
    index: Int,
    totalListAmount: Int,
    decision: DecisionsListResponseItem,
    viewModel: DecisionsListViewModel? = null,
    onNavigateToDetails: (() -> Unit)?
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var showExpireConfirm by remember { mutableStateOf(false) }
    var showExpireError by remember { mutableStateOf(false) }

    val scenarioLabel = remember(decision.scenario) {
        val parts = decision.scenario.split("/")
        if (parts.size >= 2) parts.last() else decision.scenario
    }

    if (onNavigateToDetails != null) {
        SegmentedListItem(
            onClick = onNavigateToDetails,
            onLongClick = { if (viewModel != null) { menuExpanded = true } else null },
            shapes = ListItemDefaults.segmentedShapes(index = index, count = totalListAmount),
        ) {
            if (viewModel != null) {
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.expire_decision)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.HourglassEmpty,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            showExpireConfirm = true
                        }
                    )
                }
            }
            Content(
                decision = decision,
                scenarioLabel = scenarioLabel
            )
        }
    }
    else {
        RoundedCornersListTile(
            index = index,
            totalItems = totalListAmount,
        ) {
            Content(
                decision,
                scenarioLabel = scenarioLabel
            )
        }
    }

    if (viewModel != null && showExpireConfirm) {
        AlertDialog(
            onDismissRequest = { showExpireConfirm = false },
            title = { Text(stringResource(R.string.expire_decision)) },
            text = { Text(stringResource(R.string.expire_decision_confirm)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showExpireConfirm = false
                        viewModel.expireDecision(decision.id) { success ->
                            if (!success) showExpireError = true
                        }
                    }
                ) {
                    Text(
                        text = stringResource(R.string.expire_decision),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showExpireConfirm = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (viewModel != null && showExpireError) {
        AlertDialog(
            onDismissRequest = { showExpireError = false },
            title = { Text(stringResource(R.string.expire_decision_error_title)) },
            text = { Text(stringResource(R.string.expire_decision_error_msg)) },
            confirmButton = {
                TextButton(onClick = { showExpireError = false }) {
                    Text(stringResource(R.string.ok))
                }
            }
        )
    }
}

@Composable
private fun Content(
    decision: DecisionsListResponseItem,
    scenarioLabel: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = scenarioLabel,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!decision.source.cn.isNullOrBlank()) {
                    CountryFlag(countryCode = decision.source.cn, onlyFlag = true)
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Text(
                    text = decision.source.ip ?: decision.source.value,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            DecisionTypeChip(decisionType = decision.type)
            DecisionTimer(expiration = decision.expiration)
        }
    }
}
