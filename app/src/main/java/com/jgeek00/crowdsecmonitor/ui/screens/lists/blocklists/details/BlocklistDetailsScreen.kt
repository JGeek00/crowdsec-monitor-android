package com.jgeek00.crowdsecmonitor.ui.screens.lists.blocklists.details

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.activity.compose.BackHandler
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.jgeek00.crowdsecmonitor.R
import com.jgeek00.crowdsecmonitor.constants.Defaults
import com.jgeek00.crowdsecmonitor.data.models.BlocklistType
import com.jgeek00.crowdsecmonitor.data.models.LoadingResult
import com.jgeek00.crowdsecmonitor.ui.components.ListItemContent
import com.jgeek00.crowdsecmonitor.ui.components.RoundedCornersListTile
import com.jgeek00.crowdsecmonitor.ui.screens.lists.blocklists.getBlocklistActiveProcess
import com.jgeek00.crowdsecmonitor.viewmodel.BlocklistDetailsViewModel
import com.jgeek00.crowdsecmonitor.viewmodel.ServiceStatusViewModel
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BlocklistDetailsScreen(
    blocklistId: String,
    blocklistName: String? = null,
    showBackButton: Boolean = false,
    onNavigateBack: () -> Unit = {}
) {
    val viewModel: BlocklistDetailsViewModel = hiltViewModel(key = blocklistId.toString())
    val serviceStatusViewModel: ServiceStatusViewModel = hiltViewModel()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val snackbarHostState = remember { SnackbarHostState() }

    var showToolbarMenu by remember { mutableStateOf(false) }
    var showRefreshConfirm by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(blocklistId) {
        viewModel.initialize(blocklistId)
    }

    BackHandler(enabled = viewModel.searchPresented) {
        viewModel.updateSearchPresented(false)
    }

    val successData = (viewModel.state as? LoadingResult.Success)?.value
    val title = successData?.data?.name ?: blocklistName ?: stringResource(R.string.blocklist_details)
    val data = successData?.data

    val serviceStatus = serviceStatusViewModel.status.collectAsState().value
    val blocklistProcess = data?.let { getBlocklistActiveProcess(serviceStatus.data, data.id) }

    // Snackbar notifications
    val refreshErrorMsg = stringResource(R.string.error_refresh_blocklist)
    val toggleErrorMsg = stringResource(R.string.error_toggle_blocklist)
    val deleteErrorMsg = stringResource(R.string.error_delete_blocklist)

    LaunchedEffect(viewModel.errorRefreshBlocklist) {
        if (viewModel.errorRefreshBlocklist) {
            snackbarHostState.showSnackbar(refreshErrorMsg)
            viewModel.clearErrorRefreshBlocklist()
        }
    }
    LaunchedEffect(viewModel.errorToggleBlocklist) {
        if (viewModel.errorToggleBlocklist) {
            snackbarHostState.showSnackbar(toggleErrorMsg)
            viewModel.clearErrorToggleBlocklist()
        }
    }
    LaunchedEffect(viewModel.errorDeleteBlocklist) {
        if (viewModel.errorDeleteBlocklist) {
            snackbarHostState.showSnackbar(deleteErrorMsg)
            viewModel.clearErrorDeleteBlocklist()
        }
    }
    LaunchedEffect(viewModel.blocklistDeletedSuccessfully) {
        if (viewModel.blocklistDeletedSuccessfully) {
            viewModel.clearBlocklistDeletedSuccessfully()
            onNavigateBack()
        }
    }

    // Processing modal
    if (viewModel.processingModal) {
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

    // Refresh confirmation dialog
    if (showRefreshConfirm) {
        AlertDialog(
            onDismissRequest = { showRefreshConfirm = false },
            title = { Text(stringResource(R.string.refresh_blocklist)) },
            text = { Text(stringResource(R.string.refresh_lists_confirm)) },
            confirmButton = {
                TextButton(onClick = {
                    showRefreshConfirm = false
                    viewModel.refreshBlocklist(blocklistId)
                }) {
                    Text(stringResource(R.string.continue_label))
                }
            },
            dismissButton = {
                TextButton(onClick = { showRefreshConfirm = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    // Delete confirmation dialog
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(stringResource(R.string.delete_blocklist)) },
            text = { Text(stringResource(R.string.delete_blocklist_confirm)) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    viewModel.deleteBlocklist(blocklistId)
                }) {
                    Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            AnimatedContent(
                targetState = viewModel.searchPresented,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "TopBarState"
            ) { searchPresented ->
                if (searchPresented) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceContainer)
                    ) {
                        TextField(
                            value = viewModel.searchText,
                            onValueChange = { viewModel.updateSearchText(it) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .statusBarsPadding()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            placeholder = { Text(stringResource(R.string.search_ips)) },
                            singleLine = true,
                            shape = CircleShape,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent
                            ),
                            leadingIcon = {
                                IconButton(onClick = { viewModel.updateSearchPresented(false) }) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                        contentDescription = stringResource(R.string.back)
                                    )
                                }
                            },
                            trailingIcon = {
                                if (viewModel.searchText.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.updateSearchText("") }) {
                                        Icon(Icons.Rounded.Close, contentDescription = null)
                                    }
                                }
                            }
                        )
                    }
                } else {
                    Column {
                        LargeFlexibleTopAppBar(
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                                scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                            ),
                            scrollBehavior = scrollBehavior,
                            title = {
                                Text(text = title, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            },
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
                                if (data != null) {
                                    // Search button
                                    TooltipBox(
                                        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Below),
                                        tooltip = { PlainTooltip { Text(stringResource(R.string.search_ips)) } },
                                        state = rememberTooltipState()
                                    ) {
                                        IconButton(onClick = { viewModel.updateSearchPresented(true) }) {
                                            Icon(Icons.Rounded.Search, contentDescription = stringResource(R.string.search_ips))
                                        }
                                    }

                                    // Toolbar menu (only for API blocklists)
                                    if (data.type == BlocklistType.API) {
                                        Box {
                                            TooltipBox(
                                                positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Below),
                                                tooltip = { PlainTooltip { Text(stringResource(R.string.more_options)) } },
                                                state = rememberTooltipState()
                                            ) {
                                                IconButton(onClick = { showToolbarMenu = true }) {
                                                    Icon(Icons.Rounded.MoreVert, contentDescription = stringResource(R.string.more_options))
                                                }
                                            }
                                            DropdownMenu(
                                                expanded = showToolbarMenu,
                                                onDismissRequest = { showToolbarMenu = false }
                                            ) {
                                                // Refresh (only if enabled)
                                                if (data.enabled != false) {
                                                    DropdownMenuItem(
                                                        text = { Text(stringResource(R.string.refresh_blocklist)) },
                                                        onClick = {
                                                            showToolbarMenu = false
                                                            showRefreshConfirm = true
                                                        },
                                                        enabled = blocklistProcess == null,
                                                        leadingIcon = {
                                                            Icon(Icons.Rounded.Refresh, contentDescription = null)
                                                        }
                                                    )
                                                }
                                                // Enable/Disable toggle
                                                DropdownMenuItem(
                                                    text = {
                                                        Text(
                                                            if (data.enabled == true) stringResource(R.string.disable_blocklist)
                                                            else stringResource(R.string.enable_blocklist)
                                                        )
                                                    },
                                                    onClick = {
                                                        showToolbarMenu = false
                                                        viewModel.toggleBlocklist(blocklistId, data.enabled != true)
                                                    },
                                                    enabled = blocklistProcess == null,
                                                    leadingIcon = {
                                                        Icon(
                                                            if (data.enabled == true) Icons.Rounded.Cancel else Icons.Rounded.CheckCircle,
                                                            contentDescription = null
                                                        )
                                                    }
                                                )
                                                // Delete
                                                DropdownMenuItem(
                                                    text = { Text(stringResource(R.string.delete_blocklist)) },
                                                    onClick = {
                                                        showToolbarMenu = false
                                                        showDeleteConfirm = true
                                                    },
                                                    enabled = blocklistProcess == null,
                                                    leadingIcon = {
                                                        Icon(
                                                            Icons.Rounded.Delete,
                                                            contentDescription = null,
                                                            tint = MaterialTheme.colorScheme.error
                                                        )
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            AnimatedContent(
                targetState = viewModel.state,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                contentKey = { it::class },
                label = "BlocklistDetailsState"
            ) { state ->
                when (state) {
                    is LoadingResult.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize().padding(innerPadding),
                            contentAlignment = Alignment.Center
                        ) { CircularProgressIndicator() }
                    }

                    is LoadingResult.Success -> {
                        BlocklistDetailsContent(
                            data = state.value.data,
                            innerPadding = innerPadding,
                            isRefreshing = viewModel.isRefreshing,
                            onRefresh = { viewModel.refresh(blocklistId) },
                            ipsRound = viewModel.ipsRound,
                            nestedScrollConnection = scrollBehavior.nestedScrollConnection,
                            onIncrementIpsRound = { viewModel.incrementIpsRound() },
                            snackbarHostState = snackbarHostState
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
                                IconButton(onClick = { viewModel.refresh(blocklistId) }) {
                                    Icon(Icons.Rounded.Refresh, contentDescription = null)
                                }
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = viewModel.searchPresented,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                ) {
                    val blocklistIps = successData?.data?.blocklistIps ?: emptyList()

                    if (viewModel.searchText.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Search,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = stringResource(R.string.enter_search_text),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        val filteredIps = remember(viewModel.searchText) {
                            blocklistIps.filter { it.startsWith(viewModel.searchText) }
                        }

                        if (filteredIps.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Search,
                                        contentDescription = null,
                                        modifier = Modifier.size(48.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = stringResource(R.string.no_results_for, viewModel.searchText),
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        } else {
                            val endIndex = min(viewModel.ipsRound * Defaults.IPS_AMOUNT_BATCH, filteredIps.size)
                            val slicedIps = filteredIps.subList(0, endIndex)

                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                items(slicedIps, key = { it }) { ip ->
                                    val index = slicedIps.indexOf(ip)
                                    RoundedCornersListTile(
                                        index = index,
                                        totalItems = slicedIps.size,
                                    ) {
                                        ListItemContent(headlineText = ip)
                                    }
                                    LaunchedEffect(ip) {
                                        if (ip == slicedIps.last() && endIndex < filteredIps.size) {
                                            viewModel.incrementIpsRound()
                                        }
                                    }
                                }
                                item { Spacer(modifier = Modifier.height(16.dp)) }
                            }
                        }
                    }
                }
            }
        }
    }
}
