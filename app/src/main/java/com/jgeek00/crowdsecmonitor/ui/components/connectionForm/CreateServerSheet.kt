package com.jgeek00.crowdsecmonitor.ui.components.connectionForm

import android.graphics.Color
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.jgeek00.crowdsecmonitor.R
import com.jgeek00.crowdsecmonitor.viewmodel.ConnectionFormViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateServerSheet(
    onClose: () -> Unit,
    viewModel: ConnectionFormViewModel = hiltViewModel()
) {
    val scope = rememberCoroutineScope()
    var showDiscardDialog by remember { mutableStateOf(false) }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    val darkTheme = isSystemInDarkTheme()
    val dialogWindow = (LocalView.current.parent as? DialogWindowProvider)?.window
    SideEffect {
        dialogWindow?.let { window ->
            WindowCompat.setDecorFitsSystemWindows(window, false)
            val controller = WindowInsetsControllerCompat(window, window.decorView)
            controller.isAppearanceLightStatusBars = !darkTheme
            controller.isAppearanceLightNavigationBars = !darkTheme
            @Suppress("DEPRECATION")
            window.statusBarColor = Color.TRANSPARENT
            @Suppress("DEPRECATION")
            window.navigationBarColor = Color.TRANSPARENT
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        topBar = {
            LargeTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                ),
                scrollBehavior = scrollBehavior,
                title = { Text(stringResource(R.string.create_server)) },
                navigationIcon = {
                    IconButton(
                        onClick = { showDiscardDialog = true },
                        enabled = !viewModel.connecting
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.close)
                        )
                    }
                },
                actions = {
                    if (viewModel.connecting) {
                        Box(
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            CircularProgressIndicator(
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    } else {
                        TextButton(
                            onClick = {
                                scope.launch {
                                    if (viewModel.connect()) {
                                        onClose()
                                    }
                                }
                            }
                        ) {
                            Text(stringResource(R.string.connect))
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        ConnectionForm(
            viewModel = viewModel,
            showHeader = false,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .nestedScroll(scrollBehavior.nestedScrollConnection)
        )
    }

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text(stringResource(R.string.discard_changes)) },
            text = { Text(stringResource(R.string.discard_changes_msg)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDiscardDialog = false
                        onClose()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.discard_changes))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) {
                    Text(stringResource(R.string.close))
                }
            }
        )
    }
}
