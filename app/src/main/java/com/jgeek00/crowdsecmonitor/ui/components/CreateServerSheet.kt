package com.jgeek00.crowdsecmonitor.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
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

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.create_server)) },
                navigationIcon = {
                    IconButton(
                        onClick = { showDiscardDialog = true },
                        enabled = !viewModel.connecting
                    ) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.close))
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            scope.launch {
                                if (viewModel.connect()) {
                                    onClose()
                                }
                            }
                        },
                        enabled = !viewModel.connecting
                    ) {
                        if (viewModel.connecting) {
                            CircularProgressIndicator(modifier = Modifier.padding(end = 8.dp))
                        } else {
                            Text(stringResource(R.string.connect))
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            ConnectionForm(
                viewModel = viewModel,
                showHeader = false,
                modifier = Modifier.fillMaxSize()
            )
        }
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
