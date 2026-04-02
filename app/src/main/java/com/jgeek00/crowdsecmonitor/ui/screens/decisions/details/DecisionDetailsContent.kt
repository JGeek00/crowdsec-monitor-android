package com.jgeek00.crowdsecmonitor.ui.screens.decisions.details

import android.content.Context
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.jgeek00.crowdsecmonitor.R
import com.jgeek00.crowdsecmonitor.constants.Enums
import com.jgeek00.crowdsecmonitor.constants.URLs
import com.jgeek00.crowdsecmonitor.data.models.DecisionItemResponse
import com.jgeek00.crowdsecmonitor.data.models.LoadingResult
import com.jgeek00.crowdsecmonitor.data.models.toAlertsListResponseAlert
import com.jgeek00.crowdsecmonitor.ui.components.CountryFlag
import com.jgeek00.crowdsecmonitor.ui.components.ListItemContent
import com.jgeek00.crowdsecmonitor.ui.components.RoundedCornersListTile
import com.jgeek00.crowdsecmonitor.ui.components.SectionHeader
import com.jgeek00.crowdsecmonitor.ui.screens.alerts.components.AlertListItem
import com.jgeek00.crowdsecmonitor.ui.screens.decisions.components.DecisionTimer
import com.jgeek00.crowdsecmonitor.ui.screens.decisions.components.DecisionTypeChip
import com.jgeek00.crowdsecmonitor.utils.reverseGeocode

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DecisionDetailsContent(
    data: DecisionItemResponse,
    innerPadding: PaddingValues,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onNavigateToAlert: ((Int) -> Unit)?,
    nestedScrollConnection: NestedScrollConnection,
    context: Context
) {
    var geocodedLocation by remember { mutableStateOf<LoadingResult<String>>(LoadingResult.Loading) }

    LaunchedEffect(data.source.latitude, data.source.longitude) {
        if (data.source.latitude != null && data.source.longitude != null) {
            val result = reverseGeocode(context, data.source.latitude, data.source.longitude)
            geocodedLocation = if (result != null) {
                LoadingResult.Success(result)
            } else {
                LoadingResult.Failure(Exception())
            }
        }
    }

    val scenarioParts = remember(data.scenario) { data.scenario.split("/") }
    val scenarioAuthor = scenarioParts.getOrNull(0) ?: data.scenario
    val scenarioName = scenarioParts.getOrNull(1) ?: ""
    val hubUrl = remember(data.scenario) { URLs.crowdsecHubScenario(data.scenario) }

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
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                item {
                    SectionHeader(
                        text = stringResource(R.string.general_information),
                        topPadding = Enums.SectionHeaderPaddingTop.NONE
                    )
                }
                item {
                    RoundedCornersListTile(
                        index = 0,
                        totalItems = 3,
                    ) {
                        ListItemContent(
                            headlineText = stringResource(R.string.type),
                            trailingContent = { DecisionTypeChip(decisionType = data.type) }
                        )
                    }

                    RoundedCornersListTile(
                        index = 1,
                        totalItems = 3,
                        onClick = {
                            CustomTabsIntent.Builder()
                                .setShowTitle(true)
                                .build()
                                .launchUrl(context, hubUrl.toUri())
                        },
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                if (scenarioName.isNotBlank()) {
                                    Text(
                                        text = scenarioAuthor,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = scenarioName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                } else {
                                    Text(
                                        text = data.scenario,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.OpenInNew,
                                contentDescription = stringResource(R.string.open_in_browser),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    RoundedCornersListTile(
                        index = 2,
                        totalItems = 3,
                    ) {
                        ListItemContent(
                            headlineText = stringResource(R.string.remaining_time),
                            trailingContent = { DecisionTimer(expiration = data.expiration) }
                        )
                    }
                }

                item { SectionHeader(text = stringResource(R.string.origin)) }
                item {
                    val originGroupSize = buildList {
                        add(Unit) // ip address
                        if (!data.source.asName.isNullOrBlank()) add(Unit)
                        if (!data.source.cn.isNullOrBlank()) add(Unit)
                        if (data.source.latitude != null && data.source.longitude != null) add(Unit)
                    }.size
                    var idx = 0

                    RoundedCornersListTile(
                        index = idx,
                        totalItems = originGroupSize,
                    ) {
                        ListItemContent(
                            headlineText = stringResource(R.string.ip_address),
                            subHeadlineText = data.source.ip ?: data.source.value
                        )
                    }
                    idx++

                    if (!data.source.asName.isNullOrBlank()) {
                        RoundedCornersListTile(
                            index = idx,
                            totalItems = originGroupSize,
                        ) {
                            ListItemContent(
                                headlineText = stringResource(R.string.ip_owner),
                                subHeadlineText = data.source.asName
                            )
                        }
                        idx++
                    }

                    if (!data.source.cn.isNullOrBlank()) {
                        RoundedCornersListTile(
                            index = idx,
                            totalItems = originGroupSize,
                        ) {
                            ListItemContent(
                                headlineText = stringResource(R.string.country),
                                subHeadlineComponent = { CountryFlag(countryCode = data.source.cn) }
                            )
                        }
                        idx++
                    }

                    if (data.source.latitude != null && data.source.longitude != null) {
                        RoundedCornersListTile(
                            index = idx,
                            totalItems = originGroupSize,
                        ) {
                            ListItemContent(
                                headlineText = stringResource(R.string.location),
                                subHeadlineComponent = {
                                    when (geocodedLocation) {
                                        is LoadingResult.Loading -> CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            strokeWidth = 2.dp
                                        )

                                        is LoadingResult.Success -> Text(
                                            text = (geocodedLocation as LoadingResult.Success<String>).value,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )

                                        is LoadingResult.Failure -> Text(
                                            text = stringResource(R.string.not_available),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            )
                        }
                    }
                }

                item { SectionHeader(text = stringResource(R.string.alert)) }
                item {
                    AlertListItem(
                        index = 0,
                        totalListAmount = 1,
                        alert = data.alert.toAlertsListResponseAlert(),
                        viewModel = null,
                        onNavigateToDetails = if (onNavigateToAlert != null) {
                            { onNavigateToAlert.invoke(data.alertId) }
                        } else null
                    )
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

