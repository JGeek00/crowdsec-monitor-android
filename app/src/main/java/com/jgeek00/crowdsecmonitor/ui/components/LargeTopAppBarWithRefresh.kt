package com.jgeek00.crowdsecmonitor.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll

/**
 * Scaffold with a [LargeTopAppBar] that collapses on scroll combined with a [PullToRefreshBox].
 *
 * The pull-to-refresh gesture only activates once the app bar is fully expanded.
 * This is achieved by placing [nestedScroll] with the scroll behavior connection on the first
 * element **inside** [PullToRefreshBox] rather than on the [Scaffold]. That makes the scroll
 * behavior connection inner relative to [PullToRefreshBox]'s own connection, so in
 * `onPostScroll` (dispatched inner → outer) the app bar expansion consumes the available
 * overscroll delta before [PullToRefreshBox] ever sees it.
 *
 * @param title Title composable forwarded to [LargeTopAppBar].
 * @param isRefreshing Whether the pull-to-refresh indicator should be shown.
 * @param onRefresh Called when the user triggers a pull-to-refresh.
 * @param modifier Modifier applied to the outer [Scaffold].
 * @param actions Action composables forwarded to [LargeTopAppBar].
 * @param containerColor Background color of both the scaffold and the app bar.
 * @param scrolledContainerColor App bar background color when scrolled / collapsed.
 * @param content Content rendered inside [PullToRefreshBox]. The inner padding from [Scaffold]
 *   is already applied to the box, so the content does not need to handle it.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LargeTopAppBarWithRefresh(
    title: @Composable () -> Unit,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    scrolledContainerColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
    content: @Composable BoxScope.() -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = modifier,
        containerColor = containerColor,
        topBar = {
            LargeTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = containerColor,
                    scrolledContainerColor = scrolledContainerColor
                ),
                scrollBehavior = scrollBehavior,
                title = title,
                actions = actions
            )
        }
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // nestedScroll goes here — inside PullToRefreshBox — so that in onPostScroll
            // the scroll behavior connection (inner) processes before PullToRefreshBox's
            // connection (outer). The app bar expands first; only when it is fully expanded
            // does PullToRefreshBox receive a non-zero delta and trigger the refresh.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
            ) {
                content()
            }
        }
    }
}
