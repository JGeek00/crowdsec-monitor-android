package com.jgeek00.crowdsecmonitor.ui.screens.dashboard.status

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jgeek00.crowdsecmonitor.R
import com.jgeek00.crowdsecmonitor.data.models.ApiStatusResponseProcess
import com.jgeek00.crowdsecmonitor.data.models.ApiStatusResponseProcessBlocklistProgress
import com.jgeek00.crowdsecmonitor.data.models.ApiStatusResponseProcessBlocklistSingleRefresh
import com.jgeek00.crowdsecmonitor.data.models.ApiStatusResponseProcessBlocklistStep
import com.jgeek00.crowdsecmonitor.data.models.ApiStatusResponseProcessBlocklistFieldStatus
import com.jgeek00.crowdsecmonitor.extensions.toFormattedTimeOrNull
import com.jgeek00.crowdsecmonitor.ui.components.RoundedCornersListTile

@Composable
fun ProcessBlocklistSingleRefreshStatus(process: ApiStatusResponseProcess) {
    val status: ApiStatusResponseProcessBlocklistSingleRefresh = process.blocklistSingleRefresh ?: return

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = stringResource(R.string.refresh_blocklist_fmt, status.blocklistName),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold
        )

        StatusProcessStepper(
            fetch = status.fetched,
            parse = status.parsed,
            delete = status.deleted,
            imp = status.imported,
            joinedMode = true
        )

        if (status.step == ApiStatusResponseProcessBlocklistStep.IMPORT && process.successful == null) {
            val total = status.processIps.totalIps
            val processed = status.processIps.processedIps
            val progress = if (total > 0) processed.toDouble() / total.toDouble() else 0.0

            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.imported_progress_fmt, processed, total),
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    val percent = if (total > 0) ((processed.toDouble() / total.toDouble()) * 100).toInt() else 0
                    Text(text = "$percent%", style = MaterialTheme.typography.bodySmall)
                }
                LinearProgressIndicator(progress = { progress.toFloat() }, modifier = Modifier.fillMaxWidth())
            }
        }

        if (process.successful == false) {
            Text(
                text = stringResource(R.string.imported_summary_fmt, status.processIps.processedIps, status.processIps.totalIps),
                fontSize = 14.sp
            )
        }

        if (process.successful == true) {
            Text(
                text = stringResource(R.string.processed_all_fmt, status.processIps.processedIps),
                fontSize = 14.sp
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            val start = process.beginDatetime.toFormattedTimeOrNull()
            if (start != null) {
                Text(text = stringResource(R.string.started_at_fmt, start), fontSize = 14.sp)
            }
            val end = process.endDatetime?.toFormattedTimeOrNull()
            if (end != null) {
                Spacer(modifier = Modifier.weight(1f))
                Text(text = stringResource(R.string.finished_at_fmt, end), fontSize = 14.sp)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun Preview_ProcessBlocklistSingleRefreshStatus_Running() {
    val proc = ApiStatusResponseProcess(
        id = "1",
        beginDatetime = "2026-04-11T16:20:00.000Z",
        endDatetime = null,
        successful = null,
        error = null,
        blocklistImport = null,
        blocklistEnable = null,
        blocklistDisable = null,
        blocklistDelete = null,
        blocklistRefresh = null,
        blocklistSingleRefresh = ApiStatusResponseProcessBlocklistSingleRefresh(
            blocklistId = 1,
            blocklistName = "Blocklist 1",
            step = ApiStatusResponseProcessBlocklistStep.IMPORT,
            fetched = ApiStatusResponseProcessBlocklistFieldStatus.SUCCESSFUL,
            parsed = ApiStatusResponseProcessBlocklistFieldStatus.SUCCESSFUL,
            deleted = ApiStatusResponseProcessBlocklistFieldStatus.SUCCESSFUL,
            imported = ApiStatusResponseProcessBlocklistFieldStatus.RUNNING,
            processIps = ApiStatusResponseProcessBlocklistProgress(totalIps = 1000, processedIps = 350)
        )
    )
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp)
    ) {
        RoundedCornersListTile(
            index = 0,
            totalItems = 1,
        ) {
            ProcessBlocklistSingleRefreshStatus(process = proc)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun Preview_ProcessBlocklistSingleRefreshStatus_Successful() {
    val proc = ApiStatusResponseProcess(
        id = "1",
        beginDatetime = "2026-04-11T16:20:00.000Z",
        endDatetime = "2026-04-11T16:25:00.000Z",
        successful = true,
        error = null,
        blocklistImport = null,
        blocklistEnable = null,
        blocklistDisable = null,
        blocklistDelete = null,
        blocklistRefresh = null,
        blocklistSingleRefresh = ApiStatusResponseProcessBlocklistSingleRefresh(
            blocklistId = 1,
            blocklistName = "Blocklist 1",
            step = ApiStatusResponseProcessBlocklistStep.IMPORT,
            fetched = ApiStatusResponseProcessBlocklistFieldStatus.SUCCESSFUL,
            parsed = ApiStatusResponseProcessBlocklistFieldStatus.SUCCESSFUL,
            deleted = ApiStatusResponseProcessBlocklistFieldStatus.SUCCESSFUL,
            imported = ApiStatusResponseProcessBlocklistFieldStatus.SUCCESSFUL,
            processIps = ApiStatusResponseProcessBlocklistProgress(totalIps = 1000, processedIps = 1000)
        )
    )
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp)
    ) {
        RoundedCornersListTile(
            index = 0,
            totalItems = 1,
        ) {
            ProcessBlocklistSingleRefreshStatus(process = proc)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun Preview_ProcessBlocklistSingleRefreshStatus_Failed() {
    val proc = ApiStatusResponseProcess(
        id = "1",
        beginDatetime = "2026-04-11T16:20:00.000Z",
        endDatetime = "2026-04-11T16:23:00.000Z",
        successful = false,
        error = "Connection error",
        blocklistImport = null,
        blocklistEnable = null,
        blocklistDisable = null,
        blocklistDelete = null,
        blocklistRefresh = null,
        blocklistSingleRefresh = ApiStatusResponseProcessBlocklistSingleRefresh(
            blocklistId = 1,
            blocklistName = "Blocklist 1",
            step = ApiStatusResponseProcessBlocklistStep.IMPORT,
            fetched = ApiStatusResponseProcessBlocklistFieldStatus.SUCCESSFUL,
            parsed = ApiStatusResponseProcessBlocklistFieldStatus.SUCCESSFUL,
            deleted = ApiStatusResponseProcessBlocklistFieldStatus.FAILED,
            imported = ApiStatusResponseProcessBlocklistFieldStatus.PENDING,
            processIps = ApiStatusResponseProcessBlocklistProgress(totalIps = 1000, processedIps = 500)
        )
    )
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp)
    ) {
        RoundedCornersListTile(
            index = 0,
            totalItems = 1,
        ) {
            ProcessBlocklistSingleRefreshStatus(process = proc)
        }
    }
}
