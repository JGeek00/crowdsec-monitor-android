package com.jgeek00.crowdsecmonitor.ui.screens.alerts

import android.content.Context
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import com.jgeek00.crowdsecmonitor.R
import com.jgeek00.crowdsecmonitor.constants.Enums
import com.jgeek00.crowdsecmonitor.constants.URLs
import com.jgeek00.crowdsecmonitor.data.models.AlertDetailsDecision
import com.jgeek00.crowdsecmonitor.data.models.AlertDetailsResponse
import com.jgeek00.crowdsecmonitor.data.models.LoadingResult
import com.jgeek00.crowdsecmonitor.extensions.toFormattedDate
import com.jgeek00.crowdsecmonitor.ui.components.CountryFlag
import com.jgeek00.crowdsecmonitor.ui.components.DataListTile
import com.jgeek00.crowdsecmonitor.ui.components.SectionHeader
import com.jgeek00.crowdsecmonitor.ui.screens.alerts.components.event.EventItem
import com.jgeek00.crowdsecmonitor.utils.reverseGeocode
import com.jgeek00.crowdsecmonitor.viewmodel.AlertDetailsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertDetailsScreen(
    alertId: Int,
    showBackButton: Boolean = false,
    onNavigateBack: () -> Unit = {}
) {
    val viewModel: AlertDetailsViewModel = hiltViewModel(key = alertId.toString())
    val context = LocalContext.current

    LaunchedEffect(alertId) {
        viewModel.initialize(alertId)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                ),
                title = {
                    Text(text = stringResource(R.string.alert_title, alertId))
                },
                navigationIcon = {
                    if (showBackButton) {
                        TooltipBox(
                            positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                                TooltipAnchorPosition.Below
                            ),
                            tooltip = { PlainTooltip { Text(stringResource(R.string.back)) } },
                            state = rememberTooltipState()
                        ) {
                            IconButton(onClick = onNavigateBack) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                    contentDescription = stringResource(R.string.back)
                                )
                            }
                        }
                    }
                },
                actions = {
                    TooltipBox(
                        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                            TooltipAnchorPosition.Below
                        ),
                        tooltip = { PlainTooltip { Text(stringResource(R.string.refresh)) } },
                        state = rememberTooltipState()
                    ) {
                        IconButton(onClick = { viewModel.refresh(alertId) }) {
                            Icon(
                                imageVector = Icons.Rounded.Refresh,
                                contentDescription = stringResource(R.string.refresh)
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        AnimatedContent(
            targetState = viewModel.state,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            contentKey = { it::class },
            label = "AlertDetailsState"
        ) { state ->
            when (state) {
                is LoadingResult.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is LoadingResult.Success -> {
                    AlertDetailsContent(
                        data = state.value,
                        innerPadding = innerPadding,
                        isRefreshing = viewModel.isRefreshing,
                        onRefresh = { viewModel.refresh(alertId) },
                        context = context
                    )
                }

                is LoadingResult.Failure -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Error,
                                contentDescription = null,
                                modifier = Modifier.size(56.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = stringResource(R.string.error_fetching_data),
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center
                            )
                            IconButton(onClick = { viewModel.refresh(alertId) }) {
                                Icon(Icons.Rounded.Refresh, contentDescription = null)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun AlertDetailsContent(
    data: AlertDetailsResponse,
    innerPadding: PaddingValues,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
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

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            item {
                SectionHeader(
                    text = stringResource(R.string.message),
                    topPadding = Enums.SectionHeaderPaddingTop.NONE
                )
            }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = data.message,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            item {
                SectionHeader(text = stringResource(R.string.scenario))
            }
            item {
                val scenarioParts = data.scenario.split("/")
                val author = scenarioParts.getOrNull(0) ?: data.scenario
                val name = scenarioParts.getOrNull(1) ?: ""
                val hubUrl = URLs.crowdsecHubScenario(data.scenario)

                val scenarioGroupSize = buildList {
                    add(Unit) // link row always present
                    if (data.scenarioVersion.isNotBlank()) add(Unit)
                    add(Unit) // capacity
                    add(Unit) // leakspeed
                }.size

                var idx = 0

                SegmentedListItem(
                    onClick = {
                        CustomTabsIntent.Builder()
                            .setShowTitle(true)
                            .build()
                            .launchUrl(context, hubUrl.toUri())
                    },
                    shapes = ListItemDefaults.segmentedShapes(index = idx, count = scenarioGroupSize),
                    modifier = Modifier.padding(bottom = if (0 < scenarioGroupSize - 1) 2.dp else 0.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            if (name.isNotBlank()) {
                                Text(
                                    text = author,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = name,
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
                idx++

                if (data.scenarioVersion.isNotBlank()) {
                    DataListTile(
                        tileIndex = idx,
                        groupTiles = scenarioGroupSize,
                        title = stringResource(R.string.version),
                        subtitle = data.scenarioVersion
                    )
                    idx++
                }

                DataListTile(
                    tileIndex = idx,
                    groupTiles = scenarioGroupSize,
                    title = stringResource(R.string.capacity),
                    subtitle = data.capacity.toString()
                )
                idx++

                DataListTile(
                    tileIndex = idx,
                    groupTiles = scenarioGroupSize,
                    title = stringResource(R.string.leakspeed),
                    subtitle = data.leakspeed
                )
            }

            item {
                SectionHeader(text = stringResource(R.string.origin))
            }
            item {
                val originGroupSize = buildList {
                    add(Unit) // ip address
                    if (!data.source.cn.isNullOrBlank()) add(Unit) // country
                    if (data.source.latitude != null && data.source.longitude != null) add(Unit) // location
                    if (!data.source.asName.isNullOrBlank()) add(Unit) // ip owner
                }.size

                var idx = 0

                DataListTile(
                    tileIndex = idx,
                    groupTiles = originGroupSize,
                    title = stringResource(R.string.ip_address),
                    subtitle = data.source.value
                )
                idx++

                if (!data.source.cn.isNullOrBlank()) {
                    DataListTile(
                        tileIndex = idx,
                        groupTiles = originGroupSize,
                        title = stringResource(R.string.country),
                        subtitleComponent = { CountryFlag(countryCode = data.source.cn) }
                    )
                    idx++
                }

                if (data.source.latitude != null && data.source.longitude != null) {
                    DataListTile(
                        tileIndex = idx,
                        groupTiles = originGroupSize,
                        title = stringResource(R.string.location),
                        subtitleComponent = {
                            when (geocodedLocation) {
                                is LoadingResult.Loading -> CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                                is LoadingResult.Success -> Text(
                                    text = (geocodedLocation as LoadingResult.Success<String>).value,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                is LoadingResult.Failure -> Text(
                                    text = stringResource(R.string.not_available),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    )
                    idx++
                }

                if (!data.source.asName.isNullOrBlank()) {
                    DataListTile(
                        tileIndex = idx,
                        groupTiles = originGroupSize,
                        title = stringResource(R.string.ip_owner),
                        subtitle = data.source.asName
                    )
                }
            }

            if (data.decisions.isNotEmpty()) {
                item {
                    SectionHeader(text = stringResource(R.string.decisions))
                }
                items(data.decisions, key = { it.id }) { decision ->
                    DecisionItem(
                        decision = decision,
                        index = data.decisions.indexOf(decision),
                        total = data.decisions.size
                    )
                }
            }

            if (data.events.isNotEmpty()) {
                item {
                    SectionHeader(text = stringResource(R.string.events))
                }
                items(data.events.size) { index ->
                    val event = data.events[index]
                    EventItem(
                        event = event,
                        index = index,
                        total = data.events.size
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun DecisionItem(
    decision: AlertDetailsDecision,
    index: Int,
    total: Int
) {
    DataListTile(
        tileIndex = index,
        groupTiles = total,
        title = "${decision.type} · ${decision.value}",
        subtitle = "${decision.scenario}  •  ${stringResource(R.string.expires)} ${decision.expiration.toFormattedDate()}"
    )
}





