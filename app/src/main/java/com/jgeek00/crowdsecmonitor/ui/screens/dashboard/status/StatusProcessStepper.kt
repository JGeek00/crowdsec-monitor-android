package com.jgeek00.crowdsecmonitor.ui.screens.dashboard.status

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jgeek00.crowdsecmonitor.R
import com.jgeek00.crowdsecmonitor.data.models.ApiStatusResponseProcessBlocklistFieldStatus
import com.jgeek00.crowdsecmonitor.data.models.ApiStatusResponseProcessBlocklistStep
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.ui.text.font.FontWeight

@Composable
fun StatusProcessStepper(
	fetch: ApiStatusResponseProcessBlocklistFieldStatus,
	parse: ApiStatusResponseProcessBlocklistFieldStatus,
	imp: ApiStatusResponseProcessBlocklistFieldStatus,
	modifier: Modifier = Modifier
) {
	Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
		StepPill(step = ApiStatusResponseProcessBlocklistStep.FETCH, status = fetch)
		StepDivider()
		StepPill(step = ApiStatusResponseProcessBlocklistStep.PARSE, status = parse)
		StepDivider()
		StepPill(step = ApiStatusResponseProcessBlocklistStep.IMPORT, status = imp)
	}
}

@Composable
private fun StepPill(step: ApiStatusResponseProcessBlocklistStep, status: ApiStatusResponseProcessBlocklistFieldStatus) {
	val color = when (status) {
		ApiStatusResponseProcessBlocklistFieldStatus.PENDING -> Color.Gray
		ApiStatusResponseProcessBlocklistFieldStatus.RUNNING -> Color(0xFF2196F3) // blue
		ApiStatusResponseProcessBlocklistFieldStatus.SUCCESSFUL -> Color(0xFF4CAF50) // green
		ApiStatusResponseProcessBlocklistFieldStatus.FAILED -> Color.Red
	}

	val label = when (step) {
		ApiStatusResponseProcessBlocklistStep.FETCH -> stringResource(R.string.step_fetch)
		ApiStatusResponseProcessBlocklistStep.PARSE -> stringResource(R.string.step_parse)
		ApiStatusResponseProcessBlocklistStep.IMPORT -> stringResource(R.string.step_import)
	}

	Row(
		verticalAlignment = Alignment.CenterVertically,
		modifier = Modifier
			.clip(RoundedCornerShape(20.dp))
			.background(color)
			.padding(horizontal = 8.dp, vertical = 4.dp)
	) {
		when (status) {
			ApiStatusResponseProcessBlocklistFieldStatus.RUNNING -> {
				CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = Color.White)
				Spacer(modifier = Modifier.width(6.dp))
			}
			ApiStatusResponseProcessBlocklistFieldStatus.SUCCESSFUL -> {
				Icon(imageVector = Icons.Rounded.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
				Spacer(modifier = Modifier.width(4.dp))
			}
			ApiStatusResponseProcessBlocklistFieldStatus.FAILED -> {
				Icon(imageVector = Icons.Rounded.Cancel, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
				Spacer(modifier = Modifier.width(4.dp))
			}
			else -> { /* pending: no icon */ }
		}

		Text(
			text = label,
			color = Color.White,
			style = MaterialTheme.typography.bodySmall,
			fontWeight = FontWeight.Medium
		)
	}
}

@Composable
private fun RowScope.StepDivider() {
	Box(
		modifier = Modifier
			.weight(1f)
			.height(2.dp)
			.padding(horizontal = 4.dp)
			.background(Color.Gray, shape = RoundedCornerShape(20.dp))
	)
}


@Preview(showBackground = true)
@Composable
fun Preview_StatusProcessStepper_FetchRunning() {
    Box(
		contentAlignment = Alignment.Center,
		modifier = Modifier
			.fillMaxSize()
			.background(MaterialTheme.colorScheme.surfaceVariant)
			.padding(16.dp)
	) {
		StatusProcessStepper(fetch = ApiStatusResponseProcessBlocklistFieldStatus.RUNNING, parse = ApiStatusResponseProcessBlocklistFieldStatus.PENDING, imp = ApiStatusResponseProcessBlocklistFieldStatus.PENDING)
	}
}

@Preview(showBackground = true)
@Composable
fun Preview_StatusProcessStepper_FetchFailed() {
	Box(
		contentAlignment = Alignment.Center,
		modifier = Modifier
			.fillMaxSize()
			.background(MaterialTheme.colorScheme.surfaceVariant)
			.padding(16.dp)
	) {
		StatusProcessStepper(fetch = ApiStatusResponseProcessBlocklistFieldStatus.FAILED, parse = ApiStatusResponseProcessBlocklistFieldStatus.PENDING, imp = ApiStatusResponseProcessBlocklistFieldStatus.PENDING)
	}
}

@Preview(showBackground = true)
@Composable
fun Preview_StatusProcessStepper_ParseRunning() {
	Box(
		contentAlignment = Alignment.Center,
		modifier = Modifier
			.fillMaxSize()
			.background(MaterialTheme.colorScheme.surfaceVariant)
			.padding(16.dp)
	) {
		StatusProcessStepper(fetch = ApiStatusResponseProcessBlocklistFieldStatus.SUCCESSFUL, parse = ApiStatusResponseProcessBlocklistFieldStatus.RUNNING, imp = ApiStatusResponseProcessBlocklistFieldStatus.PENDING)
	}
}

@Preview(showBackground = true)
@Composable
fun Preview_StatusProcessStepper_ParseFailed() {
	Box(
		contentAlignment = Alignment.Center,
		modifier = Modifier
			.fillMaxSize()
			.background(MaterialTheme.colorScheme.surfaceVariant)
			.padding(16.dp)
	) {
		StatusProcessStepper(fetch = ApiStatusResponseProcessBlocklistFieldStatus.SUCCESSFUL, parse = ApiStatusResponseProcessBlocklistFieldStatus.FAILED, imp = ApiStatusResponseProcessBlocklistFieldStatus.PENDING)
	}
}

@Preview(showBackground = true)
@Composable
fun Preview_StatusProcessStepper_ImportRunning() {
	Box(
		contentAlignment = Alignment.Center,
		modifier = Modifier
			.fillMaxSize()
			.background(MaterialTheme.colorScheme.surfaceVariant)
			.padding(16.dp)
	) {
		StatusProcessStepper(fetch = ApiStatusResponseProcessBlocklistFieldStatus.SUCCESSFUL, parse = ApiStatusResponseProcessBlocklistFieldStatus.SUCCESSFUL, imp = ApiStatusResponseProcessBlocklistFieldStatus.RUNNING)
	}
}

@Preview(showBackground = true)
@Composable
fun Preview_StatusProcessStepper_ImportFailed() {
	Box(
		contentAlignment = Alignment.Center,
		modifier = Modifier
			.fillMaxSize()
			.background(MaterialTheme.colorScheme.surfaceVariant)
			.padding(16.dp)
	) {
		StatusProcessStepper(fetch = ApiStatusResponseProcessBlocklistFieldStatus.SUCCESSFUL, parse = ApiStatusResponseProcessBlocklistFieldStatus.SUCCESSFUL, imp = ApiStatusResponseProcessBlocklistFieldStatus.FAILED)
	}
}

@Preview(showBackground = true)
@Composable
fun Preview_StatusProcessStepper_ImportSuccess() {
	Box(
		contentAlignment = Alignment.Center,
		modifier = Modifier
			.fillMaxSize()
			.background(MaterialTheme.colorScheme.surfaceVariant)
			.padding(16.dp)
	) {
		StatusProcessStepper(fetch = ApiStatusResponseProcessBlocklistFieldStatus.SUCCESSFUL, parse = ApiStatusResponseProcessBlocklistFieldStatus.SUCCESSFUL, imp = ApiStatusResponseProcessBlocklistFieldStatus.SUCCESSFUL)
	}
}