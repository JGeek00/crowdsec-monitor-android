package com.jgeek00.crowdsecmonitor.ui.screens.decisions.list

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
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
import com.jgeek00.crowdsecmonitor.viewmodel.DecisionsListViewModel
import com.jgeek00.crowdsecmonitor.ui.screens.decisions.details.DecisionDetailPane
import com.jgeek00.crowdsecmonitor.ui.navigation.detailPaneEnterTransition
import com.jgeek00.crowdsecmonitor.ui.navigation.detailPaneExitTransition
import com.jgeek00.crowdsecmonitor.ui.navigation.listPaneEnterTransition
import com.jgeek00.crowdsecmonitor.ui.navigation.listPaneExitTransition
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun DecisionsListScreen(
    viewModel: DecisionsListViewModel = hiltViewModel()
) {
    val navigator = rememberListDetailPaneScaffoldNavigator<Int>()
    val scope = rememberCoroutineScope()

    val currentDecisionId = navigator.currentDestination?.contentKey
    var activeDecisionId by remember { mutableStateOf<Int?>(null) }
    LaunchedEffect(currentDecisionId) {
        if (currentDecisionId != null) {
            activeDecisionId = currentDecisionId
        } else {
            delay(350)
            activeDecisionId = null
        }
    }

    val isSinglePane = navigator.scaffoldDirective.maxHorizontalPartitions == 1

    BackHandler(navigator.canNavigateBack()) {
        scope.launch { navigator.navigateBack() }
    }

    LaunchedEffect(Unit) {
        viewModel.initialFetchDecisions()
    }

    ListDetailPaneScaffold(
        directive = navigator.scaffoldDirective,
        value = navigator.scaffoldValue,
        listPane = {
            AnimatedPane(
                enterTransition = listPaneEnterTransition,
                exitTransition = listPaneExitTransition
            ) {
                DecisionsListPane(
                    viewModel = viewModel,
                    onNavigateToDetails = { decisionId ->
                        scope.launch {
                            navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, decisionId)
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
                DecisionDetailPane(
                    decisionId = activeDecisionId,
                    showBackButton = isSinglePane && activeDecisionId != null,
                    onNavigateBack = { scope.launch { navigator.navigateBack() } }
                )
            }
        }
    )

    if (viewModel.expiringDecisionProcess) {
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
