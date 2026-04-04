package com.jgeek00.crowdsecmonitor.ui.screens.decisions.details

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.HistoryToggleOff
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.jgeek00.crowdsecmonitor.R
import com.jgeek00.crowdsecmonitor.data.models.LoadingResult
import com.jgeek00.crowdsecmonitor.extensions.toInstant
import com.jgeek00.crowdsecmonitor.viewmodel.DecisionDetailsViewModel
import java.time.Instant

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DecisionDetailsScreen(
    decisionId: Int,
    showBackButton: Boolean = false,
    onNavigateBack: () -> Unit = {},
    onNavigateToAlert: ((Int) -> Unit)? = null
) {
    val viewModel: DecisionDetailsViewModel = hiltViewModel(key = decisionId.toString())
    val context = LocalContext.current

    var showExpireConfirm by remember { mutableStateOf(false) }
    var showExpireError by remember { mutableStateOf(false) }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    val successData = (viewModel.state as? LoadingResult.Success)?.value
    val isExpired = remember(successData?.expiration) {
        val instant = successData?.expiration?.toInstant()
        instant == null || instant.isBefore(Instant.now())
    }

    LaunchedEffect(decisionId) {
        viewModel.initialize(decisionId)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        topBar = {
            LargeFlexibleTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                ),
                scrollBehavior = scrollBehavior,
                title = { Text(stringResource(R.string.decision_title, decisionId)) },
                navigationIcon = {
                    if (showBackButton) {
                        TooltipBox(
                            positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Below),
                            tooltip = { PlainTooltip { Text(stringResource(R.string.back)) } },
                            state = rememberTooltipState()
                        ) {
                            IconButton(onClick = onNavigateBack) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                    contentDescription = stringResource(R.string.back)
                                )
                            }
                        }
                    }
                },
                actions = {
                    if (successData != null && !isExpired) {
                        TooltipBox(
                            positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Below),
                            tooltip = { PlainTooltip { Text(stringResource(R.string.expire_decision)) } },
                            state = rememberTooltipState()
                        ) {
                            IconButton(onClick = { showExpireConfirm = true }) {
                                Icon(
                                    imageVector = Icons.Rounded.HistoryToggleOff,
                                    contentDescription = stringResource(R.string.expire_decision)
                                )
                            }
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        AnimatedContent(
            targetState = viewModel.state,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            contentKey = { it::class },
            label = "DecisionDetailsState"
        ) { state ->
            when (state) {
                is LoadingResult.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is LoadingResult.Success -> {
                    DecisionDetailsContent(
                        data = state.value,
                        innerPadding = innerPadding,
                        isRefreshing = viewModel.isRefreshing,
                        onRefresh = { viewModel.refresh(decisionId) },
                        onNavigateToAlert = onNavigateToAlert,
                        nestedScrollConnection = scrollBehavior.nestedScrollConnection,
                        context = context,
                        disableTimerAnimation = viewModel.disableDecisionTimerAnimation
                    )
                }

                is LoadingResult.Failure -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Error,
                                contentDescription = null,
                                modifier = Modifier.size(56.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = stringResource(R.string.error_fetching_data),
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center
                            )
                            IconButton(onClick = { viewModel.refresh(decisionId) }) {
                                Icon(Icons.Rounded.Refresh, contentDescription = null)
                            }
                        }
                    }
                }
            }
        }
    }

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

    if (showExpireConfirm) {
        AlertDialog(
            onDismissRequest = { showExpireConfirm = false },
            title = { Text(stringResource(R.string.expire_decision)) },
            text = { Text(stringResource(R.string.expire_decision_confirm)) },
            confirmButton = {
                TextButton(onClick = {
                    showExpireConfirm = false
                    viewModel.expireDecision(decisionId) { success ->
                        if (!success) showExpireError = true
                    }
                }) {
                    Text(
                        text = stringResource(R.string.expire_decision),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showExpireConfirm = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (showExpireError) {
        AlertDialog(
            onDismissRequest = { showExpireError = false },
            title = { Text(stringResource(R.string.expire_decision_error_title)) },
            text = { Text(stringResource(R.string.expire_decision_error_msg)) },
            confirmButton = {
                TextButton(onClick = { showExpireError = false }) {
                    Text(stringResource(R.string.ok))
                }
            }
        )
    }
}
