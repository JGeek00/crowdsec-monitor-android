package com.jgeek00.crowdsecmonitor.ui.screens.lists

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
import com.jgeek00.crowdsecmonitor.constants.Enums.ListType
import com.jgeek00.crowdsecmonitor.ui.screens.lists.allowlists.details.AllowlistDetailPane
import com.jgeek00.crowdsecmonitor.ui.screens.lists.blocklists.details.BlocklistDetailPane
import com.jgeek00.crowdsecmonitor.viewmodel.AllowlistsListViewModel
import com.jgeek00.crowdsecmonitor.viewmodel.BlocklistsListViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun ListsScreen(
    blocklistsViewModel: BlocklistsListViewModel = hiltViewModel(),
    allowlistsViewModel: AllowlistsListViewModel = hiltViewModel()
) {
    var selectedListType by remember { mutableStateOf(ListType.BLOCKLIST) }

    val navigator = rememberListDetailPaneScaffoldNavigator<String>()
    val scope = rememberCoroutineScope()

    // Keep active id visible through the back-navigation exit animation (same pattern as AlertsListScreen)
    val currentListId = navigator.currentDestination?.contentKey
    var activeListId by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(currentListId) {
        if (currentListId != null) {
            activeListId = currentListId
        } else {
            delay(350)
            activeListId = null
        }
    }

    val isSinglePane = navigator.scaffoldDirective.maxHorizontalPartitions == 1

    BackHandler(navigator.canNavigateBack()) {
        scope.launch { navigator.navigateBack() }
    }

    LaunchedEffect(Unit) {
        blocklistsViewModel.initialFetch()
        allowlistsViewModel.initialFetch()
    }

    ListDetailPaneScaffold(
        directive = navigator.scaffoldDirective,
        value = navigator.scaffoldValue,
        listPane = {
            AnimatedPane(
                enterTransition = listPaneEnterTransition,
                exitTransition = listPaneExitTransition
            ) {
                ListsListPane(
                    selectedListType = selectedListType,
                    onListTypeChange = { selectedListType = it },
                    blocklistsViewModel = blocklistsViewModel,
                    allowlistsViewModel = allowlistsViewModel,
                    onNavigateToDetails = { listId ->
                        scope.launch {
                            navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, listId)
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
                val blocklistSegment = activeListId
                    ?.takeIf { it.startsWith("blocklist:") }
                    ?.removePrefix("blocklist:")
                val blocklistId = blocklistSegment?.substringBefore(':')
                val blocklistName = blocklistSegment?.substringAfter(':')?.takeIf { it.isNotEmpty() }

                val allowlistName = activeListId
                    ?.takeIf { it.startsWith("allowlist:") }
                    ?.removePrefix("allowlist:")

                if (blocklistId != null || (activeListId == null && selectedListType == ListType.BLOCKLIST)) {
                    BlocklistDetailPane(
                        blocklistId = blocklistId,
                        blocklistName = blocklistName,
                        showBackButton = isSinglePane && activeListId != null,
                        onNavigateBack = { scope.launch { navigator.navigateBack() } }
                    )
                } else {
                    AllowlistDetailPane(
                        allowlistName = allowlistName,
                        viewModel = allowlistsViewModel,
                        showBackButton = isSinglePane && activeListId != null,
                        onNavigateBack = { scope.launch { navigator.navigateBack() } }
                    )
                }
            }
        }
    )

    // Processing modal for blocklist actions
    if (blocklistsViewModel.processingModal) {
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

