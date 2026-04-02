package com.jgeek00.crowdsecmonitor.ui.screens.decisions.components.filters

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jgeek00.crowdsecmonitor.R
import com.jgeek00.crowdsecmonitor.ui.components.FullScreenDialog
import com.jgeek00.crowdsecmonitor.viewmodel.DecisionsListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DecisionsFiltersSheet(
    viewModel: DecisionsListViewModel,
    onDismiss: () -> Unit
) {
    FullScreenDialog(
        title = stringResource(R.string.filters),
        onClose = onDismiss,
        allowClose = true,
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.only_active),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = viewModel.filters.onlyActive == true,
                        onCheckedChange = { checked ->
                            viewModel.updateFilters(viewModel.filters.copy(onlyActive = checked))
                        }
                    )
                }
            }
        }
    ) {
        TooltipBox(
            positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Below),
            tooltip = { PlainTooltip { Text(stringResource(R.string.reset)) } },
            state = rememberTooltipState()
        ) {
            IconButton(onClick = {
                onDismiss()
                viewModel.resetFilters()
            }) {
                Icon(Icons.Rounded.DeleteSweep, contentDescription = stringResource(R.string.reset))
            }
        }
        TooltipBox(
            positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Below),
            tooltip = { PlainTooltip { Text(stringResource(R.string.apply)) } },
            state = rememberTooltipState()
        ) {
            IconButton(onClick = {
                onDismiss()
                viewModel.applyFilters()
            }) {
                Icon(Icons.Rounded.Check, contentDescription = stringResource(R.string.apply))
            }
        }
    }
}

