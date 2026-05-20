package com.jgeek00.crowdsecmonitor.ui.screens.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Dns
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jgeek00.crowdsecmonitor.data.db.CSServerModel
import java.util.UUID
import com.jgeek00.crowdsecmonitor.ui.components.ListItemContent
import com.jgeek00.crowdsecmonitor.ui.components.RoundedCornersListTile
import com.jgeek00.crowdsecmonitor.utils.buildServerUrl

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerSwitcherBottomSheet(
    servers: List<CSServerModel>,
    currentServerId: UUID?,
    onServerSelected: (CSServerModel) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState()
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .size(width = 40.dp, height = 4.dp)
                    .background(
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        shape = MaterialTheme.shapes.small
                    )
            )
        },
        modifier = modifier
    ) {
        LazyColumn(
            modifier = Modifier.padding(16.dp)
        ) {
            items(servers, key = { it.id }) { server ->
                val index = servers.indexOf(server)
                val isCurrentServer = server.id == currentServerId
                RoundedCornersListTile(
                    index = index,
                    totalItems = servers.size,
                    onClick = {
                        if (!isCurrentServer) {
                            onServerSelected(server)
                            onDismiss()
                        }
                    },
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
                        trailingContent = {
                            if (isCurrentServer) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        },
                    )
                }
            }
        }
        Spacer(modifier = Modifier.navigationBarsPadding())
    }
}
