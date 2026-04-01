package com.jgeek00.crowdsecmonitor.ui.screens.dashboard.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jgeek00.crowdsecmonitor.R
import com.jgeek00.crowdsecmonitor.constants.Enums
import com.jgeek00.crowdsecmonitor.data.models.StatisticsResponse

@Composable
fun DashboardContentTablet(
    data: StatisticsResponse,
    onNavigateToFullList: (Enums.DashboardItemType) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Summary cards
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                DashboardSummaryItem(
                    type = Enums.DashboardBoxSummaryType.ALERTS,
                    value = data.alertsLast24Hours,
                    modifier = Modifier.weight(1f)
                )
                DashboardSummaryItem(
                    type = Enums.DashboardBoxSummaryType.DECISIONS,
                    value = data.activeDecisions,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Bar chart
        item { DashboardBarChart(activityHistory = data.activityHistory) }

        // First row: countries + ip owners
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                if (data.topCountries.isNotEmpty()) {
                    val total = data.topCountries.sumOf { it.amount }.coerceAtLeast(1)
                    val listLength = data.topTargets.size + 1
                    StyledListContainer(
                        listLength = listLength,
                        sectionTitle = stringResource(R.string.top_countries),
                        onViewAll = { onNavigateToFullList(Enums.DashboardItemType.COUNTRY) },
                        modifier = Modifier.weight(1f)
                    ) {
                        data.topCountries.forEach { item ->
                            val index = data.topCountries.indexOf(item)
                            DashboardItem(
                                index = index,
                                listLength = listLength,
                                itemType = Enums.DashboardItemType.COUNTRY,
                                label = item.countryCode,
                                amount = item.amount,
                                percentage = item.amount.toDouble() / total,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                }
                if (data.topIpOwners.isNotEmpty()) {
                    val total = data.topIpOwners.sumOf { it.amount }.coerceAtLeast(1)
                    val listLength = data.topTargets.size + 1
                    StyledListContainer(
                        listLength = listLength,
                        sectionTitle = stringResource(R.string.top_ip_owners),
                        onViewAll = { onNavigateToFullList(Enums.DashboardItemType.IP_OWNER) },
                        modifier = Modifier.weight(1f)
                    ) {
                        data.topIpOwners.forEach { item ->
                            val index = data.topIpOwners.indexOf(item)
                            DashboardItem(
                                index = index,
                                listLength = listLength,
                                itemType = Enums.DashboardItemType.IP_OWNER,
                                label = item.ipOwner,
                                amount = item.amount,
                                percentage = item.amount.toDouble() / total,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                }
            }
        }

        // Second row: scenarios + targets
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                if (data.topScenarios.isNotEmpty()) {
                    val total = data.topScenarios.sumOf { it.amount }.coerceAtLeast(1)
                    val listLength = data.topTargets.size + 1
                    StyledListContainer(
                        listLength = listLength,
                        sectionTitle = stringResource(R.string.top_scenarios),
                        onViewAll = { onNavigateToFullList(Enums.DashboardItemType.SCENARIO) },
                        modifier = Modifier.weight(1f)
                    ) {
                        data.topScenarios.forEach { item ->
                            val index = data.topScenarios.indexOf(item)
                            DashboardItem(
                                index = index,
                                listLength = listLength,
                                itemType = Enums.DashboardItemType.SCENARIO,
                                label = item.scenario,
                                amount = item.amount,
                                percentage = item.amount.toDouble() / total,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                }
                if (data.topTargets.isNotEmpty()) {
                    val total = data.topTargets.sumOf { it.amount }.coerceAtLeast(1)
                    val listLength = data.topTargets.size + 1
                    StyledListContainer(
                        listLength = listLength,
                        sectionTitle = stringResource(R.string.top_targets),
                        onViewAll = { onNavigateToFullList(Enums.DashboardItemType.TARGET) },
                        modifier = Modifier.weight(1f)
                    ) {
                        data.topTargets.forEach { item ->
                            val index = data.topTargets.indexOf(item)
                            DashboardItem(
                                index = index,
                                listLength = listLength,
                                itemType = Enums.DashboardItemType.TARGET,
                                label = item.target,
                                amount = item.amount,
                                percentage = item.amount.toDouble() / total,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
