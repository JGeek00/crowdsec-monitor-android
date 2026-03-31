package com.jgeek00.crowdsecmonitor.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jgeek00.crowdsecmonitor.R
import com.jgeek00.crowdsecmonitor.constants.Enums
import com.jgeek00.crowdsecmonitor.data.models.LoadingResult
import com.jgeek00.crowdsecmonitor.data.models.StatisticsResponse
import com.jgeek00.crowdsecmonitor.ui.screens.dashboard.DashboardBarChart
import com.jgeek00.crowdsecmonitor.ui.screens.dashboard.DashboardContentPhone
import com.jgeek00.crowdsecmonitor.ui.screens.dashboard.DashboardContentTablet
import com.jgeek00.crowdsecmonitor.ui.screens.dashboard.DashboardItem
import com.jgeek00.crowdsecmonitor.ui.screens.dashboard.DashboardSummaryItem
import com.jgeek00.crowdsecmonitor.ui.screens.dashboard.StyledListContainer
import com.jgeek00.crowdsecmonitor.viewmodel.DashboardViewModel
import com.jgeek00.crowdsecmonitor.viewmodel.ServerStatusViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToFullList: (Enums.DashboardItemType) -> Unit,
    dashboardViewModel: DashboardViewModel = hiltViewModel(),
    serverStatusViewModel: ServerStatusViewModel = hiltViewModel()
) {
    val isTablet = LocalConfiguration.current.screenWidthDp >= 600
    var lapiOnlineAlertPresented by remember { mutableStateOf(false) }
    var lapiOfflineAlertPresented by remember { mutableStateOf(false) }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        topBar = {
            LargeTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                ),
                scrollBehavior = scrollBehavior,
                title = { Text(stringResource(R.string.dashboard)) },
                actions = {
                    when (val status = serverStatusViewModel.status) {
                        is LoadingResult.Loading -> {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .size(20.dp)
                                    .padding(end = 4.dp),
                                strokeWidth = 2.dp
                            )
                        }
                        is LoadingResult.Success -> {
                            if (status.value.csLapi.lapiConnected) {
                                IconButton(onClick = { lapiOnlineAlertPresented = true }) {
                                    Icon(
                                        imageVector = Icons.Rounded.CheckCircle,
                                        contentDescription = stringResource(R.string.lapi_online_title),
                                        tint = Color(0xFF4CAF50)
                                    )
                                }
                            } else {
                                IconButton(onClick = { lapiOfflineAlertPresented = true }) {
                                    Icon(
                                        imageVector = Icons.Rounded.Error,
                                        contentDescription = stringResource(R.string.lapi_offline_title),
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                        is LoadingResult.Failure -> {
                            IconButton(onClick = { lapiOfflineAlertPresented = true }) {
                                Icon(
                                    imageVector = Icons.Rounded.Error,
                                    contentDescription = stringResource(R.string.lapi_offline_title),
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = dashboardViewModel.isRefreshing,
            onRefresh = { dashboardViewModel.refresh() },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val state = dashboardViewModel.state) {
                is LoadingResult.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is LoadingResult.Success -> {
                    if (isTablet) {
                        DashboardContentTablet(data = state.value, onNavigateToFullList = onNavigateToFullList)
                    } else {
                        DashboardContentPhone(data = state.value, onNavigateToFullList = onNavigateToFullList)
                    }
                }
                is LoadingResult.Failure -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Icon(Icons.Rounded.Error, null, modifier = Modifier.size(56.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(stringResource(R.string.error_fetching_data), style = MaterialTheme.typography.bodyLarge)
                            IconButton(onClick = { dashboardViewModel.fetchDashboardData() }) {
                                Icon(Icons.Rounded.Refresh, contentDescription = null)
                            }
                        }
                    }
                }
            }
        }
    }

    if (lapiOnlineAlertPresented) {
        AlertDialog(
            onDismissRequest = { lapiOnlineAlertPresented = false },
            title = { Text(stringResource(R.string.lapi_online_title)) },
            text = { Text(stringResource(R.string.lapi_online_message)) },
            confirmButton = {
                TextButton(onClick = { lapiOnlineAlertPresented = false }) { Text(stringResource(R.string.ok)) }
            }
        )
    }

    if (lapiOfflineAlertPresented) {
        AlertDialog(
            onDismissRequest = { lapiOfflineAlertPresented = false },
            title = { Text(stringResource(R.string.lapi_offline_title)) },
            text = { Text(stringResource(R.string.lapi_offline_message)) },
            confirmButton = {
                TextButton(onClick = { lapiOfflineAlertPresented = false }) { Text(stringResource(R.string.ok)) }
            }
        )
    }
}
