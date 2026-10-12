package com.jgeek00.crowdsecmonitor.ui.screens.settings.notifications

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.jgeek00.crowdsecmonitor.R
import com.jgeek00.crowdsecmonitor.constants.NotificationDefaults
import com.jgeek00.crowdsecmonitor.ui.components.ListItemContent
import com.jgeek00.crowdsecmonitor.ui.components.RoundedCornersListTile
import com.jgeek00.crowdsecmonitor.ui.components.SectionHeader
import com.jgeek00.crowdsecmonitor.constants.Enums

import com.jgeek00.crowdsecmonitor.viewmodel.ConditionField
import com.jgeek00.crowdsecmonitor.viewmodel.ConditionOperator
import com.jgeek00.crowdsecmonitor.viewmodel.NotificationFormViewModel
import com.jgeek00.crowdsecmonitor.viewmodel.RulesValidationIssue

// MARK: - Localized vocabulary (shared with the rule editor and detail screens)

@Composable
fun fieldLabel(field: ConditionField): String = when (field) {
    ConditionField.SCENARIO -> stringResource(R.string.field_scenario)
    ConditionField.COUNTRY -> stringResource(R.string.field_country)
    ConditionField.TARGET -> stringResource(R.string.field_target)
    ConditionField.IP_OWNER -> stringResource(R.string.field_ip_owner)
}

@Composable
fun operatorPhrase(op: ConditionOperator): String = when (op) {
    ConditionOperator.EQUALS -> stringResource(R.string.op_is)
    ConditionOperator.NOT_EQUALS -> stringResource(R.string.op_is_not)
    ConditionOperator.IN -> stringResource(R.string.op_is_any_of)
    ConditionOperator.NOT_IN -> stringResource(R.string.op_is_none_of)
    ConditionOperator.CONTAINS -> stringResource(R.string.op_contains)
}

@Composable
fun valuesSummary(values: List<String>): String {
    val parts = ConditionFormatting.valuesSummaryParts(values)
    if (!parts.hasMore) return parts.head
    val more = stringResource(R.string.values_more_fmt, parts.moreCount)
    return parts.head + " " + more
}

@Composable
fun windowLabel(seconds: Int): String {
    val res = ConditionFormatting.windowLabelRes(seconds)
    return if (res != null) {
        stringResource(res)
    } else {
        pluralStringResource(R.plurals.notification_seconds, seconds, seconds)
    }
}

@Composable
fun cooldownLabel(seconds: Int): String {
    val res = ConditionFormatting.cooldownLabelRes(seconds)
    return if (res != null) {
        stringResource(res)
    } else {
        pluralStringResource(R.plurals.notification_seconds, seconds, seconds)
    }
}

@Composable
fun validationHint(issue: RulesValidationIssue): String = when (issue) {
    RulesValidationIssue.EMPTY -> stringResource(R.string.validation_empty)
    RulesValidationIssue.INCOMPLETE -> stringResource(R.string.validation_incomplete)
    RulesValidationIssue.ADVANCED -> stringResource(R.string.validation_advanced)
    RulesValidationIssue.INVALID_WINDOW -> stringResource(R.string.window_range_error)
}

// MARK: - Condition editor (wizard step 1)

/**
 * Full condition step of the notification wizard: info box plus the shared
 * condition/rule/frequency/cooldown sections.
 */
@Composable
fun ConditionEditor(
    viewModel: NotificationFormViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        FormInfoBox(label = stringResource(R.string.wizard_info_box))
        ConditionEditorSections(viewModel = viewModel)
        Spacer(modifier = Modifier.height(24.dp))
    }
}

/**
 * Condition, rules, frequency and cooldown sections shared by the wizard and
 * (later, A10) the notification detail edit form.
 */
@Composable
fun ConditionEditorSections(viewModel: NotificationFormViewModel) {
    ConditionSection(viewModel)
    if (viewModel.advancedCondition != null && !viewModel.noCondition) {
        AdvancedConditionSection(viewModel)
    }
    if (!viewModel.noCondition && viewModel.advancedCondition == null) {
        RulesSection(viewModel)
    }
    FrequencySection(viewModel)
    CooldownSection(viewModel)
}

@Composable
private fun ConditionSection(viewModel: NotificationFormViewModel) {
    SectionHeader(text = stringResource(R.string.condition), topPadding = Enums.SectionHeaderPaddingTop.SMALL)
    val totalRows = if (viewModel.noCondition) 2 else 1
    RoundedCornersListTile(index = 0, totalItems = totalRows, onClick = { viewModel.noCondition = !viewModel.noCondition }) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.any_alert),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge
            )
            Switch(
                checked = viewModel.noCondition,
                onCheckedChange = { viewModel.noCondition = it }
            )
        }
    }
    if (viewModel.noCondition) {
        RoundedCornersListTile(index = 1, totalItems = 2) {
            Text(
                text = stringResource(R.string.any_alert_explainer),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AdvancedConditionSection(viewModel: NotificationFormViewModel) {
    val warningIconSize = with(LocalDensity.current) {
        MaterialTheme.typography.titleMedium.fontSize.toDp()
    }
    Spacer(modifier = Modifier.height(12.dp))
    RoundedCornersListTile(index = 0, totalItems = 1) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.Warning,
                    contentDescription = null,
                    tint = Color(0xFFFF9800),
                    modifier = Modifier
                        .padding(end = 12.dp)
                        .height(warningIconSize)
                        .width(warningIconSize)
                )
                Text(
                    text = stringResource(R.string.advanced_condition_warning),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFFF9800)
                )
            }
            TextButton(
                onClick = { viewModel.replaceWithSimpleRules() }
            ) {
                Text(
                    text = stringResource(R.string.replace_with_simple_rules),
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun RulesSection(viewModel: NotificationFormViewModel) {
    SectionHeader(text = stringResource(R.string.rules), topPadding = Enums.SectionHeaderPaddingTop.SMALL)
    val rules = viewModel.rules
    if (rules.isEmpty()) {
        RoundedCornersListTile(index = 0, totalItems = 1) {
            Text(
                text = stringResource(R.string.no_rules_yet),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
    rules.forEachIndexed { index, rule ->
        RoundedCornersListTile(index = index, totalItems = rules.size) {
            ListItemContent(
                headlineText = stringResource(
                    R.string.rule_label_fmt,
                    fieldLabel(rule.field),
                    operatorPhrase(rule.op)
                ),
                subHeadlineText = valuesSummary(rule.values),
                trailingContent = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = { viewModel.openRuleSheet(draft = rule, index = index) }) {
                            Text(stringResource(R.string.edit))
                        }
                        IconButton(onClick = {
                            viewModel.rules = viewModel.rules.toMutableList().also { it.removeAt(index) }
                        }) {
                            Icon(
                                imageVector = Icons.Rounded.Delete,
                                contentDescription = stringResource(R.string.delete_rule)
                            )
                        }
                    }
                }
            )
        }
    }
    TextButton(onClick = { viewModel.openRuleSheet(draft = viewModel.newDraft(), index = null) }) {
        Icon(imageVector = Icons.Rounded.Add, contentDescription = null)
        Spacer(modifier = Modifier.width(4.dp))
        Text(stringResource(R.string.add_rule))
    }
    val issue = viewModel.validationIssue(1)
    if (issue != null) {
        Text(
            text = validationHint(issue),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(start = 16.dp, top = 4.dp)
        )
    }
}

@Composable
private fun FrequencySection(viewModel: NotificationFormViewModel) {
    SectionHeader(text = stringResource(R.string.frequency), topPadding = Enums.SectionHeaderPaddingTop.SMALL)
    RoundedCornersListTile(index = 0, totalItems = 2) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.count_fmt, viewModel.count),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge
            )
            OutlinedIconButton(
                onClick = { if (viewModel.count > 1) viewModel.count-- }
            ) {
                Icon(imageVector = Icons.Rounded.Remove, contentDescription = null)
            }
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedIconButton(
                onClick = { if (viewModel.count < 1000) viewModel.count++ }
            ) {
                Icon(imageVector = Icons.Rounded.Add, contentDescription = null)
            }
        }
    }
    RoundedCornersListTile(index = 1, totalItems = 2) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.time_window_seconds),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge
            )
            OutlinedTextField(
                value = viewModel.windowSecondsText,
                onValueChange = { viewModel.windowSecondsText = it.filter { ch -> ch.isDigit() } },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.width(120.dp)
            )
        }
    }
    if (viewModel.windowValid) {
        Text(
            text = stringResource(
                R.string.frequency_footer_fmt,
                viewModel.count,
                windowLabel(viewModel.parsedWindowSeconds)
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 16.dp, top = 4.dp)
        )
    } else {
        Text(
            text = stringResource(R.string.window_range_error),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(start = 16.dp, top = 4.dp)
        )
    }
}

@Composable
private fun CooldownSection(viewModel: NotificationFormViewModel) {
    SectionHeader(text = stringResource(R.string.cooldown), topPadding = Enums.SectionHeaderPaddingTop.SMALL)
    RoundedCornersListTile(index = 0, totalItems = 1) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.cooldown),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge
            )
            CooldownDropdown(
                selectedSeconds = viewModel.cooldownSeconds,
                onSelected = { viewModel.cooldownSeconds = it }
            )
        }
    }
    Text(
        text = stringResource(R.string.cooldown_footer),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
    )
}

@Composable
private fun CooldownDropdown(
    selectedSeconds: Int,
    onSelected: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = cooldownLabel(selectedSeconds),
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.cooldown)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .width(180.dp)
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            NotificationDefaults.COOLDOWN_OPTIONS.forEach { seconds ->
                DropdownMenuItem(
                    text = { Text(cooldownLabel(seconds)) },
                    onClick = {
                        onSelected(seconds)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}
