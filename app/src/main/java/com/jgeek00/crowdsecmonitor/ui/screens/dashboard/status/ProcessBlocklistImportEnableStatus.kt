package com.jgeek00.crowdsecmonitor.ui.screens.dashboard.status

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.jgeek00.crowdsecmonitor.R
import com.jgeek00.crowdsecmonitor.data.models.ApiStatusResponseProcess
import com.jgeek00.crowdsecmonitor.data.models.ApiStatusResponseProcessBlocklist
import com.jgeek00.crowdsecmonitor.data.models.ApiStatusResponseProcessBlocklistProgress
import com.jgeek00.crowdsecmonitor.data.models.ApiStatusResponseProcessBlocklistStep
import com.jgeek00.crowdsecmonitor.data.models.ApiStatusResponseProcessBlocklistFieldStatus
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material3.MaterialTheme
import com.jgeek00.crowdsecmonitor.extensions.toFormattedTimeOrNull

@Composable
fun ProcessBlocklistImportEnableStatus(process: ApiStatusResponseProcess) {
	val status: ApiStatusResponseProcessBlocklist? = process.blocklistImport ?: process.blocklistEnable
	if (status == null) return

	Column(modifier = Modifier
		.fillMaxWidth()
		.padding(vertical = 8.dp)) {
		if (process.blocklistImport != null) {
			Text(
				text = stringResource(R.string.import_blocklist_fmt, status.blocklistName),
				fontWeight = FontWeight.SemiBold,
				style = MaterialTheme.typography.bodyLarge
			)
		} else {
			Text(
				text = stringResource(R.string.enable_blocklist_fmt, status.blocklistName),
				fontWeight = FontWeight.SemiBold,
				style = MaterialTheme.typography.bodyLarge
			)
		}

        StatusProcessStepper(fetch = status.fetched, parse = status.parsed, imp = status.imported)

		if (status.step == ApiStatusResponseProcessBlocklistStep.IMPORT && process.successful == null) {
			val total = status.processIps.totalIps
			val processed = status.processIps.processedIps
			val progress = if (total > 0) processed.toDouble() / total.toDouble() else 0.0

			Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
				Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
					Text(text = stringResource(R.string.imported_progress_fmt, processed, total), style = MaterialTheme.typography.bodySmall)
					androidx.compose.foundation.layout.Spacer(modifier = Modifier.weight(1f))
					val percent = if (total > 0) ((processed.toDouble() / total.toDouble()) * 100).toInt() else 0
					Text(text = "$percent%", style = MaterialTheme.typography.bodySmall)
				}
                LinearProgressIndicator(
                    progress = { progress.toFloat() },
                    modifier = Modifier.fillMaxWidth(),
                    color = ProgressIndicatorDefaults.linearColor,
                    trackColor = ProgressIndicatorDefaults.linearTrackColor,
                    strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
                )
			}
		}

		if (status.step == ApiStatusResponseProcessBlocklistStep.IMPORT && process.successful == false) {
			Text(text = stringResource(R.string.imported_summary_fmt, status.processIps.processedIps, status.processIps.totalIps), style = MaterialTheme.typography.bodySmall)
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
fun Preview_ProcessBlocklistImportEnable_FetchRunning() {
	val proc = ApiStatusResponseProcess(
		id = "",
		beginDatetime = "2026-04-11T16:20:00.000Z",
		endDatetime = null,
		successful = null,
		error = null,
		blocklistImport = null,
		blocklistEnable = ApiStatusResponseProcessBlocklist(
			blocklistId = 1,
			blocklistName = "Blocklist 1",
			step = ApiStatusResponseProcessBlocklistStep.FETCH,
			fetched = ApiStatusResponseProcessBlocklistFieldStatus.RUNNING,
			parsed = ApiStatusResponseProcessBlocklistFieldStatus.PENDING,
			imported = ApiStatusResponseProcessBlocklistFieldStatus.PENDING,
			processIps = ApiStatusResponseProcessBlocklistProgress(totalIps = 1000, processedIps = 0)
		),
		blocklistDisable = null,
		blocklistDelete = null,
		blocklistRefresh = null
	)
	Column { ProcessBlocklistImportEnableStatus(process = proc) }
}

@Preview(showBackground = true)
@Composable
fun Preview_ProcessBlocklistImportEnable_ParseRunning() {
	val proc = ApiStatusResponseProcess(
		id = "",
		beginDatetime = "2026-04-11T16:20:00.000Z",
		endDatetime = null,
		successful = null,
		error = null,
		blocklistImport = null,
		blocklistEnable = ApiStatusResponseProcessBlocklist(
			blocklistId = 1,
			blocklistName = "Blocklist 1",
			step = ApiStatusResponseProcessBlocklistStep.PARSE,
			fetched = ApiStatusResponseProcessBlocklistFieldStatus.SUCCESSFUL,
			parsed = ApiStatusResponseProcessBlocklistFieldStatus.RUNNING,
			imported = ApiStatusResponseProcessBlocklistFieldStatus.PENDING,
			processIps = ApiStatusResponseProcessBlocklistProgress(totalIps = 1000, processedIps = 0)
		),
		blocklistDisable = null,
		blocklistDelete = null,
		blocklistRefresh = null
	)
	Column { ProcessBlocklistImportEnableStatus(process = proc) }
}

@Preview(showBackground = true)
@Composable
fun Preview_ProcessBlocklistImportEnable_ImportRunning() {
	val proc = ApiStatusResponseProcess(
		id = "",
		beginDatetime = "2026-04-11T16:20:00.000Z",
		endDatetime = null,
		successful = null,
		error = null,
		blocklistImport = ApiStatusResponseProcessBlocklist(
			blocklistId = 1,
			blocklistName = "Blocklist 1",
			step = ApiStatusResponseProcessBlocklistStep.IMPORT,
			fetched = ApiStatusResponseProcessBlocklistFieldStatus.SUCCESSFUL,
			parsed = ApiStatusResponseProcessBlocklistFieldStatus.SUCCESSFUL,
			imported = ApiStatusResponseProcessBlocklistFieldStatus.RUNNING,
			processIps = ApiStatusResponseProcessBlocklistProgress(totalIps = 1000, processedIps = 200)
		),
		blocklistEnable = null,
		blocklistDisable = null,
		blocklistDelete = null,
		blocklistRefresh = null
	)
	Column { ProcessBlocklistImportEnableStatus(process = proc) }
}

@Preview(showBackground = true)
@Composable
fun Preview_ProcessBlocklistImportEnable_ImportSuccess() {
	val proc = ApiStatusResponseProcess(
		id = "",
		beginDatetime = "2026-04-11T16:20:00.000Z",
		endDatetime = "2026-04-11T16:20:10.000Z",
		successful = true,
		error = null,
		blocklistImport = ApiStatusResponseProcessBlocklist(
			blocklistId = 1,
			blocklistName = "Blocklist 1",
			step = ApiStatusResponseProcessBlocklistStep.IMPORT,
			fetched = ApiStatusResponseProcessBlocklistFieldStatus.SUCCESSFUL,
			parsed = ApiStatusResponseProcessBlocklistFieldStatus.SUCCESSFUL,
			imported = ApiStatusResponseProcessBlocklistFieldStatus.SUCCESSFUL,
			processIps = ApiStatusResponseProcessBlocklistProgress(totalIps = 1000, processedIps = 1000)
		),
		blocklistEnable = null,
		blocklistDisable = null,
		blocklistDelete = null,
		blocklistRefresh = null
	)
	Column { ProcessBlocklistImportEnableStatus(process = proc) }
}




// moved parse/format helpers to `com.jgeek00.crowdsecmonitor.extensions.DateExtensions`


