package com.jgeek00.crowdsecmonitor.ui.screens.alerts.components.event

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.jgeek00.crowdsecmonitor.R
import com.jgeek00.crowdsecmonitor.data.models.AlertDetailsEvent
import com.jgeek00.crowdsecmonitor.extensions.toFormattedDateTime
import com.jgeek00.crowdsecmonitor.ui.components.RoundedCornersListTile

private val HttpBlue = Color(0xFF2196F3)
private val HttpStatusRed = Color(0xFFF44336)
private val SshOrange = Color(0xFFFF9800)
private val NetworkPurple = Color(0xFF9C27B0)
private val GenericGray = Color(0xFF9E9E9E)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun EventItem(
    event: AlertDetailsEvent,
    index: Int,
    total: Int
) {
    var showFullDetails by remember { mutableStateOf(false) }

    val meta = event.meta
    val targetFqdn = meta.firstOrNull { it.key == "target_fqdn" }?.value?.firstOrNull()
    val logType = meta.firstOrNull { it.key == "log_type" }
    val service = meta.firstOrNull { it.key == "service" }?.value?.firstOrNull()
    val httpVerb = meta.firstOrNull { it.key == "http_verb" }?.value?.firstOrNull()
    val httpPath = meta.firstOrNull { it.key == "http_path" }?.value?.firstOrNull()
    val httpStatus = meta.firstOrNull { it.key == "http_status" }?.value?.firstOrNull()
    val httpUserAgent = meta.firstOrNull { it.key == "http_user_agent" }?.value?.firstOrNull()
    val datasourcePath = meta.firstOrNull { it.key == "datasource_path" }?.value?.firstOrNull()
    val asnOrg = meta.firstOrNull { it.key == "ASNOrg" }?.value?.firstOrNull()
    val logTypeValue = logType?.value?.firstOrNull()

    RoundedCornersListTile(
        index = index,
        totalItems = total,
        onClick = { showFullDetails = true },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (targetFqdn != null) {
                Text(
                    text = targetFqdn,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End
                )
            }

            when {
                // HTTP event
                httpVerb != null && httpPath != null && httpStatus != null && httpUserAgent != null -> {
                    HttpEventContent(
                        httpVerb = httpVerb,
                        httpPath = httpPath,
                        httpStatus = httpStatus,
                        httpUserAgent = httpUserAgent,
                        datasourcePath = datasourcePath
                    )
                }
                // SSH event
                logType?.value?.contains("ssh") == true -> {
                    ServiceEventContent(
                        service = service,
                        logTypeValue = logTypeValue,
                        datasourcePath = datasourcePath,
                        badgeColor = SshOrange
                    )
                }
                // Port scan / Network event
                logType != null && service != null -> {
                    ServiceEventContent(
                        service = service,
                        logTypeValue = logTypeValue,
                        datasourcePath = datasourcePath,
                        badgeColor = NetworkPurple
                    )
                }
                // Generic fallback
                logType != null -> {
                    GenericEventContent(
                        service = service,
                        logTypeValue = logTypeValue,
                        asnOrg = asnOrg
                    )
                }
                // Raw fallback
                else -> {
                    Text(
                        text = event.timestamp.toFormattedDateTime(),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }

    if (showFullDetails) {
        EventFullDetailsSheet(event = event, onDismiss = { showFullDetails = false })
    }
}


@Composable
private fun HttpEventContent(
    httpVerb: String,
    httpPath: String,
    httpStatus: String,
    httpUserAgent: String,
    datasourcePath: String?
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            EventBadge(text = httpVerb, color = HttpBlue)
            Text(
                text = httpPath,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            EventBadge(text = httpStatus, color = HttpStatusRed)
        }
        val userAgentText = if (httpUserAgent == "-") {
            stringResource(R.string.user_agent_not_available)
        } else {
            httpUserAgent
        }
        EventMetaRow(icon = Icons.Rounded.Language, text = userAgentText)
        if (datasourcePath != null) {
            EventMetaRow(icon = Icons.Rounded.Description, text = datasourcePath)
        }
    }
}

@Composable
private fun ServiceEventContent(
    service: String?,
    logTypeValue: String?,
    datasourcePath: String?,
    badgeColor: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (service != null) EventBadge(text = service.uppercase(), color = badgeColor)
            if (logTypeValue != null) {
                Text(
                    text = logTypeValue,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        if (datasourcePath != null) {
            EventMetaRow(icon = Icons.Rounded.Description, text = datasourcePath)
        }
    }
}

@Composable
private fun GenericEventContent(
    service: String?,
    logTypeValue: String?,
    asnOrg: String?
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (service != null) EventBadge(text = service.uppercase(), color = GenericGray)
            if (logTypeValue != null) {
                Text(
                    text = logTypeValue,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        if (asnOrg != null) {
            EventMetaRow(icon = Icons.Rounded.Info, text = asnOrg)
        }
    }
}


@Composable
private fun EventBadge(text: String, color: Color) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        color = Color.White,
        modifier = Modifier
            .background(color, RoundedCornerShape(50))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    )
}

@Composable
private fun EventMetaRow(
    icon: ImageVector,
    text: String,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(12.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}


