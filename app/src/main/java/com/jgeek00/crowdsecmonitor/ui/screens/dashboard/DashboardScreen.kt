package com.jgeek00.crowdsecmonitor.ui.screens.dashboard

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jgeek00.crowdsecmonitor.R
import com.jgeek00.crowdsecmonitor.constants.Enums
import com.jgeek00.crowdsecmonitor.data.models.LoadingResult
import com.jgeek00.crowdsecmonitor.ui.components.LargeTopAppBarWithRefresh
import com.jgeek00.crowdsecmonitor.ui.screens.dashboard.components.DashboardContentPhone
import com.jgeek00.crowdsecmonitor.ui.screens.dashboard.components.DashboardContentTablet
import com.jgeek00.crowdsecmonitor.ui.screens.dashboard.status.ServiceStatusScreen
import com.jgeek00.crowdsecmonitor.viewmodel.DashboardViewModel
import com.jgeek00.crowdsecmonitor.viewmodel.ServiceStatusViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToFullList: (Enums.DashboardItemType) -> Unit,
    dashboardViewModel: DashboardViewModel = hiltViewModel(),
) {
    val isTablet = LocalConfiguration.current.screenWidthDp >= 600
    var statusScreenPresented by remember { mutableStateOf(false) }

    LargeTopAppBarWithRefresh(
        title = { Text(stringResource(R.string.dashboard)) },
        isRefreshing = dashboardViewModel.isRefreshing,
        onRefresh = { dashboardViewModel.refresh() },
        actions = {
            TextButton(
                onClick = { statusScreenPresented = true },
            ) {
                Text(stringResource(R.string.status))
            }
        }
    ) {
        AnimatedContent(
            targetState = dashboardViewModel.state,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            contentKey = { it::class },
            label = "DashboardState"
        ) { state ->
            when (state) {
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
                    Box(
                        Modifier
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
                                null,
                                modifier = Modifier.size(56.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                stringResource(R.string.error_fetching_data),
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center
                            )
                            IconButton(onClick = { dashboardViewModel.fetchDashboardData() }) {
                                Icon(Icons.Rounded.Refresh, contentDescription = null)
                            }
                        }
                    }
                }
            }
        }
    }

    if (statusScreenPresented) {
        ServiceStatusScreen(
            onClose = { statusScreenPresented = false },
        )
    }
}
