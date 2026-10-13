package com.jgeek00.crowdsecmonitor.ui.screens.settings.notifications

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Circle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jgeek00.crowdsecmonitor.R
import com.jgeek00.crowdsecmonitor.constants.Enums
import com.jgeek00.crowdsecmonitor.data.models.NotificationChannelType
import com.jgeek00.crowdsecmonitor.ui.components.ListItemContent
import com.jgeek00.crowdsecmonitor.ui.components.RoundedCornersListTile
import com.jgeek00.crowdsecmonitor.ui.components.SectionHeader
import com.jgeek00.crowdsecmonitor.viewmodel.NotificationFormViewModel

/**
 * Channels multi-select section shared by the notification wizard (step 3) and
 * the notification details edit form. Toggling rows updates
 * `viewModel.selectedChannelIds`.
 */
@Composable
fun ChannelsSelectorSection(viewModel: NotificationFormViewModel) {
    FormInfoBox(label = stringResource(R.string.channels_step_info))
    SectionHeader(text = stringResource(R.string.channels))
    if (viewModel.channels.isEmpty()) {
        RoundedCornersListTile(index = 0, totalItems = 1) {
            Text(
                text = stringResource(R.string.no_channels_configured),
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        val channels = viewModel.channels
        channels.forEachIndexed { index, channel ->
            val selected = viewModel.selectedChannelIds.contains(channel.id)
            RoundedCornersListTile(index = index, totalItems = channels.size, onClick = {
                viewModel.selectedChannelIds = if (selected) {
                    viewModel.selectedChannelIds - channel.id
                } else {
                    viewModel.selectedChannelIds + channel.id
                }
            }) {
                ListItemContent(
                    headlineText = channel.name,
                    subHeadlineText = stringResource(
                        if (channel.type == NotificationChannelType.EMAIL) {
                            R.string.channel_type_email
                        } else {
                            R.string.channel_type_ntfy
                        }
                    ),
                    trailingContent = {
                        Icon(
                            imageVector = if (selected) Icons.Rounded.CheckCircle else Icons.Rounded.Circle,
                            contentDescription = null,
                            tint = if (selected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                )
            }
        }
    }
    Spacer(modifier = Modifier.height(24.dp))
}
