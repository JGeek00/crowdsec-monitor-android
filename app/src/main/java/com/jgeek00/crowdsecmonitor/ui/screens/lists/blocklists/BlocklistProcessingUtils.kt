package com.jgeek00.crowdsecmonitor.ui.screens.lists.blocklists

import com.jgeek00.crowdsecmonitor.R
import com.jgeek00.crowdsecmonitor.data.models.ApiStatusResponse
import com.jgeek00.crowdsecmonitor.data.models.ApiStatusResponseProcess

fun getBlocklistActiveProcess(data: ApiStatusResponse?, blocklistId: String): ApiStatusResponseProcess? {
	if (data == null) return null

	val process = data.processes.firstOrNull { p ->
		p.blocklistEnable?.let { return@firstOrNull it.blocklistId.toString() == blocklistId }
		p.blocklistImport?.let { return@firstOrNull it.blocklistId.toString() == blocklistId }
		p.blocklistDisable?.let { return@firstOrNull it.blocklistId.toString() == blocklistId }
		p.blocklistDelete?.let { return@firstOrNull it.blocklistId.toString() == blocklistId }
		false
	}

	return if (process?.successful == null) process else null
}

fun getProcessType(process: ApiStatusResponseProcess): Int? {
	return when {
		process.blocklistEnable != null -> R.string.enabling_blocklist
		process.blocklistImport != null -> R.string.importing_blocklist
		process.blocklistDelete != null -> R.string.deleting_blocklist
		process.blocklistDisable != null -> R.string.disabling_blocklist
		else -> null
	}
}


