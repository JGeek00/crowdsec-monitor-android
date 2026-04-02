package com.jgeek00.crowdsecmonitor.ui.screens.lists.blocklists.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.FormatListBulleted
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jgeek00.crowdsecmonitor.R
import com.jgeek00.crowdsecmonitor.constants.Defaults
import com.jgeek00.crowdsecmonitor.constants.Enums
import com.jgeek00.crowdsecmonitor.data.models.BlocklistDataResponseData
import com.jgeek00.crowdsecmonitor.data.models.BlocklistType
import com.jgeek00.crowdsecmonitor.extensions.toFormattedDateTime
import com.jgeek00.crowdsecmonitor.extensions.toInstant
import com.jgeek00.crowdsecmonitor.ui.components.DataListTile
import com.jgeek00.crowdsecmonitor.ui.components.SectionHeader
import kotlin.math.abs
import kotlin.math.min

@Composable
fun BlocklistDetailsContent(
    data: BlocklistDataResponseData,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    ipsRound: Int,
    innerPadding: PaddingValues,
    nestedScrollConnection: NestedScrollConnection,
    onIncrementIpsRound: () -> Unit
) {
    val refreshWarning = remember(data.lastRefreshAttempt, data.lastSuccessfulRefresh) {
        val attempt = data.lastRefreshAttempt?.toInstant() ?: return@remember false
        val successful = data.lastSuccessfulRefresh?.toInstant() ?: return@remember false
        abs(attempt.epochSecond - successful.epochSecond) >= 300L
    }

    val infoCount = remember(data, refreshWarning) {
        var count = 2
        if (data.url != null) count++
        count++
        if (data.enabled != null) count++
        if (data.addedDate != null) count++
        if (data.lastSuccessfulRefresh != null) count++
        if (refreshWarning && data.lastRefreshAttempt != null) count++
        count
    }

    val endIndex = min(ipsRound * Defaults.IPS_AMOUNT_BATCH, data.blocklistIps.size)
    val slicedIps = data.blocklistIps.subList(0, endIndex)

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(nestedScrollConnection)
        ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
        ) {
            item {
                SectionHeader(
                    text = stringResource(R.string.information),
                    topPadding = Enums.SectionHeaderPaddingTop.NONE
                )
            }
            item {
                var tileIdx = 0
                DataListTile(
                    tileIndex = tileIdx++, groupTiles = infoCount,
                    title = stringResource(R.string.name), subtitle = data.name
                )
                if (data.url != null) {
                    DataListTile(
                        tileIndex = tileIdx++, groupTiles = infoCount,
                        title = stringResource(R.string.url), subtitle = data.url
                    )
                }
                DataListTile(
                    tileIndex = tileIdx++, groupTiles = infoCount,
                    title = stringResource(R.string.amount_of_blocked_ips),
                    subtitle = "%,d".format(data.countIps)
                )
                DataListTile(
                    tileIndex = tileIdx++, groupTiles = infoCount,
                    title = stringResource(R.string.managed_by),
                    trailingContent = {
                        Text(
                            text = when (data.type) {
                                BlocklistType.API -> stringResource(R.string.monitor_api)
                                BlocklistType.CROWDSEC -> stringResource(R.string.crowdsec)
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = when (data.type) {
                                BlocklistType.API -> MaterialTheme.colorScheme.primary
                                BlocklistType.CROWDSEC -> MaterialTheme.colorScheme.tertiary
                            }
                        )
                    }
                )
                if (data.enabled != null) {
                    DataListTile(
                        tileIndex = tileIdx++, groupTiles = infoCount,
                        title = stringResource(R.string.enabled),
                        trailingContent = {
                            Icon(
                                imageVector = if (data.enabled) Icons.Rounded.CheckCircle else Icons.Rounded.Close,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = if (data.enabled) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
                            )
                        }
                    )
                }
                if (data.addedDate != null) {
                    DataListTile(
                        tileIndex = tileIdx++, groupTiles = infoCount,
                        title = stringResource(R.string.added),
                        subtitle = data.addedDate.toFormattedDateTime()
                    )
                }
                if (data.lastSuccessfulRefresh != null) {
                    DataListTile(
                        tileIndex = tileIdx++, groupTiles = infoCount,
                        title = if (refreshWarning) stringResource(R.string.last_successful_refresh)
                        else stringResource(R.string.last_refresh),
                        subtitle = data.lastSuccessfulRefresh.toFormattedDateTime()
                    )
                }
                if (refreshWarning && data.lastRefreshAttempt != null) {
                    DataListTile(
                        tileIndex = tileIdx, groupTiles = infoCount,
                        title = stringResource(R.string.last_refresh_attempt),
                        trailingContent = {
                            Text(
                                text = data.lastRefreshAttempt.toFormattedDateTime(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    )
                }
            }
            item { SectionHeader(text = stringResource(R.string.blocked_ips)) }
            if (data.blocklistIps.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.FormatListBulleted,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = stringResource(R.string.no_blocked_ips_title),
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = stringResource(R.string.no_blocked_ips_description),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(slicedIps, key = { it }) { ip ->
                    val index = slicedIps.indexOf(ip)
                    DataListTile(
                        tileIndex = index,
                        groupTiles = slicedIps.size,
                        title = ip
                    )
                    LaunchedEffect(ip) {
                        if (ip == slicedIps.last() && endIndex < data.blocklistIps.size) {
                            onIncrementIpsRound()
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
        }
    }
}
