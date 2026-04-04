package com.jgeek00.crowdsecmonitor.ui.screens.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jgeek00.crowdsecmonitor.R
import com.jgeek00.crowdsecmonitor.constants.chartColors
import com.jgeek00.crowdsecmonitor.constants.Enums
import com.jgeek00.crowdsecmonitor.data.models.LoadingResult
import com.jgeek00.crowdsecmonitor.ui.screens.dashboard.components.DashboardItem
import com.jgeek00.crowdsecmonitor.ui.screens.dashboard.components.DashboardPieChart
import com.jgeek00.crowdsecmonitor.utils.DashboardItemDataForView
import com.jgeek00.crowdsecmonitor.viewmodel.FullListDashboardViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FullListDashboardScreen(
    itemType: Enums.DashboardItemType,
    onBack: () -> Unit,
    viewModel: FullListDashboardViewModel = hiltViewModel<FullListDashboardViewModel, FullListDashboardViewModel.Factory>(
        creationCallback = { factory -> factory.create(itemType) }
    )
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val isTablet = LocalConfiguration.current.screenWidthDp >= 600

    val title = when (itemType) {
        Enums.DashboardItemType.COUNTRY -> stringResource(R.string.top_countries)
        Enums.DashboardItemType.IP_OWNER -> stringResource(R.string.top_ip_owners)
        Enums.DashboardItemType.SCENARIO -> stringResource(R.string.top_scenarios)
        Enums.DashboardItemType.TARGET -> stringResource(R.string.top_targets)
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text(title) },
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                ),
            )
        }
    ) { innerPadding ->
        when (val state = viewModel.state) {
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
                if (isTablet) {
                    TabletContent(
                        data = state.value,
                        chartData = viewModel.chartData,
                        itemType = itemType,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    )
                } else {
                    PhoneContent(
                        data = state.value,
                        chartData = viewModel.chartData,
                        itemType = itemType,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    )
                }
            }

            is LoadingResult.Failure -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
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
                        IconButton(onClick = { viewModel.fetchData() }) {
                            Icon(Icons.Rounded.Refresh, contentDescription = null)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PhoneContent(
    data: List<DashboardItemDataForView>,
    chartData: List<DashboardItemDataForView>,
    itemType: Enums.DashboardItemType,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    var highlightedIndex by remember { mutableStateOf<Int?>(null) }
    var blinkVisible by remember { mutableStateOf(false) }

    // Scroll to item (centered) and blink it when highlightedIndex changes
    LaunchedEffect(highlightedIndex) {
        val idx = highlightedIndex ?: return@LaunchedEffect
        if (idx >= data.size) return@LaunchedEffect
        // +1 because index 0 in the LazyColumn is the pie chart card
        val scrollIndex = idx + 1
        val viewportHeight = listState.layoutInfo.viewportSize.height
        val itemSize = listState.layoutInfo.visibleItemsInfo
            .find { it.index == scrollIndex }?.size ?: 0
        val centerOffset = -(viewportHeight / 2 - itemSize / 2)
        listState.animateScrollToItem(scrollIndex, scrollOffset = centerOffset)
        repeat(3) {
            blinkVisible = true
            delay(180)
            blinkVisible = false
            delay(120)
        }
        highlightedIndex = null
    }

    LazyColumn(state = listState, modifier = modifier) {
        // Pie chart card at the top
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    DashboardPieChart(
                        data = chartData,
                        onSliceClick = { sliceIndex ->
                            // Slices 0..chartColors.size-1 map 1:1 to data.
                            // Slice chartColors.size is "Others" → scroll to the first item
                            // of the Others group, which starts at index chartColors.size in data.
                            val dataIndex = if (sliceIndex < chartColors.size) sliceIndex else chartColors.size
                            if (dataIndex < data.size) highlightedIndex = dataIndex
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // List items
        itemsIndexed(data) { index, item ->
            DashboardItem(
                index = index,
                listLength = data.size,
                itemType = itemType,
                label = item.item,
                amount = item.value,
                percentage = item.percentage,
                color = item.color,
                isHighlighted = highlightedIndex == index && blinkVisible,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun TabletContent(
    data: List<DashboardItemDataForView>,
    chartData: List<DashboardItemDataForView>,
    itemType: Enums.DashboardItemType,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    var highlightedIndex by remember { mutableStateOf<Int?>(null) }
    var blinkVisible by remember { mutableStateOf(false) }

    LaunchedEffect(highlightedIndex) {
        val idx = highlightedIndex ?: return@LaunchedEffect
        if (idx >= data.size) return@LaunchedEffect
        val viewportHeight = listState.layoutInfo.viewportSize.height
        val itemSize = listState.layoutInfo.visibleItemsInfo
            .find { it.index == idx }?.size ?: 0
        val centerOffset = -(viewportHeight / 2 - itemSize / 2)
        listState.animateScrollToItem(idx, scrollOffset = centerOffset)
        repeat(3) {
            blinkVisible = true
            delay(180)
            blinkVisible = false
            delay(120)
        }
        highlightedIndex = null
    }

    Row(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        // List on the left (~65%)
        LazyColumn(state = listState, modifier = Modifier.weight(0.65f)) {
            itemsIndexed(data) { index, item ->
                DashboardItem(
                    index = index,
                    listLength = data.size,
                    itemType = itemType,
                    label = item.item,
                    amount = item.value,
                    percentage = item.percentage,
                    color = item.color,
                    isHighlighted = highlightedIndex == index && blinkVisible
                )
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }

        // Pie chart on the right (~35%)
        Card(modifier = Modifier.weight(0.35f)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                DashboardPieChart(
                    data = chartData,
                    onSliceClick = { sliceIndex ->
                        val dataIndex = if (sliceIndex < chartColors.size) sliceIndex else chartColors.size
                        if (dataIndex < data.size) highlightedIndex = dataIndex
                    }
                )
            }
        }
    }
}
