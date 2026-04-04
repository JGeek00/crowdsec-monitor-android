package com.jgeek00.crowdsecmonitor.ui.screens.decisions.list

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jgeek00.crowdsecmonitor.R
import com.jgeek00.crowdsecmonitor.data.models.LoadingResult
import com.jgeek00.crowdsecmonitor.ui.components.LargeTopAppBarWithRefresh
import com.jgeek00.crowdsecmonitor.ui.screens.decisions.components.DecisionListItem
import com.jgeek00.crowdsecmonitor.ui.screens.decisions.components.NoDecisions
import com.jgeek00.crowdsecmonitor.ui.screens.decisions.components.filters.DecisionsFiltersSheet
import com.jgeek00.crowdsecmonitor.ui.screens.decisions.create.CreateDecisionFormScreen
import com.jgeek00.crowdsecmonitor.viewmodel.DecisionsListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DecisionsListPane(
    viewModel: DecisionsListViewModel,
    onNavigateToDetails: (Int) -> Unit
) {
    val listState = rememberLazyListState()
    var showFiltersSheet by remember { mutableStateOf(false) }
    var showCreateDecisionForm by remember { mutableStateOf(false) }

    LargeTopAppBarWithRefresh(
        title = { Text(stringResource(R.string.decisions)) },
        isRefreshing = viewModel.isRefreshing,
        onRefresh = { viewModel.refreshDecisions() },
        actions = {
            TooltipBox(
                positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Below),
                tooltip = { PlainTooltip { Text(stringResource(R.string.create_decision)) } },
                state = rememberTooltipState()
            ) {
                IconButton(onClick = { showCreateDecisionForm = true }) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = stringResource(R.string.create_decision)
                    )
                }
            }
            if (viewModel.state is LoadingResult.Success) {
                TooltipBox(
                    positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Below),
                    tooltip = { PlainTooltip { Text(stringResource(R.string.filters)) } },
                    state = rememberTooltipState()
                ) {
                    IconButton(onClick = {
                        viewModel.resetFiltersPanelToAppliedOnes()
                        showFiltersSheet = true
                    }) {
                        Icon(
                            imageVector = Icons.Rounded.FilterList,
                            contentDescription = stringResource(R.string.filters)
                        )
                    }
                }
            }
        }
    ) {
        AnimatedContent(
            targetState = viewModel.state,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            contentKey = { it::class },
            label = "DecisionsListState"
        ) { state ->
            when (state) {
                is LoadingResult.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is LoadingResult.Success -> {
                    val data = state.value
                    if (data.items.isEmpty()) {
                        NoDecisions(
                            showingOnlyActive = viewModel.filters.onlyActive == true
                        )
                    } else {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            items(data.items, key = { it.id }) { decision ->
                                val index = data.items.indexOf(decision)
                                DecisionListItem(
                                    index = index,
                                    totalListAmount = data.items.size,
                                    decision = decision,
                                    viewModel = viewModel,
                                    disableTimerAnimation = viewModel.disableDecisionTimerAnimation,
                                    onNavigateToDetails = { onNavigateToDetails(decision.id) }
                                )
                                LaunchedEffect(decision.id) {
                                    if (decision == data.items.last()) {
                                        viewModel.fetchMore()
                                    }
                                }
                            }
                            if (viewModel.isLoadingMore) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(28.dp),
                                            strokeWidth = 3.dp
                                        )
                                    }
                                }
                            }
                            item { Spacer(modifier = Modifier.height(16.dp)) }
                        }
                    }
                }

                is LoadingResult.Failure -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                Icons.Rounded.Error,
                                contentDescription = null,
                                modifier = Modifier.size(56.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = stringResource(R.string.error_fetching_data),
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center
                            )
                            IconButton(onClick = { viewModel.initialFetchDecisions() }) {
                                Icon(Icons.Rounded.Refresh, contentDescription = null)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showFiltersSheet) {
        DecisionsFiltersSheet(
            viewModel = viewModel,
            onDismiss = { showFiltersSheet = false }
        )
    }

    if (showCreateDecisionForm) {
        CreateDecisionFormScreen(
            onClose = { showCreateDecisionForm = false }
        )
    }
}