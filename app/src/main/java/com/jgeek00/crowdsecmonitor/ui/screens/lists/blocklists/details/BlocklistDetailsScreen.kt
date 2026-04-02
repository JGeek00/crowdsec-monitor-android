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
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jgeek00.crowdsecmonitor.R
import com.jgeek00.crowdsecmonitor.constants.Defaults
import com.jgeek00.crowdsecmonitor.data.models.LoadingResult
import com.jgeek00.crowdsecmonitor.ui.components.ListItemContent
import com.jgeek00.crowdsecmonitor.ui.components.RoundedCornersListTile
import com.jgeek00.crowdsecmonitor.viewmodel.BlocklistDetailsViewModel
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BlocklistDetailsScreen(
    blocklistId: Int,
    showBackButton: Boolean = false,
    onNavigateBack: () -> Unit = {}
) {
    val viewModel: BlocklistDetailsViewModel = hiltViewModel(key = blocklistId.toString())
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(blocklistId) {
        viewModel.initialize(blocklistId)
    }

    val successData = (viewModel.state as? LoadingResult.Success)?.value
    val title = successData?.data?.name ?: stringResource(R.string.blocklist_details)

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
                            if (successData != null) {
                                TooltipBox(
                                    positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Below),
                                    tooltip = { PlainTooltip { Text(stringResource(R.string.search_ips)) } },
                                    state = rememberTooltipState()
                                ) {
                                    IconButton(onClick = { viewModel.updateSearchPresented(true) }) {
                                        Icon(Icons.Rounded.Search, contentDescription = stringResource(R.string.search_ips))
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
                            modifier = Modifier.fillMaxSize().padding(innerPadding),
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
                                    style = MaterialTheme.typography.bodyLarge
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
                                    text = stringResource(R.string.enter_search_text),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
