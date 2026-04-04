package com.jgeek00.crowdsecmonitor.ui.screens.alerts.list

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import com.jgeek00.crowdsecmonitor.ui.navigation.detailPaneEnterTransition
import com.jgeek00.crowdsecmonitor.ui.navigation.detailPaneExitTransition
import com.jgeek00.crowdsecmonitor.ui.navigation.listPaneEnterTransition
import com.jgeek00.crowdsecmonitor.ui.navigation.listPaneExitTransition
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.jgeek00.crowdsecmonitor.viewmodel.AlertsListViewModel
import com.jgeek00.crowdsecmonitor.ui.screens.alerts.details.AlertDetailPane
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun AlertsListScreen(
    viewModel: AlertsListViewModel = hiltViewModel()
) {
    val navigator = rememberListDetailPaneScaffoldNavigator<Int>()
    val scope = rememberCoroutineScope()

    // Mirrors navigator.currentDestination?.contentKey but delays clearing to null
    // so the detail content stays visible during the back-navigation exit animation,
    // preventing the placeholder from flashing before the transition finishes.
    val currentAlertId = navigator.currentDestination?.contentKey
    var activeAlertId by remember { mutableStateOf<Int?>(null) }
    LaunchedEffect(currentAlertId) {
        if (currentAlertId != null) {
            activeAlertId = currentAlertId
        } else {
            delay(350)
            activeAlertId = null
        }
    }

    // Single-pane: back button should be visible for as long as the detail content
    // is visible (i.e. including the 350ms exit-animation window). Using
    // navigator.canNavigateBack() would hide the button immediately on back-press,
    // before the animation finishes.
    val isSinglePane = navigator.scaffoldDirective.maxHorizontalPartitions == 1

    BackHandler(navigator.canNavigateBack()) {
        scope.launch { navigator.navigateBack() }
    }

    LaunchedEffect(Unit) {
        viewModel.initialFetchAlerts()
    }

    ListDetailPaneScaffold(
        directive = navigator.scaffoldDirective,
        value = navigator.scaffoldValue,
        listPane = {
            AnimatedPane(
                enterTransition = listPaneEnterTransition,
                exitTransition = listPaneExitTransition
            ) {
                AlertsListPane(
                    viewModel = viewModel,
                    onNavigateToDetails = { alertId ->
                        scope.launch {
                            navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, alertId)
                        }
                    }
                )
            }
        },
        detailPane = {
            AnimatedPane(
                enterTransition = detailPaneEnterTransition,
                exitTransition = detailPaneExitTransition
            ) {
                AlertDetailPane(
                    alertId = activeAlertId,
                    showBackButton = isSinglePane && activeAlertId != null,
                    onNavigateBack = { scope.launch { navigator.navigateBack() } }
                )
            }
        }
    )

    if (viewModel.deletingAlertProcess) {
        Dialog(
            onDismissRequest = {},
            properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
        ) {
            Card {
                Box(
                    modifier = Modifier.padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}
