package com.jgeek00.crowdsecmonitor.ui.screens.alerts.list.filters

import android.graphics.Color
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.jgeek00.crowdsecmonitor.R
import com.jgeek00.crowdsecmonitor.data.models.AlertsRequestFilters
import com.jgeek00.crowdsecmonitor.data.models.LoadingResult
import com.jgeek00.crowdsecmonitor.ui.components.CountryFlag
import com.jgeek00.crowdsecmonitor.viewmodel.AlertsListViewModel

private sealed interface FilterScreen {
    data object Main : FilterScreen
    data object Scenarios : FilterScreen
    data object IpOwners : FilterScreen
    data object Countries : FilterScreen
    data object Targets : FilterScreen
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertsFiltersSheet(
    viewModel: AlertsListViewModel,
    onDismiss: () -> Unit
) {
    var currentScreen by remember { mutableStateOf<FilterScreen>(FilterScreen.Main) }

    fun navigateTo(screen: FilterScreen) { currentScreen = screen }
    fun navigateToMain() { currentScreen = FilterScreen.Main }

    val availableData = (viewModel.state as? LoadingResult.Success)?.value?.filtering

    val screenTitle = when (currentScreen) {
        FilterScreen.Main -> stringResource(R.string.filters)
        FilterScreen.Scenarios -> stringResource(R.string.scenarios)
        FilterScreen.IpOwners -> stringResource(R.string.ip_owners)
        FilterScreen.Countries -> stringResource(R.string.countries)
        FilterScreen.Targets -> stringResource(R.string.targets)
    }

    val darkTheme = isSystemInDarkTheme()

    Dialog(
        onDismissRequest = {
            navigateToMain()
            onDismiss()
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            decorFitsSystemWindows = false
        ),
    ) {
        BackHandler {
            if (currentScreen != FilterScreen.Main) {
                navigateToMain()
            } else {
                onDismiss()
            }
        }

        val dialogWindow = (LocalView.current.parent as? DialogWindowProvider)?.window
        SideEffect {
            dialogWindow?.let { window ->
                WindowCompat.setDecorFitsSystemWindows(window, false)
                val controller = WindowInsetsControllerCompat(window, window.decorView)
                controller.isAppearanceLightStatusBars = !darkTheme
                controller.isAppearanceLightNavigationBars = !darkTheme
                @Suppress("DEPRECATION")
                window.statusBarColor = Color.TRANSPARENT
                @Suppress("DEPRECATION")
                window.navigationBarColor = Color.TRANSPARENT
            }
        }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = { Text(screenTitle) },
                    navigationIcon = {
                        if (currentScreen == FilterScreen.Main) {
                            TooltipBox(
                                positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Below),
                                tooltip = { PlainTooltip { Text(stringResource(R.string.close)) } },
                                state = rememberTooltipState()
                            ) {
                                IconButton(onClick = { navigateToMain(); onDismiss() }) {
                                    Icon(
                                        imageVector = Icons.Rounded.Close,
                                        contentDescription = stringResource(R.string.close)
                                    )
                                }
                            }
                        } else {
                            TooltipBox(
                                positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Below),
                                tooltip = { PlainTooltip { Text(stringResource(R.string.back)) } },
                                state = rememberTooltipState()
                            ) {
                                IconButton(onClick = { navigateToMain() }) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                        contentDescription = stringResource(R.string.back)
                                    )
                                }
                            }
                        }
                    },
                    actions = {
                        if (currentScreen == FilterScreen.Main) {
                            TooltipBox(
                                positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Below),
                                tooltip = { PlainTooltip { Text(stringResource(R.string.reset)) } },
                                state = rememberTooltipState()
                            ) {
                                IconButton(
                                    onClick = {
                                        navigateToMain()
                                        onDismiss()
                                        viewModel.resetFilters()
                                    }
                                ) {
                                    Icon(
                                        Icons.Rounded.DeleteSweep,
                                        contentDescription = stringResource(R.string.reset),
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.size(2.dp))
                            TooltipBox(
                                positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Below),
                                tooltip = { PlainTooltip { Text(stringResource(R.string.apply)) } },
                                state = rememberTooltipState()
                            ) {
                                IconButton(
                                    onClick = {
                                        navigateToMain()
                                        onDismiss()
                                        viewModel.applyFilters()
                                    }
                                ) {
                                    Icon(
                                        Icons.Rounded.Check,
                                        contentDescription = stringResource(R.string.apply),
                                    )
                                }
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors().copy(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    )
                )
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ) { innerPadding ->
            AnimatedContent(
                targetState = currentScreen,
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(top = 8.dp),
                transitionSpec = {
                    if (targetState == FilterScreen.Main) {
                        slideInHorizontally { -it } togetherWith slideOutHorizontally { it }
                    } else {
                        slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
                    }
                },
                label = "FilterScreen"
            ) { screen ->
                when (screen) {
                    FilterScreen.Main -> MainFilterContent(
                        filters = viewModel.filters,
                        enabled = availableData != null,
                        onNavigateTo = { navigateTo(it) },
                    )

                    FilterScreen.Scenarios -> MultiSelectFilterContent(
                        options = availableData?.scenarios ?: emptyList(),
                        selected = viewModel.filters.scenarios,
                        onChange = { values ->
                            viewModel.updateFilters(viewModel.filters.copy(scenarios = values))
                        }
                    )

                    FilterScreen.IpOwners -> MultiSelectFilterContent(
                        options = availableData?.ipOwners ?: emptyList(),
                        selected = viewModel.filters.ipOwners,
                        onChange = { values ->
                            viewModel.updateFilters(viewModel.filters.copy(ipOwners = values))
                        }
                    )

                    FilterScreen.Countries -> MultiSelectFilterContent(
                        options = availableData?.countries ?: emptyList(),
                        selected = viewModel.filters.countries,
                        onChange = { values ->
                            viewModel.updateFilters(viewModel.filters.copy(countries = values))
                        },
                        customLabel = { code -> CountryFlag(countryCode = code) }
                    )

                    FilterScreen.Targets -> MultiSelectFilterContent(
                        options = availableData?.targets ?: emptyList(),
                        selected = viewModel.filters.targets,
                        onChange = { values ->
                            viewModel.updateFilters(viewModel.filters.copy(targets = values))
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun MainFilterContent(
    filters: AlertsRequestFilters,
    enabled: Boolean,
    onNavigateTo: (FilterScreen) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 24.dp)
    ) {

        FilterRow(
            index = 0,
            total = 4,
            label = stringResource(R.string.scenarios),
            selectedCount = filters.scenarios.size,
            enabled = enabled,
            onClick = { onNavigateTo(FilterScreen.Scenarios) }
        )
        FilterRow(
            index = 1,
            total = 4,
            label = stringResource(R.string.ip_owners),
            selectedCount = filters.ipOwners.size,
            enabled = enabled,
            onClick = { onNavigateTo(FilterScreen.IpOwners) }
        )
        FilterRow(
            index = 2,
            total = 4,
            label = stringResource(R.string.countries),
            selectedCount = filters.countries.size,
            enabled = enabled,
            onClick = { onNavigateTo(FilterScreen.Countries) }
        )
        FilterRow(
            index = 3,
            total = 4,
            label = stringResource(R.string.targets),
            selectedCount = filters.targets.size,
            enabled = enabled,
            onClick = { onNavigateTo(FilterScreen.Targets) }
        )
    }
}