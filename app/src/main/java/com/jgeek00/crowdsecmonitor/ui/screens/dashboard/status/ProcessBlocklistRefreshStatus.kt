package com.jgeek00.crowdsecmonitor.ui.screens.dashboard.status

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.jgeek00.crowdsecmonitor.R
import com.jgeek00.crowdsecmonitor.data.models.ApiStatusResponseProcess
import com.jgeek00.crowdsecmonitor.data.models.ApiStatusResponseProcessBlocklistRefresh
import androidx.compose.ui.tooling.preview.Preview
import com.jgeek00.crowdsecmonitor.extensions.toFormattedTimeOrNull

@Composable
fun ProcessBlocklistRefreshStatus(process: ApiStatusResponseProcess) {
    val status: ApiStatusResponseProcessBlocklistRefresh = process.blocklistRefresh ?: return

    Column(modifier = Modifier
		.fillMaxWidth()
		.padding(vertical = 8.dp)) {
		Text(text = stringResource(R.string.refresh_blocklists_title), style = MaterialTheme.typography.bodyLarge)

		if (process.successful == null) {
			val total = status.totalBlocklists
			val processed = status.processedBlocklists
			val progress = if (total > 0) processed.toDouble() / total.toDouble() else 0.0

			Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
				Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
					Text(text = stringResource(R.string.processed_blocklists_progress_fmt, processed, total), style = MaterialTheme.typography.bodySmall)
					androidx.compose.foundation.layout.Spacer(modifier = Modifier.weight(1f))
					val percent = if (total > 0) ((processed.toDouble() / total.toDouble()) * 100).toInt() else 0
					Text(text = "$percent%", style = MaterialTheme.typography.bodySmall)
				}
				LinearProgressIndicator(progress = { progress.toFloat() }, modifier = Modifier.fillMaxWidth())
			}
		}

		if (process.successful == false) {
			Text(text = stringResource(R.string.processed_blocklists_summary_fmt, status.processedBlocklists, status.totalBlocklists), style = MaterialTheme.typography.bodySmall)
		}

		if (process.successful == true) {
			Text(text = stringResource(R.string.processed_blocklists_all_fmt, status.processedBlocklists), style = MaterialTheme.typography.bodySmall)
		}

		Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
			val start = process.beginDatetime.toFormattedTimeOrNull()
			if (start != null) {
				Text(text = stringResource(R.string.started_at_fmt, start))
			}
			val end = process.endDatetime?.let { it.toFormattedTimeOrNull() }
			if (end != null) {
				androidx.compose.foundation.layout.Spacer(modifier = Modifier.weight(1f))
				Text(text = stringResource(R.string.finished_at_fmt, end))
			}
		}
	}
}

@Preview(showBackground = true)
@Composable
fun Preview_ProcessBlocklistRefreshStatus_Default() {
    val proc = ApiStatusResponseProcess(
        id = "1",
        beginDatetime = "2026-04-11T16:20:00.000Z",
        endDatetime = "2026-04-11T16:20:07.000Z",
        successful = null,
        error = null,
        blocklistImport = null,
        blocklistEnable = null,
        blocklistDisable = null,
        blocklistDelete = null,
        blocklistRefresh = ApiStatusResponseProcessBlocklistRefresh(totalBlocklists = 10, processedBlocklists = 5, successful = 0, failed = 0)
    )
    Column { ProcessBlocklistRefreshStatus(process = proc) }
}
