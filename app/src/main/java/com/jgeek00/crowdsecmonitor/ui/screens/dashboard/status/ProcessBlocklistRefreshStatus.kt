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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.jgeek00.crowdsecmonitor.R
import com.jgeek00.crowdsecmonitor.data.models.ApiStatusResponseProcess
import com.jgeek00.crowdsecmonitor.data.models.ApiStatusResponseProcessBlocklistRefresh
import com.jgeek00.crowdsecmonitor.data.models.ApiStatusResponseProcessBlocklistRefreshBlocklist
import com.jgeek00.crowdsecmonitor.data.models.ApiStatusResponseProcessBlocklistFieldStatus
import com.jgeek00.crowdsecmonitor.extensions.toFormattedTimeOrNull
import com.jgeek00.crowdsecmonitor.ui.components.RoundedCornersListTile

@Composable
fun ProcessBlocklistRefreshStatus(process: ApiStatusResponseProcess) {
    val status: ApiStatusResponseProcessBlocklistRefresh = process.blocklistRefresh ?: return

    Column(
		verticalArrangement = Arrangement.spacedBy(12.dp),
		modifier = Modifier
			.fillMaxWidth()
	) {
		Text(
			text = stringResource(R.string.refresh_blocklists_title),
			style = MaterialTheme.typography.bodyLarge,
			fontWeight = FontWeight.SemiBold
		)

		if (process.successful == null) {
			val currentIndex = (status.currentBlocklist - 1).coerceIn(0, status.blocklists.size - 1)
			val currentBlocklist = status.blocklists[currentIndex]

			Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
				Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
					Text(text = stringResource(R.string.refresh_current_label), style = MaterialTheme.typography.bodySmall)
					Spacer(modifier = Modifier.weight(1f))
					Text(text = currentBlocklist.name, style = MaterialTheme.typography.bodySmall)
				}

				StatusProcessStepper(
					fetch = currentBlocklist.steps.fetch,
					parse = currentBlocklist.steps.parse,
					delete = currentBlocklist.steps.delete,
					imp = currentBlocklist.steps.imported,
					joinedMode = true
				)

				Spacer(modifier = Modifier.height(12.dp))

				Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
					Text(text = stringResource(R.string.refresh_total_label), style = MaterialTheme.typography.bodySmall)
					Spacer(modifier = Modifier.weight(1f))
					Text(
						text = stringResource(R.string.refresh_progress_of_fmt, status.currentBlocklist, status.totalBlocklists),
						style = MaterialTheme.typography.bodySmall
					)
				}

				val progress = if (status.totalBlocklists > 0) {
					status.currentBlocklist.toDouble() / status.totalBlocklists.toDouble()
				} else 0.0
				LinearProgressIndicator(progress = { progress.toFloat() }, modifier = Modifier.fillMaxWidth())
			}
		}

		if (process.successful == false) {
			val successfulCount = status.blocklists.count { bl ->
				bl.steps.fetch == ApiStatusResponseProcessBlocklistFieldStatus.SUCCESSFUL &&
				bl.steps.parse == ApiStatusResponseProcessBlocklistFieldStatus.SUCCESSFUL &&
				bl.steps.delete == ApiStatusResponseProcessBlocklistFieldStatus.SUCCESSFUL &&
				bl.steps.imported == ApiStatusResponseProcessBlocklistFieldStatus.SUCCESSFUL
			}
			Text(
				text = stringResource(R.string.processed_blocklists_summary_fmt, successfulCount, status.totalBlocklists),
				fontSize = 14.sp
			)
		}

		if (process.successful == true) {
			Text(
				text = stringResource(R.string.processed_blocklists_all_fmt, status.totalBlocklists),
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
fun Preview_ProcessBlocklistRefreshStatus_Running() {
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
		blocklistRefresh = ApiStatusResponseProcessBlocklistRefresh(
			totalBlocklists = 3,
			currentBlocklist = 2,
			blocklists = listOf(
				ApiStatusResponseProcessBlocklistRefreshBlocklist(
					number = 1,
					name = "Blocklist 1",
					steps = com.jgeek00.crowdsecmonitor.data.models.ApiStatusResponseProcessBlocklistRefreshBlocklistSteps(
						fetch = ApiStatusResponseProcessBlocklistFieldStatus.SUCCESSFUL,
						parse = ApiStatusResponseProcessBlocklistFieldStatus.SUCCESSFUL,
						delete = ApiStatusResponseProcessBlocklistFieldStatus.SUCCESSFUL,
						imported = ApiStatusResponseProcessBlocklistFieldStatus.SUCCESSFUL
					)
				),
				ApiStatusResponseProcessBlocklistRefreshBlocklist(
					number = 2,
					name = "Blocklist 2",
					steps = com.jgeek00.crowdsecmonitor.data.models.ApiStatusResponseProcessBlocklistRefreshBlocklistSteps(
						fetch = ApiStatusResponseProcessBlocklistFieldStatus.SUCCESSFUL,
						parse = ApiStatusResponseProcessBlocklistFieldStatus.SUCCESSFUL,
						delete = ApiStatusResponseProcessBlocklistFieldStatus.RUNNING,
						imported = ApiStatusResponseProcessBlocklistFieldStatus.PENDING
					)
				),
				ApiStatusResponseProcessBlocklistRefreshBlocklist(
					number = 3,
					name = "Blocklist 3",
					steps = com.jgeek00.crowdsecmonitor.data.models.ApiStatusResponseProcessBlocklistRefreshBlocklistSteps(
						fetch = ApiStatusResponseProcessBlocklistFieldStatus.PENDING,
						parse = ApiStatusResponseProcessBlocklistFieldStatus.PENDING,
						delete = ApiStatusResponseProcessBlocklistFieldStatus.PENDING,
						imported = ApiStatusResponseProcessBlocklistFieldStatus.PENDING
					)
				)
			),
			totalIps = 10000
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
			ProcessBlocklistRefreshStatus(process = proc)
		}
	}
}

@Preview(showBackground = true)
@Composable
fun Preview_ProcessBlocklistRefreshStatus_Successful() {
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
		blocklistRefresh = ApiStatusResponseProcessBlocklistRefresh(
			totalBlocklists = 3,
			currentBlocklist = 3,
			blocklists = listOf(
				ApiStatusResponseProcessBlocklistRefreshBlocklist(
					number = 1,
					name = "Blocklist 1",
					steps = com.jgeek00.crowdsecmonitor.data.models.ApiStatusResponseProcessBlocklistRefreshBlocklistSteps(
						fetch = ApiStatusResponseProcessBlocklistFieldStatus.SUCCESSFUL,
						parse = ApiStatusResponseProcessBlocklistFieldStatus.SUCCESSFUL,
						delete = ApiStatusResponseProcessBlocklistFieldStatus.SUCCESSFUL,
						imported = ApiStatusResponseProcessBlocklistFieldStatus.SUCCESSFUL
					)
				),
				ApiStatusResponseProcessBlocklistRefreshBlocklist(
					number = 2,
					name = "Blocklist 2",
					steps = com.jgeek00.crowdsecmonitor.data.models.ApiStatusResponseProcessBlocklistRefreshBlocklistSteps(
						fetch = ApiStatusResponseProcessBlocklistFieldStatus.SUCCESSFUL,
						parse = ApiStatusResponseProcessBlocklistFieldStatus.SUCCESSFUL,
						delete = ApiStatusResponseProcessBlocklistFieldStatus.SUCCESSFUL,
						imported = ApiStatusResponseProcessBlocklistFieldStatus.SUCCESSFUL
					)
				),
				ApiStatusResponseProcessBlocklistRefreshBlocklist(
					number = 3,
					name = "Blocklist 3",
					steps = com.jgeek00.crowdsecmonitor.data.models.ApiStatusResponseProcessBlocklistRefreshBlocklistSteps(
						fetch = ApiStatusResponseProcessBlocklistFieldStatus.SUCCESSFUL,
						parse = ApiStatusResponseProcessBlocklistFieldStatus.SUCCESSFUL,
						delete = ApiStatusResponseProcessBlocklistFieldStatus.SUCCESSFUL,
						imported = ApiStatusResponseProcessBlocklistFieldStatus.SUCCESSFUL
					)
				)
			),
			totalIps = 10000
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
			ProcessBlocklistRefreshStatus(process = proc)
		}
	}
}

@Preview(showBackground = true)
@Composable
fun Preview_ProcessBlocklistRefreshStatus_Failed() {
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
		blocklistRefresh = ApiStatusResponseProcessBlocklistRefresh(
			totalBlocklists = 3,
			currentBlocklist = 2,
			blocklists = listOf(
				ApiStatusResponseProcessBlocklistRefreshBlocklist(
					number = 1,
					name = "Blocklist 1",
					steps = com.jgeek00.crowdsecmonitor.data.models.ApiStatusResponseProcessBlocklistRefreshBlocklistSteps(
						fetch = ApiStatusResponseProcessBlocklistFieldStatus.SUCCESSFUL,
						parse = ApiStatusResponseProcessBlocklistFieldStatus.SUCCESSFUL,
						delete = ApiStatusResponseProcessBlocklistFieldStatus.SUCCESSFUL,
						imported = ApiStatusResponseProcessBlocklistFieldStatus.SUCCESSFUL
					)
				),
				ApiStatusResponseProcessBlocklistRefreshBlocklist(
					number = 2,
					name = "Blocklist 2",
					steps = com.jgeek00.crowdsecmonitor.data.models.ApiStatusResponseProcessBlocklistRefreshBlocklistSteps(
						fetch = ApiStatusResponseProcessBlocklistFieldStatus.SUCCESSFUL,
						parse = ApiStatusResponseProcessBlocklistFieldStatus.SUCCESSFUL,
						delete = ApiStatusResponseProcessBlocklistFieldStatus.FAILED,
						imported = ApiStatusResponseProcessBlocklistFieldStatus.PENDING
					)
				),
				ApiStatusResponseProcessBlocklistRefreshBlocklist(
					number = 3,
					name = "Blocklist 3",
					steps = com.jgeek00.crowdsecmonitor.data.models.ApiStatusResponseProcessBlocklistRefreshBlocklistSteps(
						fetch = ApiStatusResponseProcessBlocklistFieldStatus.PENDING,
						parse = ApiStatusResponseProcessBlocklistFieldStatus.PENDING,
						delete = ApiStatusResponseProcessBlocklistFieldStatus.PENDING,
						imported = ApiStatusResponseProcessBlocklistFieldStatus.PENDING
					)
				)
			),
			totalIps = 10000
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
			ProcessBlocklistRefreshStatus(process = proc)
		}
	}
}
