package com.jgeek00.crowdsecmonitor.ui.screens.dashboard

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jgeek00.crowdsecmonitor.R
import com.jgeek00.crowdsecmonitor.constants.Enums
import com.jgeek00.crowdsecmonitor.data.models.LoadingResult
import com.jgeek00.crowdsecmonitor.viewmodel.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullListDashboardScreen(
    itemType: Enums.DashboardItemType,
    onBack: () -> Unit,
    dashboardViewModel: DashboardViewModel = hiltViewModel()
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    val title = when (itemType) {
        Enums.DashboardItemType.COUNTRY -> stringResource(R.string.top_countries)
        Enums.DashboardItemType.IP_OWNER -> stringResource(R.string.top_ip_owners)
        Enums.DashboardItemType.SCENARIO -> stringResource(R.string.top_scenarios)
        Enums.DashboardItemType.TARGET -> stringResource(R.string.top_targets)
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text(title) },
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { innerPadding ->
        val data = (dashboardViewModel.state as? LoadingResult.Success)?.value

        if (data != null) {
            val items: List<Pair<String, Int>> = when (itemType) {
                Enums.DashboardItemType.COUNTRY -> data.topCountries.map { it.countryCode to it.amount }
                Enums.DashboardItemType.IP_OWNER -> data.topIpOwners.map { it.ipOwner to it.amount }
                Enums.DashboardItemType.SCENARIO -> data.topScenarios.map { it.scenario to it.amount }
                Enums.DashboardItemType.TARGET -> data.topTargets.map { it.target to it.amount }
            }
            val total = items.sumOf { it.second }.coerceAtLeast(1)

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
            ) {
                itemsIndexed(items) { index, (label, amount) ->
                    DashboardItem(
                        index = index,
                        listLength = items.size,
                        itemType = itemType,
                        label = label,
                        amount = amount,
                        percentage = amount.toDouble() / total
                    )
                }
            }
        }
    }
}

