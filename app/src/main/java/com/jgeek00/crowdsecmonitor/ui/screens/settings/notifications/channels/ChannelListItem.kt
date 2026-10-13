package com.jgeek00.crowdsecmonitor.ui.screens.settings.notifications.channels

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jgeek00.crowdsecmonitor.R
import com.jgeek00.crowdsecmonitor.data.models.NotificationChannelType
import com.jgeek00.crowdsecmonitor.data.models.UserNotificationChannel
import com.jgeek00.crowdsecmonitor.ui.components.ListItemContent
import com.jgeek00.crowdsecmonitor.ui.components.OptionsMenuBottomSheet
import com.jgeek00.crowdsecmonitor.ui.components.OptionsMenuBottomSheetItem
import com.jgeek00.crowdsecmonitor.ui.components.OptionsMenuBottomSheetItemRole
import com.jgeek00.crowdsecmonitor.ui.components.RoundedCornersListTile
import com.jgeek00.crowdsecmonitor.viewmodel.NotificationChannelsListViewModel

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ChannelListItem(
    index: Int,
    totalItems: Int,
    channel: UserNotificationChannel,
    viewModel: NotificationChannelsListViewModel,
    onOpen: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    RoundedCornersListTile(
        index = index,
        totalItems = totalItems,
        onClick = onOpen,
        onLongClick = { menuExpanded = true }
    ) {
        ListItemContent(
            headlineText = channel.name,
            subHeadlineText = ChannelTypeLabel(channel.type),
            leadingContent = {
                ChannelIcon(
                    type = channel.type,
                    modifier = Modifier.size(28.dp),
                    tint = if (channel.type == NotificationChannelType.EMAIL) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        )

        OptionsMenuBottomSheet(
            options = emptyList(),
            showMenu = menuExpanded,
            onDismiss = { menuExpanded = false },
            sections = listOf(
                listOf(
                    OptionsMenuBottomSheetItem(
                        title = stringResource(R.string.delete_channel),
                        icon = Icons.Rounded.Delete,
                        onClick = { showDeleteConfirm = true },
                        role = OptionsMenuBottomSheetItemRole.DESTRUCTIVE
                    )
                )
            )
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(stringResource(R.string.delete_channel)) },
            text = { Text(stringResource(R.string.delete_channel_confirm)) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    viewModel.delete(channel.id)
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
