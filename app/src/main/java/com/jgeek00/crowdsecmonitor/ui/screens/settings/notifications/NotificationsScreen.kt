package com.jgeek00.crowdsecmonitor.ui.screens.settings.notifications

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
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jgeek00.crowdsecmonitor.R
import com.jgeek00.crowdsecmonitor.ui.screens.settings.notifications.ChannelWizard
import com.jgeek00.crowdsecmonitor.ui.screens.settings.notifications.NotificationWizard
import com.jgeek00.crowdsecmonitor.viewmodel.ChannelFormViewModel
import com.jgeek00.crowdsecmonitor.viewmodel.NotificationChannelsListViewModel
import com.jgeek00.crowdsecmonitor.viewmodel.NotificationFormViewModel
import com.jgeek00.crowdsecmonitor.viewmodel.NotificationsListViewModel
import kotlinx.coroutines.launch

private const val ANIM_DURATION = 350
private const val SLIDE_RATIO = 0.10f

private const val TAB_NOTIFICATIONS = 0
private const val TAB_CHANNELS = 1

/**
 * Entry point of the notifications settings: two tabs (notifications / channels)
 * hosting the lists, mirroring the iOS NotificationsSettingsView. Add-actions are
 * placeholders until the wizards land (tasks A8/A9); row taps navigate to the
 * details destinations (filled by task A10).
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun NotificationsScreen(
    onBack: () -> Unit,
    onOpenNotification: (id: Int) -> Unit,
    onOpenChannel: (id: Int) -> Unit,
    notificationsViewModel: NotificationsListViewModel = hiltViewModel(),
    channelsViewModel: NotificationChannelsListViewModel = hiltViewModel()
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var selectedTab by rememberSaveable { mutableIntStateOf(TAB_NOTIFICATIONS) }

    // A8: present the notification wizard (identity-fresh view model per
    // presentation) and refresh the list after a successful save.
    var showNotificationWizard by rememberSaveable { mutableStateOf(false) }
    var wizardPresentationId by remember { mutableIntStateOf(0) }
    val onAddNotification: () -> Unit = {
        wizardPresentationId++
        showNotificationWizard = true
    }
    // A9: present the channel wizard (identity-fresh view model per
    // presentation) and refresh the list after a successful save.
    var showChannelWizard by rememberSaveable { mutableStateOf(false) }
    var channelWizardPresentationId by remember { mutableIntStateOf(0) }
    val onAddChannel: () -> Unit = {
        channelWizardPresentationId++
        showChannelWizard = true
    }

    val errorToggleMsg = stringResource(R.string.error_toggle_notification)
    val errorDeleteNotificationMsg = stringResource(R.string.error_delete_notification)
    val notificationDeletedMsg = stringResource(R.string.notification_deleted)
    val errorDeleteChannelMsg = stringResource(R.string.error_delete_channel)
    val channelInUseMsg = stringResource(R.string.channel_in_use)
    val channelDeletedMsg = stringResource(R.string.channel_deleted)

    LaunchedEffect(Unit) {
        notificationsViewModel.initialFetch()
        channelsViewModel.initialFetch()
    }

    LaunchedEffect(notificationsViewModel.errorToggle) {
        if (notificationsViewModel.errorToggle) {
            scope.launch { snackbarHostState.showSnackbar(errorToggleMsg) }
            notificationsViewModel.clearErrorToggle()
        }
    }
    LaunchedEffect(notificationsViewModel.errorDelete) {
        if (notificationsViewModel.errorDelete) {
            scope.launch { snackbarHostState.showSnackbar(errorDeleteNotificationMsg) }
            notificationsViewModel.clearErrorDelete()
        }
    }
    LaunchedEffect(notificationsViewModel.didDelete) {
        if (notificationsViewModel.didDelete) {
            scope.launch { snackbarHostState.showSnackbar(notificationDeletedMsg) }
            notificationsViewModel.clearDidDelete()
        }
    }
    LaunchedEffect(channelsViewModel.errorDelete) {
        if (channelsViewModel.errorDelete) {
            scope.launch { snackbarHostState.showSnackbar(errorDeleteChannelMsg) }
            channelsViewModel.clearErrorDelete()
        }
    }
    LaunchedEffect(channelsViewModel.channelInUse) {
        if (channelsViewModel.channelInUse) {
            scope.launch { snackbarHostState.showSnackbar(channelInUseMsg) }
            channelsViewModel.clearChannelInUse()
        }
    }
    LaunchedEffect(channelsViewModel.didDelete) {
        if (channelsViewModel.didDelete) {
            scope.launch { snackbarHostState.showSnackbar(channelDeletedMsg) }
            channelsViewModel.clearDidDelete()
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column {
                LargeFlexibleTopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                    ),
                    scrollBehavior = scrollBehavior,
                    title = { Text(stringResource(R.string.notifications)) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = stringResource(R.string.back)
                            )
                        }
                    },
                    actions = {
                        val (addLabelRes, addAction) = if (selectedTab == TAB_NOTIFICATIONS) {
                            Pair(R.string.add_notification, onAddNotification)
                        } else {
                            Pair(R.string.add_channel, onAddChannel)
                        }
                        IconButton(onClick = addAction) {
                            Icon(Icons.Rounded.Add, contentDescription = stringResource(addLabelRes))
                        }
                    }
                )
                PrimaryTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = lerp(
                        MaterialTheme.colorScheme.surfaceContainer,
                        MaterialTheme.colorScheme.surfaceContainerHighest,
                        scrollBehavior.state.collapsedFraction
                    )
                ) {
                    Tab(
                        selected = selectedTab == TAB_NOTIFICATIONS,
                        onClick = { selectedTab = TAB_NOTIFICATIONS },
                        text = { Text(stringResource(R.string.notifications)) }
                    )
                    Tab(
                        selected = selectedTab == TAB_CHANNELS,
                        onClick = { selectedTab = TAB_CHANNELS },
                        text = { Text(stringResource(R.string.channels)) }
                    )
                }
            }
        }
    ) { paddingValues ->
        val isRefreshing = if (selectedTab == TAB_NOTIFICATIONS) {
            notificationsViewModel.isRefreshing
        } else {
            channelsViewModel.isRefreshing
        }
        val onRefresh: () -> Unit = {
            if (selectedTab == TAB_NOTIFICATIONS) notificationsViewModel.refresh() else channelsViewModel.refresh()
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
                    .padding(top = 12.dp)
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
            ) {
                AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = {
                        val direction = if (targetState == TAB_CHANNELS) 1 else -1
                        slideInHorizontally(
                            initialOffsetX = { (it * SLIDE_RATIO * direction).toInt() },
                            animationSpec = tween(ANIM_DURATION, easing = FastOutSlowInEasing)
                        ) + fadeIn(tween(ANIM_DURATION, easing = FastOutSlowInEasing)) togetherWith
                        slideOutHorizontally(
                            targetOffsetX = { -(it * SLIDE_RATIO * direction).toInt() },
                            animationSpec = tween(ANIM_DURATION, easing = FastOutSlowInEasing)
                        ) + fadeOut(tween(ANIM_DURATION, easing = FastOutSlowInEasing))
                    },
                    label = "NotificationsTabContent",
                    modifier = Modifier.fillMaxSize()
                ) { tab ->
                    when (tab) {
                        TAB_CHANNELS -> ChannelsListPane(
                            viewModel = channelsViewModel,
                            onOpenChannel = onOpenChannel
                        )
                        else -> NotificationsListPane(
                            viewModel = notificationsViewModel,
                            onOpenNotification = onOpenNotification
                        )
                    }
                }
            }
        }
    }

    if (showNotificationWizard) {
        val wizardViewModel: NotificationFormViewModel = hiltViewModel(
            key = "notification-wizard-$wizardPresentationId"
        )
        NotificationWizard(
            viewModel = wizardViewModel,
            onClose = { saved ->
                showNotificationWizard = false
                if (saved) {
                    notificationsViewModel.refresh()
                }
            }
        )
    }

    if (showChannelWizard) {
        val channelWizardViewModel: ChannelFormViewModel = hiltViewModel(
            key = "channel-wizard-$channelWizardPresentationId"
        )
        ChannelWizard(
            viewModel = channelWizardViewModel,
            onClose = { saved ->
                showChannelWizard = false
                if (saved) {
                    channelsViewModel.refresh()
                }
            }
        )
    }
}
