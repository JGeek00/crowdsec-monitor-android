package com.jgeek00.crowdsecmonitor.ui.screens.decisions.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.HistoryToggleOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jgeek00.crowdsecmonitor.R
import com.jgeek00.crowdsecmonitor.data.models.DecisionsByIPDetailResponseDecision
import com.jgeek00.crowdsecmonitor.data.models.LoadingResult
import com.jgeek00.crowdsecmonitor.ui.components.CountryFlag
import com.jgeek00.crowdsecmonitor.ui.components.OptionsMenuBottomSheet
import com.jgeek00.crowdsecmonitor.ui.components.OptionsMenuBottomSheetItem
import com.jgeek00.crowdsecmonitor.ui.components.OptionsMenuBottomSheetItemRole
import com.jgeek00.crowdsecmonitor.extensions.toInstant
import com.jgeek00.crowdsecmonitor.ui.components.RoundedCornersListTile
import com.jgeek00.crowdsecmonitor.ui.screens.decisions.components.DecisionItemNoIP
import com.jgeek00.crowdsecmonitor.ui.components.ListItemContent
import com.jgeek00.crowdsecmonitor.ui.components.SectionHeader
import com.jgeek00.crowdsecmonitor.utils.reverseGeocode
import com.jgeek00.crowdsecmonitor.viewmodel.DecisionIPGroupDetailViewModel
import java.time.Instant

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DecisionIPGroupDetailScreen(
    ip: String,
    onlyActive: Boolean? = null,
    showBackButton: Boolean = false,
    onNavigateBack: () -> Unit = {},
    onNavigateToAlert: (Int) -> Unit = {},
    viewModel: DecisionIPGroupDetailViewModel = hiltViewModel()
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val context = LocalContext.current

    LaunchedEffect(ip) {
        viewModel.initialize(ip, onlyActive)
    }

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
                title = { Text(ip) },
                navigationIcon = {
                    if (showBackButton) {
                        androidx.compose.material3.IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = stringResource(R.string.back)
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        when (val state = viewModel.state) {
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
                val data = state.value
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    // Origin section
                    item {
                        SectionHeader(stringResource(R.string.origin))
                    }

                    // IP address
                    item {
                        RoundedCornersListTile(index = 0, totalItems = 5) {
                            ListItemContent(
                                headlineText = stringResource(R.string.ip_address),
                                subHeadlineText = data.ip
                            )
                        }
                    }

                    // Range
                    item {
                        RoundedCornersListTile(index = 1, totalItems = 5) {
                            ListItemContent(
                                headlineText = stringResource(R.string.range),
                                subHeadlineText = data.range ?: stringResource(R.string.not_available)
                            )
                        }
                    }

                    // Country
                    if (data.country != null) {
                        item {
                            RoundedCornersListTile(index = 2, totalItems = 5) {
                                ListItemContent(
                                    headlineText = stringResource(R.string.country),
                                    subHeadlineComponent = {
                                        CountryFlag(countryCode = data.country, fontSize = 14)
                                    }
                                )
                            }
                        }
                    } else {
                        item {
                            RoundedCornersListTile(index = 2, totalItems = 5) {
                                ListItemContent(
                                    headlineText = stringResource(R.string.country),
                                    subHeadlineText = stringResource(R.string.not_available)
                                )
                            }
                        }
                    }

                    // Owner
                    if (!data.owner.isNullOrBlank()) {
                        item {
                            RoundedCornersListTile(index = 3, totalItems = 5) {
                                ListItemContent(
                                    headlineText = stringResource(R.string.owner),
                                    subHeadlineText = data.owner
                                )
                            }
                        }
                    } else {
                        item {
                            RoundedCornersListTile(index = 3, totalItems = 5) {
                                ListItemContent(
                                    headlineText = stringResource(R.string.owner),
                                    subHeadlineText = stringResource(R.string.not_available)
                                )
                            }
                        }
                    }

                    // Location
                    item {
                        var geocodedLocation by remember(data.latitude, data.longitude) {
                            mutableStateOf<LoadingResult<String>>(LoadingResult.Loading)
                        }

                        LaunchedEffect(data.latitude, data.longitude) {
                            if (data.latitude != null && data.longitude != null) {
                                val result = reverseGeocode(context, data.latitude, data.longitude)
                                geocodedLocation = if (result != null) {
                                    LoadingResult.Success(result)
                                } else {
                                    LoadingResult.Failure(Exception())
                                }
                            } else {
                                geocodedLocation = LoadingResult.Failure(Exception())
                            }
                        }

                        val locationText = when (val loc = geocodedLocation) {
                            is LoadingResult.Success -> loc.value
                            else -> null
                        }
                        if (locationText != null) {
                            RoundedCornersListTile(index = 4, totalItems = 5) {
                                ListItemContent(
                                    headlineText = stringResource(R.string.location),
                                    subHeadlineText = locationText
                                )
                            }
                        } else {
                            RoundedCornersListTile(index = 4, totalItems = 5) {
                                ListItemContent(
                                    headlineText = stringResource(R.string.location),
                                    subHeadlineText = stringResource(R.string.not_available)
                                )
                            }
                        }
                    }

                    // Decisions section
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SectionHeader(stringResource(R.string.decisions))
                            if (data.activeDecisions > 0) {
                                Text(
                                    text = pluralStringResource(
                                        R.plurals.active_decisions_count,
                                        data.activeDecisions,
                                        data.activeDecisions
                                    ),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    // Decision items
                    items(data.decisions, key = { it.id }) { decision ->
                        val index = data.decisions.indexOf(decision)
                        DecisionItemNoIPWithExpire(
                            index = index,
                            totalListAmount = data.decisions.size,
                            decision = decision,
                            viewModel = viewModel,
                            disableTimerAnimation = viewModel.disableDecisionTimerAnimation,
                            onClick = { onNavigateToAlert(decision.alertId) }
                        )
                    }
                }
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
                            Icons.Rounded.Error,
                            contentDescription = null,
                            modifier = Modifier.size(56.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = stringResource(R.string.error_fetching_data),
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                        TextButton(onClick = { viewModel.refresh() }) {
                            Text(stringResource(R.string.refresh))
                        }
                    }
                }
            }
        }
    }

    if (viewModel.expiringDecisionProcess) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text(stringResource(R.string.expire_decision)) },
            text = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            },
            confirmButton = {},
            dismissButton = {}
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun DecisionItemNoIPWithExpire(
    index: Int,
    totalListAmount: Int,
    decision: DecisionsByIPDetailResponseDecision,
    viewModel: DecisionIPGroupDetailViewModel,
    disableTimerAnimation: Boolean = false,
    onClick: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var showExpireConfirm by remember { mutableStateOf(false) }
    var showExpireError by remember { mutableStateOf(false) }

    val isExpired = remember(decision.expiration) {
        val instant = decision.expiration.toInstant()
        instant != null && !instant.isAfter(Instant.now())
    }

    DecisionItemNoIP(
        index = index,
        totalListAmount = totalListAmount,
        decision = decision,
        disableTimerAnimation = disableTimerAnimation,
        onClick = onClick,
        onLongClick = {
            if (!isExpired) {
                menuExpanded = true
            }
        }
    )

    if (menuExpanded) {
        OptionsMenuBottomSheet(
            options = listOf(
                OptionsMenuBottomSheetItem(
                    title = stringResource(R.string.expire_decision),
                    icon = Icons.Rounded.HistoryToggleOff,
                    onClick = { showExpireConfirm = true },
                    role = OptionsMenuBottomSheetItemRole.DESTRUCTIVE
                )
            ),
            showMenu = menuExpanded,
            onDismiss = { menuExpanded = false }
        )
    }

    if (showExpireConfirm) {
        AlertDialog(
            onDismissRequest = { showExpireConfirm = false },
            title = { Text(stringResource(R.string.expire_decision)) },
            text = { Text(stringResource(R.string.expire_decision_confirm)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showExpireConfirm = false
                        viewModel.expireDecision(decision.id) { success ->
                            if (!success) showExpireError = true
                        }
                    }
                ) {
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


