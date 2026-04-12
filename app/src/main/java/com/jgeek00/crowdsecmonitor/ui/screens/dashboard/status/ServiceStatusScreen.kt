package com.jgeek00.crowdsecmonitor.ui.screens.dashboard.status

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jgeek00.crowdsecmonitor.R
import com.jgeek00.crowdsecmonitor.constants.Enums
import com.jgeek00.crowdsecmonitor.data.models.ApiStatusResponse
import com.jgeek00.crowdsecmonitor.data.models.ApiStatusResponseProcess
import com.jgeek00.crowdsecmonitor.data.models.LoadingResult
import com.jgeek00.crowdsecmonitor.ui.components.FullScreenDialog
import com.jgeek00.crowdsecmonitor.ui.components.SectionHeader
import com.jgeek00.crowdsecmonitor.viewmodel.ServiceStatusViewModel


@Composable
fun ServiceStatusScreen(
    onClose: () -> Unit,
    serviceStatusViewModel: ServiceStatusViewModel = hiltViewModel()
) {
    val state by serviceStatusViewModel.status.collectAsState()

    FullScreenDialog(
        onClose = onClose,
        title = stringResource(R.string.service_status),
        allowClose = true,
        actions = {}
    ) { innerPadding ->
        when (state) {
            is LoadingResult.Loading -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Text(text = stringResource(R.string.loading))
                }
            }

            is LoadingResult.Success -> {
                Content(
                    status = (state as LoadingResult.Success).value,
                    innerPadding = innerPadding
                )
            }

            is LoadingResult.Failure -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Cancel,
                        contentDescription = null,
                        tint = Color.Red,
                    )
                    Text(text = stringResource(R.string.error))
                    Text(text = stringResource(R.string.error_fetching_service_status))
                }
            }
        }
    }
}

@Composable
private fun Content(status: ApiStatusResponse, innerPadding: PaddingValues) {
    val filteredFailed = status.processes.filter { it.successful == false }
    val filteredRunning = status.processes.filter { it.successful == null }
    val filteredSuccessful = status.processes.filter { it.successful == true }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = innerPadding
    ) {
        item {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(text = stringResource(R.string.lapi_available))
                    Spacer(modifier = Modifier.weight(1f))
                    if (status.csLapi.lapiConnected) {
                        Icon(imageVector = Icons.Rounded.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50))
                    } else {
                        Icon(imageVector = Icons.Rounded.Cancel, contentDescription = null, tint = Color.Red)
                    }
                }
                Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(text = stringResource(R.string.bouncer_available))
                    Spacer(modifier = Modifier.weight(1f))
                    if (status.csBouncer.available) {
                        Icon(imageVector = Icons.Rounded.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50))
                    } else {
                        Icon(imageVector = Icons.Rounded.Cancel, contentDescription = null, tint = Color.Red)
                    }
                }
            }
        }

        if (filteredFailed.isNotEmpty()) {
            item {
                SectionHeader(
                    text = stringResource(R.string.failed_tasks),
                    topPadding = Enums.SectionHeaderPaddingTop.SMALL,
                )
            }
            items(filteredFailed) { item ->
                ProcessSummary(item)
            }
        }

        if (filteredRunning.isNotEmpty()) {
            item {
                SectionHeader(
                    text = stringResource(R.string.running_tasks),
                    topPadding = if (filteredFailed.isNotEmpty()) Enums.SectionHeaderPaddingTop.NORMAL
                    else Enums.SectionHeaderPaddingTop.SMALL,
                )
            }
            items(filteredRunning) { item ->
                ProcessSummary(item)
            }
        }

        if (filteredSuccessful.isNotEmpty()) {
            item {
                SectionHeader(
                    text = stringResource(R.string.successful_tasks),
                    topPadding = if (filteredFailed.isNotEmpty() || filteredRunning.isNotEmpty()) Enums.SectionHeaderPaddingTop.NORMAL
                    else Enums.SectionHeaderPaddingTop.SMALL,
                )
            }
            items(filteredSuccessful) { item ->
                ProcessSummary(item)
            }
        }
    }
}

@Composable
private fun ProcessSummary(process: ApiStatusResponseProcess) {
    Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        if (process.blocklistImport != null || process.blocklistEnable != null) {
            ProcessBlocklistImportEnableStatus(process = process)
        }
        if (process.blocklistDelete != null || process.blocklistDisable != null) {
            ProcessBlocklistDeleteDisableStatus(process = process)
        }
        if (process.blocklistRefresh != null) {
           ProcessBlocklistRefreshStatus(process = process)
        }
    }
}