package com.jgeek00.crowdsecmonitor.ui.screens.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jgeek00.crowdsecmonitor.R
import com.jgeek00.crowdsecmonitor.constants.Enums
import com.jgeek00.crowdsecmonitor.data.models.StatisticsResponse
import com.jgeek00.crowdsecmonitor.ui.components.SectionHeader
import com.jgeek00.crowdsecmonitor.ui.screens.ViewAllRow

@Composable
fun DashboardContentPhone(
    data: StatisticsResponse,
    onNavigateToFullList: (Enums.DashboardItemType) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        // Summary cards
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
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
        item {
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                DashboardBarChart(activityHistory = data.activityHistory)
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Top countries
        if (data.topCountries.isNotEmpty()) {
            val total = data.topCountries.sumOf { it.amount }.coerceAtLeast(1)
            item {
                SectionHeader(
                    text = stringResource(R.string.top_countries),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            items(data.topCountries) { item ->
                DashboardItem(
                    itemType = Enums.DashboardItemType.COUNTRY,
                    label = item.countryCode,
                    amount = item.amount,
                    percentage = item.amount.toDouble() / total,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            }
            item { ViewAllRow(onClick = { onNavigateToFullList(Enums.DashboardItemType.COUNTRY) }) }
        }

        // Top IP owners
        if (data.topIpOwners.isNotEmpty()) {
            val total = data.topIpOwners.sumOf { it.amount }.coerceAtLeast(1)
            item {
                SectionHeader(
                    text = stringResource(R.string.top_ip_owners),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            items(data.topIpOwners) { item ->
                DashboardItem(
                    itemType = Enums.DashboardItemType.IP_OWNER,
                    label = item.ipOwner,
                    amount = item.amount,
                    percentage = item.amount.toDouble() / total,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            }
            item { ViewAllRow(onClick = { onNavigateToFullList(Enums.DashboardItemType.IP_OWNER) }) }
        }

        // Top scenarios
        if (data.topScenarios.isNotEmpty()) {
            val total = data.topScenarios.sumOf { it.amount }.coerceAtLeast(1)
            item {
                SectionHeader(
                    text = stringResource(R.string.top_scenarios),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            items(data.topScenarios) { item ->
                DashboardItem(
                    itemType = Enums.DashboardItemType.SCENARIO,
                    label = item.scenario,
                    amount = item.amount,
                    percentage = item.amount.toDouble() / total,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            }
            item { ViewAllRow(onClick = { onNavigateToFullList(Enums.DashboardItemType.SCENARIO) }) }
        }

        // Top targets
        if (data.topTargets.isNotEmpty()) {
            val total = data.topTargets.sumOf { it.amount }.coerceAtLeast(1)
            item {
                SectionHeader(
                    text = stringResource(R.string.top_targets),
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
            items(data.topTargets) { item ->
                DashboardItem(
                    itemType = Enums.DashboardItemType.TARGET,
                    label = item.target,
                    amount = item.amount,
                    percentage = item.amount.toDouble() / total,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            }
            item { ViewAllRow(onClick = { onNavigateToFullList(Enums.DashboardItemType.TARGET) }) }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}