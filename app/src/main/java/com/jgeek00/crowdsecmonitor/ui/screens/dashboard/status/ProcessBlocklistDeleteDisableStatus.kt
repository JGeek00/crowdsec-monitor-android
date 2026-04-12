package com.jgeek00.crowdsecmonitor.ui.screens.dashboard.status

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.jgeek00.crowdsecmonitor.R
import com.jgeek00.crowdsecmonitor.data.models.ApiStatusResponseProcess
import com.jgeek00.crowdsecmonitor.data.models.ApiStatusResponseProcessBlocklistIps
import androidx.compose.ui.tooling.preview.Preview
import com.jgeek00.crowdsecmonitor.extensions.toFormattedTimeOrNull

@Composable
fun ProcessBlocklistDeleteDisableStatus(process: ApiStatusResponseProcess) {
	val status: ApiStatusResponseProcessBlocklistIps? = process.blocklistDelete ?: process.blocklistDisable
	if (status == null) return

	Column(modifier = Modifier
		.fillMaxWidth()
		.padding(vertical = 8.dp)) {
		if (process.blocklistDelete != null) {
			Text(text = stringResource(R.string.delete_blocklist_fmt, status.blocklistName), style = androidx.compose.material3.MaterialTheme.typography.bodyLarge)
		} else {
			Text(text = stringResource(R.string.disable_blocklist_fmt, status.blocklistName), style = androidx.compose.material3.MaterialTheme.typography.bodyLarge)
		}

		if (process.successful == null) {
			val total = status.ipsToDelete
			val processed = status.processedIps
			val progress = if (total > 0) processed.toDouble() / total.toDouble() else 0.0

			Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
				Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
					Text(text = stringResource(R.string.processed_progress_fmt, processed, total), style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
					androidx.compose.foundation.layout.Spacer(modifier = Modifier.weight(1f))
					val percent = if (total > 0) ((processed.toDouble() / total.toDouble()) * 100).toInt() else 0
					Text(text = "$percent%", style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
				}
				LinearProgressIndicator(progress = { progress.toFloat() }, modifier = Modifier.fillMaxWidth())
			}
		}

		if (process.successful == false) {
			Text(text = stringResource(R.string.processed_summary_fmt, status.processedIps, status.ipsToDelete), style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
		}

		if (process.successful == true) {
			Text(text = stringResource(R.string.processed_all_fmt, status.processedIps), style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
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
fun Preview_ProcessBlocklistDeleteDisable_Processing() {
	val proc = ApiStatusResponseProcess(
		id = "1",
		beginDatetime = "2026-04-11T16:20:00.000Z",
		endDatetime = null,
		successful = null,
		error = null,
		blocklistImport = null,
		blocklistEnable = null,
		blocklistDisable = ApiStatusResponseProcessBlocklistIps(blocklistId = 1, blocklistName = "Blocklist 1", blocklistIps = 1000, ipsToDelete = 1500, processedIps = 800),
		blocklistDelete = null,
		blocklistRefresh = null
	)
	Column { ProcessBlocklistDeleteDisableStatus(process = proc) }
}

@Preview(showBackground = true)
@Composable
fun Preview_ProcessBlocklistDeleteDisable_Error() {
	val proc = ApiStatusResponseProcess(
		id = "1",
		beginDatetime = "2026-04-11T16:20:00.000Z",
		endDatetime = "2026-04-11T16:20:07.000Z",
		successful = false,
		error = null,
		blocklistImport = null,
		blocklistEnable = null,
		blocklistDisable = ApiStatusResponseProcessBlocklistIps(blocklistId = 1, blocklistName = "Blocklist 1", blocklistIps = 1000, ipsToDelete = 1500, processedIps = 800),
		blocklistDelete = null,
		blocklistRefresh = null
	)
	Column { ProcessBlocklistDeleteDisableStatus(process = proc) }
}

@Preview(showBackground = true)
@Composable
fun Preview_ProcessBlocklistDeleteDisable_Success() {
	val proc = ApiStatusResponseProcess(
		id = "1",
		beginDatetime = "2026-04-11T16:20:00.000Z",
		endDatetime = "2026-04-11T16:20:07.000Z",
		successful = true,
		error = null,
		blocklistImport = null,
		blocklistEnable = null,
		blocklistDisable = ApiStatusResponseProcessBlocklistIps(blocklistId = 1, blocklistName = "Blocklist 1", blocklistIps = 1000, ipsToDelete = 1500, processedIps = 1500),
		blocklistDelete = null,
		blocklistRefresh = null
	)
	Column { ProcessBlocklistDeleteDisableStatus(process = proc) }
}

