package com.jgeek00.crowdsecmonitor.ui.screens.settings.notifications

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.jgeek00.crowdsecmonitor.R
import com.jgeek00.crowdsecmonitor.ui.components.FullScreenDialog
import com.jgeek00.crowdsecmonitor.ui.components.RoundedCornersListTile
import com.jgeek00.crowdsecmonitor.ui.components.SectionHeader
import com.jgeek00.crowdsecmonitor.constants.Enums
import com.jgeek00.crowdsecmonitor.viewmodel.ConditionField
import com.jgeek00.crowdsecmonitor.viewmodel.ConditionOperator
import com.jgeek00.crowdsecmonitor.viewmodel.EditableLeaf
import com.jgeek00.crowdsecmonitor.viewmodel.NotificationFormViewModel
import com.jgeek00.crowdsecmonitor.viewmodel.RuleSheetModel

/**
 * Atomic rule editor dialog, hosted ONCE at the wizard root and driven entirely
 * by `vm.ruleSheet` (gotcha 2 — never per-row, keyboard staged by the VM).
 *
 * Value editing applies the `syncSingleValue` guard (gotcha 3): single-value
 * operators only reassign `values` when the computed array differs, a field
 * change clears custom values, and switching to a multi-value operator clears
 * values.
 */
@Composable
fun RuleEditDialog(
    viewModel: NotificationFormViewModel
) {
    val sheet = viewModel.ruleSheet ?: return

    var draft by remember(sheet.id) { mutableStateOf(sheet.draft) }
    val initialOptions = availableOptions(sheet.draft.field, viewModel)
    val firstValue = sheet.draft.values.firstOrNull() ?: ""
    var selectedOption by remember(sheet.id) {
        mutableStateOf(if (firstValue.isNotEmpty() && initialOptions.contains(firstValue)) firstValue else initialOptions.firstOrNull() ?: "")
    }
    var customValue by remember(sheet.id) {
        mutableStateOf(if (firstValue.isNotEmpty() && !initialOptions.contains(firstValue)) firstValue else "")
    }
    var customMultiValue by remember(sheet.id) { mutableStateOf("") }

    val currentOptions = availableOptions(draft.field, viewModel)

    fun syncSingleValue() {
        if (draft.op.needsMultipleValues || draft.op == ConditionOperator.CONTAINS) return
        val trimmed = customValue.trim()
        val value = trimmed.ifEmpty { selectedOption }
        val next = if (value.isEmpty()) emptyList() else listOf(value)
        if (draft.values != next) {
            draft = draft.copy(values = next)
        }
    }

    fun onFieldChanged(field: ConditionField) {
        draft = draft.copy(field = field)
        customValue = ""
        customMultiValue = ""
        selectedOption = availableOptions(field, viewModel).firstOrNull() ?: ""
        if (draft.op.needsMultipleValues || draft.op == ConditionOperator.CONTAINS) {
            draft = draft.copy(values = emptyList())
        } else {
            syncSingleValue()
        }
    }

    fun onOperatorChanged(op: ConditionOperator) {
        draft = draft.copy(op = op)
        if (!op.needsMultipleValues && op != ConditionOperator.CONTAINS) {
            if (selectedOption.isEmpty() || !currentOptions.contains(selectedOption)) {
                selectedOption = draft.values.firstOrNull { currentOptions.contains(it) }
                    ?: currentOptions.firstOrNull()
                    ?: ""
            }
        }
        syncSingleValue()
    }

    FullScreenDialog(
        onClose = { viewModel.dismissRuleSheet() },
        title = stringResource(R.string.rule_title),
        actions = {
            TextButton(
                onClick = { viewModel.commitRuleSheet(draft) },
                enabled = draft.isValid
            ) {
                Text(stringResource(R.string.save))
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            SectionHeader(
                text = stringResource(R.string.rule_section_field),
                topPadding = Enums.SectionHeaderPaddingTop.SMALL
            )
            RoundedCornersListTile(index = 0, totalItems = 1) {
                FieldDropdown(
                    label = stringResource(R.string.field_label),
                    selected = draft.field,
                    options = ConditionField.entries.toList(),
                    labelFor = { fieldLabel(it) },
                    onSelected = { onFieldChanged(it) }
                )
            }

            SectionHeader(text = stringResource(R.string.rule_section_operator))
            RoundedCornersListTile(index = 0, totalItems = 1) {
                FieldDropdown(
                    label = stringResource(R.string.operator_label),
                    selected = draft.op,
                    options = ConditionOperator.entries.toList(),
                    labelFor = { operatorPhrase(it) },
                    onSelected = { onOperatorChanged(it) }
                )
            }

            SectionHeader(text = stringResource(R.string.rule_section_values))
            RuleValueEditor(
                draft = draft,
                options = currentOptions,
                onContainsValueChanged = { text ->
                    draft = if (text.isEmpty()) {
                        draft.copy(values = emptyList())
                    } else {
                        draft.copy(values = listOf(text))
                    }
                },
                selectedOption = selectedOption,
                onSelectedOptionChanged = {
                    selectedOption = it
                    syncSingleValue()
                },
                customValue = customValue,
                onCustomValueChanged = {
                    customValue = it
                    syncSingleValue()
                },
                customMultiValue = customMultiValue,
                onCustomMultiValueChanged = { customMultiValue = it },
                onToggleOption = { option ->
                    draft = if (draft.values.contains(option)) {
                        draft.copy(values = draft.values - option)
                    } else {
                        draft.copy(values = draft.values + option)
                    }
                },
                onRemoveValue = { value ->
                    draft = draft.copy(values = draft.values - value)
                },
                onAddCustomMultiValue = {
                    val trimmed = customMultiValue.trim()
                    if (trimmed.isNotEmpty() && !draft.values.contains(trimmed)) {
                        draft = draft.copy(values = draft.values + trimmed)
                    }
                    customMultiValue = ""
                }
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

private fun availableOptions(field: ConditionField, viewModel: NotificationFormViewModel): List<String> {
    return viewModel.options(field)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> FieldDropdown(
    label: String,
    selected: T,
    options: List<T>,
    labelFor: @Composable (T) -> String,
    onSelected: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = labelFor(selected),
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(labelFor(option)) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}

@Composable
private fun RuleValueEditor(
    draft: EditableLeaf,
    options: List<String>,
    onContainsValueChanged: (String) -> Unit,
    selectedOption: String,
    onSelectedOptionChanged: (String) -> Unit,
    customValue: String,
    onCustomValueChanged: (String) -> Unit,
    customMultiValue: String,
    onCustomMultiValueChanged: (String) -> Unit,
    onToggleOption: (String) -> Unit,
    onRemoveValue: (String) -> Unit,
    onAddCustomMultiValue: () -> Unit
) {
    when {
        draft.op.needsMultipleValues -> MultiValueEditor(
            draft = draft,
            options = options,
            customMultiValue = customMultiValue,
            onCustomMultiValueChanged = onCustomMultiValueChanged,
            onToggleOption = onToggleOption,
            onRemoveValue = onRemoveValue,
            onAddCustomMultiValue = onAddCustomMultiValue
        )

        draft.op == ConditionOperator.CONTAINS -> OutlinedTextField(
            value = draft.values.firstOrNull() ?: "",
            onValueChange = onContainsValueChanged,
            label = { Text(stringResource(R.string.rule_value_text)) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )

        else -> SingleValueEditor(
            options = options,
            selectedOption = selectedOption,
            onSelectedOptionChanged = onSelectedOptionChanged,
            customValue = customValue,
            onCustomValueChanged = onCustomValueChanged
        )
    }
}

@Composable
private fun MultiValueEditor(
    draft: EditableLeaf,
    options: List<String>,
    customMultiValue: String,
    onCustomMultiValueChanged: (String) -> Unit,
    onToggleOption: (String) -> Unit,
    onRemoveValue: (String) -> Unit,
    onAddCustomMultiValue: () -> Unit
) {
    if (options.isEmpty()) {
        Text(
            text = stringResource(R.string.no_options_available),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    } else {
        RoundedCornersListTile(index = 0, totalItems = 1) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                options.forEach { option ->
                    val isSelected = draft.values.contains(option)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onToggleOption(option) }
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = option,
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = 12.dp)
                        )
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
    val customValues = draft.values.filter { !options.contains(it) }
    if (customValues.isNotEmpty()) {
        RoundedCornersListTile(index = 0, totalItems = 1) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                customValues.forEach { value ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = value,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        IconButton(onClick = { onRemoveValue(value) }) {
                            Icon(
                                imageVector = Icons.Rounded.Delete,
                                contentDescription = stringResource(R.string.remove_value)
                            )
                        }
                    }
                }
            }
        }
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = customMultiValue,
            onValueChange = onCustomMultiValueChanged,
            label = { Text(stringResource(R.string.custom_value)) },
            singleLine = true,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        TextButton(
            onClick = onAddCustomMultiValue,
            enabled = customMultiValue.trim().isNotEmpty()
        ) {
            Text(stringResource(R.string.add))
        }
    }
}

@Composable
private fun SingleValueEditor(
    options: List<String>,
    selectedOption: String,
    onSelectedOptionChanged: (String) -> Unit,
    customValue: String,
    onCustomValueChanged: (String) -> Unit
) {
    Column {
        if (options.isEmpty()) {
            Text(
                text = stringResource(R.string.no_options_custom_hint),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            RoundedCornersListTile(index = 0, totalItems = 1) {
                ValueDropdown(
                    label = stringResource(R.string.value_label),
                    selectedOption = selectedOption,
                    options = options,
                    onSelected = onSelectedOptionChanged
                )
            }
        }
        OutlinedTextField(
            value = customValue,
            onValueChange = onCustomValueChanged,
            label = { Text(stringResource(R.string.or_type_custom)) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ValueDropdown(
    label: String,
    selectedOption: String,
    options: List<String>,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selectedOption,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}
