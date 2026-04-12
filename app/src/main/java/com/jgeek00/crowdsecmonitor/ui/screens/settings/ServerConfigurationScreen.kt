package com.jgeek00.crowdsecmonitor.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.jgeek00.crowdsecmonitor.R
import com.jgeek00.crowdsecmonitor.constants.Enums
import com.jgeek00.crowdsecmonitor.ui.components.SectionHeader
import com.jgeek00.crowdsecmonitor.ui.components.connectionForm.CreateServerSheet
import com.jgeek00.crowdsecmonitor.ui.screens.settings.components.ServerInformationSection
import com.jgeek00.crowdsecmonitor.ui.screens.settings.components.ServerListItem
import com.jgeek00.crowdsecmonitor.ui.theme.CrowdSecMonitorTheme
import com.jgeek00.crowdsecmonitor.ui.theme.LocalDarkTheme
import com.jgeek00.crowdsecmonitor.viewmodel.ServersManagerViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ServerConfigurationScreen(
    onBack: () -> Unit,
    serversManagerViewModel: ServersManagerViewModel = hiltViewModel()
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val snackbarHostState = remember { SnackbarHostState() }
    var showCreateServerSheet by remember { mutableStateOf(false) }

    val newDefault = serversManagerViewModel.newDefaultServerSet
    val newDefaultMsg = newDefault?.let { stringResource(R.string.new_default_server_msg, it) }
    LaunchedEffect(newDefault) {
        if (newDefault != null && newDefaultMsg != null) {
            snackbarHostState.showSnackbar(newDefaultMsg)
            serversManagerViewModel.clearNewDefaultServerSet()
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            LargeFlexibleTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                ),
                scrollBehavior = scrollBehavior,
                title = { Text(stringResource(R.string.server_configuration)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            item {
                SectionHeader(stringResource(R.string.servers_section), topPadding = Enums.SectionHeaderPaddingTop.SMALL)
            }
            if (serversManagerViewModel.servers.isNotEmpty()) {
                items(serversManagerViewModel.servers, key = { it.id }) { server ->
                    val index = serversManagerViewModel.servers.indexOf(server)
                    ServerListItem(
                        index = index,
                        totalItems = serversManagerViewModel.servers.size,
                        server = server,
                        isCurrentServer = server.id == serversManagerViewModel.currentServer?.id,
                        onSelect = { serversManagerViewModel.changeCurrentServer(server) },
                        onSetDefault = { serversManagerViewModel.setDefaultServer(server) },
                        onDelete = { serversManagerViewModel.deleteServer(server) },
                    )
                }
            } else {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.no_servers_configured),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
            item {
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Button(onClick = { showCreateServerSheet = true }) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = null,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.add_server))
                    }
                }
            }
            if (serversManagerViewModel.hasServerConfigured) {
                item {
                    ServerInformationSection()
                }
            }
        }
    }

    if (serversManagerViewModel.deleteServerError) {
        AlertDialog(
            onDismissRequest = { serversManagerViewModel.clearDeleteServerError() },
            title = { Text(stringResource(R.string.error)) },
            text = { Text(stringResource(R.string.delete_server_error)) },
            confirmButton = {
                TextButton(onClick = { serversManagerViewModel.clearDeleteServerError() }) {
                    Text(stringResource(R.string.ok))
                }
            }
        )
    }

    if (serversManagerViewModel.setDefaultServerError) {
        AlertDialog(
            onDismissRequest = { serversManagerViewModel.clearSetDefaultServerError() },
            title = { Text(stringResource(R.string.error)) },
            text = { Text(stringResource(R.string.set_default_server_error)) },
            confirmButton = {
                TextButton(onClick = { serversManagerViewModel.clearSetDefaultServerError() }) {
                    Text(stringResource(R.string.ok))
                }
            }
        )
    }

    if (showCreateServerSheet) {
        val darkTheme = LocalDarkTheme.current
        Dialog(
            onDismissRequest = { showCreateServerSheet = false },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {
            CrowdSecMonitorTheme(darkTheme = darkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CreateServerSheet(onClose = { showCreateServerSheet = false })
                }
            }
        }
    }
}
