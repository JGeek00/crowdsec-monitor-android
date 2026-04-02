package com.jgeek00.crowdsecmonitor.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jgeek00.crowdsecmonitor.R
import com.jgeek00.crowdsecmonitor.constants.Defaults
import com.jgeek00.crowdsecmonitor.constants.Enums
import com.jgeek00.crowdsecmonitor.ui.components.ListItemContent
import com.jgeek00.crowdsecmonitor.ui.components.RoundedCornersListTile
import com.jgeek00.crowdsecmonitor.ui.components.SectionHeader
import com.jgeek00.crowdsecmonitor.viewmodel.AppConfigurationViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppConfigurationScreen(
    onBack: () -> Unit,
    viewModel: AppConfigurationViewModel = hiltViewModel()
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        topBar = {
            LargeFlexibleTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                ),
                scrollBehavior = scrollBehavior,
                title = { Text(stringResource(R.string.app_configuration)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Dashboard section
            item {
                SectionHeader(
                    stringResource(R.string.dashboard_section),
                    topPadding = Enums.SectionHeaderPaddingTop.SMALL
                )
            }
            item {
                RoundedCornersListTile(index = 0, totalItems = 1) {
                    ListItemContent(
                        headlineText = stringResource(R.string.amount_items_dashboard_label),
                        subHeadlineText = stringResource(
                            R.string.amount_items_dashboard_description,
                            viewModel.topItemsDashboard
                        ),
                        trailingContent = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                FilledTonalIconButton(
                                    onClick = { viewModel.updateTopItemsDashboard(viewModel.topItemsDashboard - 1) },
                                    enabled = viewModel.topItemsDashboard > Defaults.TOP_ITEMS_DASHBOARD_MIN,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Remove,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Text(
                                    text = viewModel.topItemsDashboard.toString(),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                FilledTonalIconButton(
                                    onClick = { viewModel.updateTopItemsDashboard(viewModel.topItemsDashboard + 1) },
                                    enabled = viewModel.topItemsDashboard < Defaults.TOP_ITEMS_DASHBOARD_MAX,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Add,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    )
                }
            }

            // Decisions section
            item {
                SectionHeader(stringResource(R.string.decisions))
            }
            item {
                RoundedCornersListTile(index = 0, totalItems = 2) {
                    ListItemContent(
                        headlineText = stringResource(R.string.show_only_active_decisions),
                        trailingContent = {
                            Switch(
                                checked = viewModel.showDefaultActiveDecisions,
                                onCheckedChange = { viewModel.updateShowDefaultActiveDecisions(it) }
                            )
                        }
                    )
                }
            }
            item {
                RoundedCornersListTile(index = 1, totalItems = 2) {
                    ListItemContent(
                        headlineText = stringResource(R.string.disable_timer_animation),
                        trailingContent = {
                            Switch(
                                checked = viewModel.disableDecisionTimerAnimation,
                                onCheckedChange = { viewModel.updateDisableDecisionTimerAnimation(it) }
                            )
                        }
                    )
                }
            }
        }
    }
}
