package com.jgeek00.crowdsecmonitor.ui.screens.decisions.create

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jgeek00.crowdsecmonitor.R
import com.jgeek00.crowdsecmonitor.constants.Enums
import com.jgeek00.crowdsecmonitor.ui.components.DurationPickerView
import com.jgeek00.crowdsecmonitor.ui.components.FullScreenDialog
import com.jgeek00.crowdsecmonitor.ui.components.SectionHeader
import com.jgeek00.crowdsecmonitor.viewmodel.CreateDecisionFormViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateDecisionFormScreen(
    onClose: () -> Unit,
    viewModel: CreateDecisionFormViewModel = hiltViewModel()
) {
    FullScreenDialog(
        title = stringResource(R.string.create_a_decision),
        onClose = onClose,
        allowClose = !viewModel.creatingDecision,
        dismissConfirmation = true,
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                SectionHeader(
                    text = stringResource(R.string.ip_address),
                    topPadding = Enums.SectionHeaderPaddingTop.SMALL
                )
                OutlinedTextField(
                    value = viewModel.ipAddress,
                    onValueChange = { viewModel.ipAddress = it },
                    label = { Text(stringResource(R.string.ip_address)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !viewModel.creatingDecision
                )

                SectionHeader(text = stringResource(R.string.type))
                DecisionTypeDropdown(
                    selectedType = viewModel.type,
                    onTypeSelected = { viewModel.type = it },
                    enabled = !viewModel.creatingDecision
                )

                SectionHeader(text = stringResource(R.string.duration))
                DurationPickerView(
                    days = viewModel.durationDays,
                    hours = viewModel.durationHours,
                    minutes = viewModel.durationMinutes,
                    onDaysChanged = { viewModel.durationDays = it },
                    onHoursChanged = { viewModel.durationHours = it },
                    onMinutesChanged = { viewModel.durationMinutes = it },
                    enabled = !viewModel.creatingDecision
                )

                SectionHeader(text = stringResource(R.string.reason))
                OutlinedTextField(
                    value = viewModel.reason,
                    onValueChange = { viewModel.reason = it },
                    label = { Text(stringResource(R.string.reason)) },
                    minLines = 4,
                    maxLines = 6,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !viewModel.creatingDecision
                )

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    ) {
        TooltipBox(
            positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Below),
            tooltip = { PlainTooltip { Text(stringResource(R.string.save)) } },
            state = rememberTooltipState()
        ) {
            IconButton(
                onClick = { viewModel.save(onSuccess = onClose) },
                enabled = !viewModel.creatingDecision
            ) {
                if (viewModel.creatingDecision) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .padding(8.dp)
                            .height(24.dp),
                        strokeWidth = 2.5.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = stringResource(R.string.save)
                    )
                }
            }
        }
    }

    if (viewModel.invalidFieldsAlert) {
        AlertDialog(
            onDismissRequest = { viewModel.invalidFieldsAlert = false },
            title = { Text(stringResource(R.string.invalid_values)) },
            text = { Text(viewModel.invalidFieldsAlertMessage) },
            confirmButton = {
                TextButton(onClick = { viewModel.invalidFieldsAlert = false }) {
                    Text(stringResource(R.string.ok))
                }
            }
        )
    }

    if (viewModel.errorCreatingDecisionAlert) {
        AlertDialog(
            onDismissRequest = { viewModel.errorCreatingDecisionAlert = false },
            title = { Text(stringResource(R.string.error)) },
            text = { Text(stringResource(R.string.error_creating_decision)) },
            confirmButton = {
                TextButton(onClick = { viewModel.errorCreatingDecisionAlert = false }) {
                    Text(stringResource(R.string.ok))
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DecisionTypeDropdown(
    selectedType: Enums.DecisionType,
    onTypeSelected: (Enums.DecisionType) -> Unit,
    enabled: Boolean
) {
    var expanded by remember { mutableStateOf(false) }

    val displayName = when (selectedType) {
        Enums.DecisionType.BAN -> stringResource(R.string.ban)
        Enums.DecisionType.CAPTCHA -> stringResource(R.string.captcha)
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (enabled) expanded = it }
    ) {
        OutlinedTextField(
            value = displayName,
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.type)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
            enabled = enabled
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            Enums.DecisionType.entries.forEach { decisionType ->
                val name = when (decisionType) {
                    Enums.DecisionType.BAN -> stringResource(R.string.ban)
                    Enums.DecisionType.CAPTCHA -> stringResource(R.string.captcha)
                }
                DropdownMenuItem(
                    text = { Text(name) },
                    onClick = {
                        onTypeSelected(decisionType)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}
