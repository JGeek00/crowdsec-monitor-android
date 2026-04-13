package com.jgeek00.crowdsecmonitor.ui.screens.lists.blocklists

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jgeek00.crowdsecmonitor.R
import com.jgeek00.crowdsecmonitor.data.models.BlocklistType
import com.jgeek00.crowdsecmonitor.data.models.BlocklistsListResponseItem
import com.jgeek00.crowdsecmonitor.extensions.toInstant
import com.jgeek00.crowdsecmonitor.ui.components.OptionsMenuBottomSheet
import com.jgeek00.crowdsecmonitor.ui.components.OptionsMenuBottomSheetItem
import com.jgeek00.crowdsecmonitor.ui.components.OptionsMenuBottomSheetItemRole
import com.jgeek00.crowdsecmonitor.ui.components.RoundedCornersListTile
import com.jgeek00.crowdsecmonitor.viewmodel.BlocklistsListViewModel
import com.jgeek00.crowdsecmonitor.viewmodel.ServiceStatusViewModel
import kotlin.math.abs

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BlocklistListItem(
    index: Int,
    totalItems: Int,
    blocklist: BlocklistsListResponseItem,
    viewModel: BlocklistsListViewModel,
    onNavigateToDetails: () -> Unit,
    serviceStatusViewModel: ServiceStatusViewModel = hiltViewModel(),
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val showRefreshWarning = remember(blocklist.lastRefreshAttempt, blocklist.lastSuccessfulRefresh) {
        val attempt = blocklist.lastRefreshAttempt?.toInstant() ?: return@remember false
        val successful = blocklist.lastSuccessfulRefresh?.toInstant() ?: return@remember false
        val diffSeconds = abs(attempt.epochSecond - successful.epochSecond)
        diffSeconds >= 3600L
    }

    val serviceStatus = serviceStatusViewModel.status.collectAsState().value
    val blocklistProcess = getBlocklistActiveProcess(serviceStatus.data, blocklist.id)

    RoundedCornersListTile(
        index = index,
        totalItems = totalItems,
        onClick = onNavigateToDetails,
        onLongClick = if (blocklist.type == BlocklistType.API) {
            { menuExpanded = true }
        } else null,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = blocklist.name,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.blocked_ips_count, blocklist.countIps),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                when (blocklist.type) {
                    BlocklistType.API -> Text(
                        text = stringResource(R.string.managed_by_monitor_api),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    BlocklistType.CROWDSEC -> Text(
                        text = stringResource(R.string.managed_by_crowdsec),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
                if (showRefreshWarning && blocklistProcess == null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Warning,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.blocklist_refresh_failed),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                if (blocklistProcess != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = stringResource(getProcessType(blocklistProcess) ?: R.string.processing_blocklist) + "...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            if (blocklist.enabled != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = if (blocklist.enabled) Icons.Rounded.CheckCircle else Icons.Rounded.Cancel,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    tint = if (blocklist.enabled) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
                )
            }
        }

        if (blocklist.type == BlocklistType.API) {
            OptionsMenuBottomSheet(
                options = listOf(
                    OptionsMenuBottomSheetItem(
                        title = if (blocklist.enabled == true) stringResource(R.string.disable_blocklist) else stringResource(
                            R.string.enable_blocklist
                        ),
                        icon = if (blocklist.enabled == true) Icons.Rounded.Cancel else Icons.Rounded.CheckCircle,
                        onClick = {
                            viewModel.enableDisableBlocklist(
                                blocklist.id,
                                blocklist.enabled != true
                            )
                        },
                        disabled = blocklistProcess != null
                    ),
                    OptionsMenuBottomSheetItem(
                        title = stringResource(R.string.delete_blocklist),
                        icon = Icons.Rounded.Delete,
                        onClick = { showDeleteConfirm = true },
                        role = OptionsMenuBottomSheetItemRole.DESTRUCTIVE,
                        disabled = blocklistProcess != null
                    )
                ),
                showMenu = menuExpanded,
                onDismiss = { menuExpanded = false}
            )
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(stringResource(R.string.delete_blocklist)) },
            text = { Text(stringResource(R.string.delete_blocklist_confirm)) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    viewModel.deleteBlocklist(blocklist.id)
                }) {
                    Text(
                        text = stringResource(R.string.delete),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

