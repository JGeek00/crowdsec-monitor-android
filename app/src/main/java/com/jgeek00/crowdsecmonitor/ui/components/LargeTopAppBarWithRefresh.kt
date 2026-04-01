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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll

/**
 * Scaffold with a [LargeTopAppBar] that collapses on scroll combined with a [PullToRefreshBox].
 *
 * The pull-to-refresh gesture is intentionally blocked while the app bar is not fully expanded
 * (i.e. collapsedFraction > 0), so the indicator only activates once the app bar has
 * completely unfolded.
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

    // Guard placed INSIDE PullToRefreshBox so that in onPostScroll (dispatched inner→outer)
    // it runs BEFORE PullToRefreshBox's own connection.
    //
    // When the user overscrolls downward at the top while the AppBar is still collapsed:
    //  - the guard intercepts the leftover delta, expands the AppBar directly, and
    //    consumes the offset so PullToRefreshBox never sees it → no premature refresh.
    // When the AppBar is fully expanded (collapsedFraction == 0):
    //  - the guard returns Zero → PullToRefreshBox receives the delta → refresh activates.
    // Regular upward/downward scrolling through list content is unaffected because the
    // LazyColumn consumes those deltas before they reach onPostScroll.
    val pullToRefreshGuard = remember(scrollBehavior) {
        object : NestedScrollConnection {
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (available.y <= 0f || scrollBehavior.state.collapsedFraction <= 0f) {
                    return Offset.Zero
                }
                // Expand the AppBar directly and absorb the delta so that the
                // PullToRefreshBox connection (which runs right after this) sees nothing.
                scrollBehavior.state.heightOffset =
                    minOf(0f, scrollBehavior.state.heightOffset + available.y)
                return Offset(0f, available.y)
            }
        }
    }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
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
            // The guard Box must be a direct child of PullToRefreshBox so that its
            // nestedScroll node sits inside the PullToRefresh node in the layout tree,
            // guaranteeing that onPostScroll fires here before reaching PullToRefreshBox.
            Box(modifier = Modifier.fillMaxSize().nestedScroll(pullToRefreshGuard)) {
                content()
            }
        }
    }
}
