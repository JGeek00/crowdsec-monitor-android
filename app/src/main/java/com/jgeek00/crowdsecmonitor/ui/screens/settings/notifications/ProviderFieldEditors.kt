package com.jgeek00.crowdsecmonitor.ui.screens.settings.notifications

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.jgeek00.crowdsecmonitor.data.models.ProviderField
import com.jgeek00.crowdsecmonitor.data.models.ProviderFieldOption
import com.jgeek00.crowdsecmonitor.viewmodel.ChannelFormViewModel
import kotlinx.serialization.json.JsonPrimitive

/**
 * Dynamic provider field editors shared by the channel wizard (step 2) and the
 * channel details edit form: text, password (masked), number (numeric
 * keyboard), boolean (switch), select (dropdown), email/url with the right
 * keyboard and no autocorrect/capitalization. Unknown field types are skipped
 * safely.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderFieldEditor(viewModel: ChannelFormViewModel, field: ProviderField) {
    when (field.type) {
        "text" -> ProviderTextField(
            viewModel = viewModel,
            field = field,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.None,
                autoCorrectEnabled = false
            )
        )
        "password" -> ProviderTextField(
            viewModel = viewModel,
            field = field,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                capitalization = KeyboardCapitalization.None,
                autoCorrectEnabled = false
            )
        )
        "number" -> ProviderTextField(
            viewModel = viewModel,
            field = field,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                capitalization = KeyboardCapitalization.None,
                autoCorrectEnabled = false
            )
        )
        "boolean" -> ProviderBooleanField(viewModel = viewModel, field = field)
        "select" -> ProviderSelectField(viewModel = viewModel, field = field)
        "email" -> ProviderTextField(
            viewModel = viewModel,
            field = field,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                capitalization = KeyboardCapitalization.None,
                autoCorrectEnabled = false
            )
        )
        "url" -> ProviderTextField(
            viewModel = viewModel,
            field = field,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Uri,
                capitalization = KeyboardCapitalization.None,
                autoCorrectEnabled = false
            )
        )
    }
}

internal fun fieldTextValue(viewModel: ChannelFormViewModel, field: ProviderField): String =
    (viewModel.values[field.key] as? JsonPrimitive)?.content ?: ""

internal fun setTextValue(viewModel: ChannelFormViewModel, field: ProviderField, text: String) {
    viewModel.values = viewModel.values + (field.key to JsonPrimitive(text))
}

@Composable
internal fun ProviderTextField(
    viewModel: ChannelFormViewModel,
    field: ProviderField,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    OutlinedTextField(
        value = fieldTextValue(viewModel, field),
        onValueChange = { setTextValue(viewModel, field, it) },
        label = { Text(backendString(field.labelKey)) },
        placeholder = field.placeholderKey?.let { placeholderKey ->
            { Text(backendString(placeholderKey)) }
        },
        singleLine = true,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun ProviderBooleanField(viewModel: ChannelFormViewModel, field: ProviderField) {
    val checked = (viewModel.values[field.key] as? JsonPrimitive)?.content == "true"
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = backendString(field.labelKey),
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge
        )
        Switch(
            checked = checked,
            onCheckedChange = { isOn ->
                viewModel.values = viewModel.values + (field.key to JsonPrimitive(isOn))
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProviderSelectField(viewModel: ChannelFormViewModel, field: ProviderField) {
    var expanded by remember { mutableStateOf(false) }
    val current = fieldTextValue(viewModel, field)
    val options = field.options ?: emptyList()
    // Inject the current value as a temporary option when it is not among the
    // definition options, so the selection never renders blank.
    val effectiveOptions = if (current.isNotEmpty() && options.none { it.value == current }) {
        listOf(ProviderFieldOption(value = current, labelKey = "")) + options
    } else {
        options
    }
    val currentLabel = when {
        current.isEmpty() -> ""
        effectiveOptions.none { it.value == current } -> current
        else -> backendString(effectiveOptions.first { it.value == current }.labelKey)
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = currentLabel,
            onValueChange = { },
            readOnly = true,
            label = { Text(backendString(field.labelKey)) },
            placeholder = field.placeholderKey?.let { placeholderKey ->
                { Text(backendString(placeholderKey)) }
            },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            effectiveOptions.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            if (option.labelKey.isEmpty()) option.value
                            else backendString(option.labelKey)
                        )
                    },
                    onClick = {
                        setTextValue(viewModel, field, option.value)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}
