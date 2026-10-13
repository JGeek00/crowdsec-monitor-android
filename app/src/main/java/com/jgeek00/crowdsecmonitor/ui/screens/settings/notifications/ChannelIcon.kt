package com.jgeek00.crowdsecmonitor.ui.screens.settings.notifications

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.jgeek00.crowdsecmonitor.data.models.NotificationChannelType

/**
 * Maps a channel type to an icon, with a generic bell fallback for provider
 * types the app does not know yet.
 */
@Composable
fun ChannelIcon(
    type: NotificationChannelType,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Icon(
        imageVector = channelIconVector(type.value),
        contentDescription = null,
        modifier = modifier,
        tint = tint
    )
}

fun channelIconVector(icon: String): ImageVector = when (icon) {
    "ntfy" -> Icons.Rounded.Campaign
    "email" -> Icons.Rounded.Email
    else -> Icons.Rounded.Notifications
}
