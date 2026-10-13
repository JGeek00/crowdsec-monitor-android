package com.jgeek00.crowdsecmonitor.ui.screens.settings.notifications

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.jgeek00.crowdsecmonitor.R
import com.jgeek00.crowdsecmonitor.data.models.NotificationChannelType

/** Localized label for a channel type ("ntfy", "Email"). */
@Composable
fun ChannelTypeLabel(type: NotificationChannelType): String = stringResource(
    when (type) {
        NotificationChannelType.NTFY -> R.string.channel_type_ntfy
        NotificationChannelType.EMAIL -> R.string.channel_type_email
    }
)
