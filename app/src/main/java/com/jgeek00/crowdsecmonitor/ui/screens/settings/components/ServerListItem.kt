package com.jgeek00.crowdsecmonitor.ui.screens.settings.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Dns
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.jgeek00.crowdsecmonitor.R
import com.jgeek00.crowdsecmonitor.data.db.CSServerModel
import com.jgeek00.crowdsecmonitor.ui.components.ListItemContent
import com.jgeek00.crowdsecmonitor.ui.components.OptionsMenuBottomSheet
import com.jgeek00.crowdsecmonitor.ui.components.OptionsMenuBottomSheetItem
import com.jgeek00.crowdsecmonitor.ui.components.OptionsMenuBottomSheetItemRole
import com.jgeek00.crowdsecmonitor.ui.components.RoundedCornersListTile
import com.jgeek00.crowdsecmonitor.utils.buildServerUrl

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ServerListItem(
    index: Int,
    totalItems: Int,
    server: CSServerModel,
    isCurrentServer: Boolean,
    onSelect: () -> Unit,
    onSetDefault: () -> Unit,
    onDelete: () -> Unit,
) {
    var showDropdownMenu by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    RoundedCornersListTile(
        index = index,
        totalItems = totalItems,
        onClick = onSelect,
        onLongClick = { showDropdownMenu = true },
        selected = isCurrentServer,
    ) {
        ListItemContent(
            headlineText = server.name,
            subHeadlineText = buildServerUrl(server),
            leadingContent = {
                Icon(
                    imageVector = Icons.Rounded.Dns,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            },
        )

        OptionsMenuBottomSheet(
            options = listOf(
                if (server.defaultServer == true) {
                    OptionsMenuBottomSheetItem(
                        title = stringResource(R.string.default_server),
                        icon = Icons.Rounded.Star,
                        onClick = { showDropdownMenu = false },
                        disabled = true
                    )
                } else {
                    OptionsMenuBottomSheetItem(
                        title = stringResource(R.string.set_as_default_server),
                        icon = Icons.Rounded.Star,
                        onClick =  {onSetDefault() },
                    )
                },
                OptionsMenuBottomSheetItem(
                    title = stringResource(R.string.delete_server),
                    icon = Icons.Rounded.Delete,
                    onClick = { showDeleteConfirmation = true },
                    role = OptionsMenuBottomSheetItemRole.DESTRUCTIVE,
                )
            ),
            showMenu = showDropdownMenu,
            onDismiss = { showDropdownMenu = false }
        )
    }


    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text(stringResource(R.string.delete_server)) },
            text = { Text(stringResource(R.string.delete_server_confirm)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmation = false
                        onDelete()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}
