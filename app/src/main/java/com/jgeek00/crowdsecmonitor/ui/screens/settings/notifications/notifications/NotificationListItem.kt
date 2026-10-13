package com.jgeek00.crowdsecmonitor.ui.screens.settings.notifications.notifications

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.jgeek00.crowdsecmonitor.R
import com.jgeek00.crowdsecmonitor.data.models.UserNotification
import com.jgeek00.crowdsecmonitor.ui.components.ListItemContent
import com.jgeek00.crowdsecmonitor.ui.components.OptionsMenuBottomSheet
import com.jgeek00.crowdsecmonitor.ui.components.OptionsMenuBottomSheetItem
import com.jgeek00.crowdsecmonitor.ui.components.OptionsMenuBottomSheetItemRole
import com.jgeek00.crowdsecmonitor.ui.components.RoundedCornersListTile
import com.jgeek00.crowdsecmonitor.viewmodel.NotificationsListViewModel

private val EnabledColor = Color(0xFF4CAF50)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun NotificationListItem(
    index: Int,
    totalItems: Int,
    notification: UserNotification,
    viewModel: NotificationsListViewModel,
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
            headlineText = notification.name,
            subHeadlineText = notification.description?.takeIf { it.isNotEmpty() },
            trailingContent = {
                Icon(
                    imageVector = if (notification.enabled) Icons.Rounded.CheckCircle else Icons.Rounded.Cancel,
                    contentDescription = stringResource(
                        if (notification.enabled) R.string.notification_state_enabled
                        else R.string.notification_state_disabled
                    ),
                    modifier = Modifier,
                    tint = if (notification.enabled) EnabledColor else MaterialTheme.colorScheme.error
                )
            }
        )

        // Delete is isolated in its own menu section, separated from the toggle
        // action to avoid accidental adjacency.
        OptionsMenuBottomSheet(
            options = emptyList(),
            showMenu = menuExpanded,
            onDismiss = { menuExpanded = false },
            sections = listOf(
                listOf(
                    OptionsMenuBottomSheetItem(
                        title = stringResource(
                            if (notification.enabled) R.string.disable_notification
                            else R.string.enable_notification
                        ),
                        icon = if (notification.enabled) Icons.Rounded.Cancel else Icons.Rounded.CheckCircle,
                        onClick = { viewModel.toggle(notification) }
                    )
                ),
                listOf(
                    OptionsMenuBottomSheetItem(
                        title = stringResource(R.string.delete_notification),
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
            title = { Text(stringResource(R.string.delete_notification)) },
            text = { Text(stringResource(R.string.delete_notification_confirm)) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    viewModel.delete(notification.id)
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
