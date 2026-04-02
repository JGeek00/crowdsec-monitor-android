package com.jgeek00.crowdsecmonitor.ui.screens.lists

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.stringResource
import com.jgeek00.crowdsecmonitor.R
import com.jgeek00.crowdsecmonitor.constants.Enums.ListType
import com.jgeek00.crowdsecmonitor.ui.screens.lists.blocklists.BlocklistsListPane
import com.jgeek00.crowdsecmonitor.viewmodel.AllowlistsListViewModel
import com.jgeek00.crowdsecmonitor.viewmodel.BlocklistsListViewModel
import kotlinx.coroutines.launch

private const val ANIM_DURATION = 350
private const val SLIDE_RATIO = 0.10f

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ListsListPane(
    selectedListType: ListType,
    onListTypeChange: (ListType) -> Unit,
    blocklistsViewModel: BlocklistsListViewModel,
    allowlistsViewModel: AllowlistsListViewModel,
    onNavigateToDetails: (String) -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Snackbar notifications for blocklists
    val deletedMsg = stringResource(R.string.blocklist_deleted)
    val errorEnableMsg = stringResource(R.string.error_enable_blocklist)
    val errorDisableMsg = stringResource(R.string.error_disable_blocklist)
    val errorDeleteMsg = stringResource(R.string.error_delete_blocklist)

    LaunchedEffect(blocklistsViewModel.blocklistDeletedSuccessfully) {
        if (blocklistsViewModel.blocklistDeletedSuccessfully) {
            scope.launch { snackbarHostState.showSnackbar(deletedMsg) }
            blocklistsViewModel.clearBlocklistDeletedSuccessfully()
        }
    }
    LaunchedEffect(blocklistsViewModel.errorEnableBlocklist) {
        if (blocklistsViewModel.errorEnableBlocklist) {
            scope.launch { snackbarHostState.showSnackbar(errorEnableMsg) }
            blocklistsViewModel.clearErrorEnableBlocklist()
        }
    }
    LaunchedEffect(blocklistsViewModel.errorDisableBlocklist) {
        if (blocklistsViewModel.errorDisableBlocklist) {
            scope.launch { snackbarHostState.showSnackbar(errorDisableMsg) }
            blocklistsViewModel.clearErrorDisableBlocklist()
        }
    }
    LaunchedEffect(blocklistsViewModel.errorDeleteBlocklist) {
        if (blocklistsViewModel.errorDeleteBlocklist) {
            scope.launch { snackbarHostState.showSnackbar(errorDeleteMsg) }
            blocklistsViewModel.clearErrorDeleteBlocklist()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column {
                LargeFlexibleTopAppBar(
                    title = { Text(stringResource(R.string.lists)) },
                    scrollBehavior = scrollBehavior,
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                    ),
                    actions = {
                        if (selectedListType == ListType.BLOCKLIST) {
                            TooltipBox(
                                positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Below),
                                tooltip = { PlainTooltip { Text(stringResource(R.string.add_blocklist)) } },
                                state = rememberTooltipState()
                            ) {
                                IconButton(onClick = { /* TODO: open add blocklist form */ }) {
                                    Icon(Icons.Rounded.Add, contentDescription = stringResource(R.string.add_blocklist))
                                }
                            }
                        }
                    }
                )
                PrimaryTabRow(
                    selectedTabIndex = if (selectedListType == ListType.BLOCKLIST) 0 else 1,
                    containerColor = lerp(
                        MaterialTheme.colorScheme.surfaceContainer,
                        MaterialTheme.colorScheme.surfaceContainerHighest,
                        scrollBehavior.state.collapsedFraction
                    )
                ) {
                    Tab(
                        selected = selectedListType == ListType.BLOCKLIST,
                        onClick = { onListTypeChange(ListType.BLOCKLIST) },
                        text = { Text(stringResource(R.string.blocklists)) }
                    )
                    Tab(
                        selected = selectedListType == ListType.ALLOWLIST,
                        onClick = { onListTypeChange(ListType.ALLOWLIST) },
                        text = { Text(stringResource(R.string.allowlists)) }
                    )
                }
            }
        }
    ) { paddingValues ->
        val isRefreshing = when (selectedListType) {
            ListType.BLOCKLIST -> blocklistsViewModel.isRefreshing
            ListType.ALLOWLIST -> allowlistsViewModel.isRefreshing
        }
        val onRefresh: () -> Unit = when (selectedListType) {
            ListType.BLOCKLIST -> { { blocklistsViewModel.refresh() } }
            ListType.ALLOWLIST -> { { allowlistsViewModel.refresh() } }
        }

        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
            ) {
                AnimatedContent(
                    targetState = selectedListType,
                    transitionSpec = {
                        val direction = if (targetState == ListType.ALLOWLIST) 1 else -1
                        slideInHorizontally(
                            initialOffsetX = { (it * SLIDE_RATIO * direction).toInt() },
                            animationSpec = tween(ANIM_DURATION, easing = FastOutSlowInEasing)
                        ) + fadeIn(tween(ANIM_DURATION, easing = FastOutSlowInEasing)) togetherWith
                        slideOutHorizontally(
                            targetOffsetX = { -(it * SLIDE_RATIO * direction).toInt() },
                            animationSpec = tween(ANIM_DURATION, easing = FastOutSlowInEasing)
                        ) + fadeOut(tween(ANIM_DURATION, easing = FastOutSlowInEasing))
                    },
                    label = "ListTypeContent",
                    modifier = Modifier.fillMaxSize()
                ) { listType ->
                    when (listType) {
                        ListType.BLOCKLIST -> {
                            BlocklistsListPane(
                                viewModel = blocklistsViewModel,
                                onNavigateToDetails = { id ->
                                    onNavigateToDetails("blocklist:$id")
                                }
                            )
                        }
                        ListType.ALLOWLIST -> {
                            // TODO: AllowlistsListPane
                            Box(modifier = Modifier.fillMaxSize())
                        }
                    }
                }
            }
        }
    }
}
