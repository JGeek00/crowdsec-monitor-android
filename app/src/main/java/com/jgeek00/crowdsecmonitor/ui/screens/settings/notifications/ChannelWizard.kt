package com.jgeek00.crowdsecmonitor.ui.screens.settings.notifications

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.jgeek00.crowdsecmonitor.R
import com.jgeek00.crowdsecmonitor.constants.Enums
import com.jgeek00.crowdsecmonitor.data.models.NotificationProvider
import com.jgeek00.crowdsecmonitor.data.models.ProviderField
import com.jgeek00.crowdsecmonitor.data.models.ProviderFieldOption
import com.jgeek00.crowdsecmonitor.ui.components.FullScreenDialog
import com.jgeek00.crowdsecmonitor.ui.components.RoundedCornersListTile
import com.jgeek00.crowdsecmonitor.ui.components.SectionHeader
import com.jgeek00.crowdsecmonitor.viewmodel.ChannelFormViewModel
import com.jgeek00.crowdsecmonitor.viewmodel.ChannelTestState
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonPrimitive

private const val WIZARD_STEPS = 4

private const val STEP_INTRO = 0
private const val STEP_PROVIDER = 1
private const val STEP_FORM = 2
private const val STEP_FINAL = 3

/**
 * Channel wizard: 4 steps (Intro, Provider grid, Dynamic form, Name + Test) in a
 * non-swipeable pager — navigation happens ONLY through the footer buttons
 * (gotcha 1), reusing the NotificationWizard shell pattern. The view model is
 * identity-fresh per presentation (created by the caller), and `onClose(true)`
 * means the channel was saved so the caller refreshes its list.
 */
@Composable
fun ChannelWizard(
    viewModel: ChannelFormViewModel,
    onClose: (saved: Boolean) -> Unit
) {
    val keyboard = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { WIZARD_STEPS })

    // The Android ChannelFormViewModel does not hold step state (unlike its iOS
    // counterpart); the wizard composable owns it, surviving configuration
    // changes like the iOS @State.
    var selectedStep by rememberSaveable { mutableIntStateOf(STEP_INTRO) }

    LaunchedEffect(Unit) {
        viewModel.loadProviders()
    }

    // Close the keyboard on every step change (gotcha 2 companion).
    LaunchedEffect(selectedStep) {
        keyboard?.hide()
        pagerState.animateScrollToPage(selectedStep)
    }

    FullScreenDialog(
        onClose = { onClose(false) },
        title = stringResource(
            if (viewModel.isEditing) R.string.channel_wizard_edit_title
            else R.string.channel_wizard_new_title
        ),
        dismissConfirmation = true,
        allowClose = !viewModel.isSaving,
        actions = { }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize()) {
            HorizontalPager(
                state = pagerState,
                userScrollEnabled = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) { page ->
                when (page) {
                    STEP_INTRO -> ChannelWizardIntroStep()
                    STEP_PROVIDER -> ChannelWizardProviderStep(viewModel)
                    STEP_FORM -> ChannelWizardFormStep(viewModel)
                    STEP_FINAL -> ChannelWizardFinalStep(viewModel)
                }
            }

            ChannelWizardFooter(
                viewModel = viewModel,
                selectedStep = selectedStep,
                onBack = {
                    keyboard?.hide()
                    selectedStep -= 1
                },
                onNext = {
                    keyboard?.hide()
                    selectedStep += 1
                },
                onFinish = {
                    keyboard?.hide()
                    scope.launch {
                        val saved = viewModel.save()
                        if (saved != null) {
                            onClose(true)
                        }
                    }
                }
            )
        }
    }

    if (viewModel.saveError) {
        WizardErrorAlert(
            message = stringResource(R.string.error_save_channel),
            onDismiss = { viewModel.saveError = false }
        )
    }
    if (viewModel.loadError) {
        WizardErrorAlert(
            message = stringResource(R.string.error_load_providers),
            onDismiss = { viewModel.loadError = false }
        )
    }
}

@Composable
private fun ChannelWizardFooter(
    viewModel: ChannelFormViewModel,
    selectedStep: Int,
    onBack: () -> Unit,
    onNext: () -> Unit,
    onFinish: () -> Unit
) {
    val step = selectedStep
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            if (step > 0) {
                TextButton(onClick = onBack, enabled = !viewModel.isSaving) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.back))
                }
            }
        }
        WizardStepIndicator(
            totalSteps = WIZARD_STEPS,
            currentStep = step,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
            if (step < WIZARD_STEPS - 1) {
                Button(
                    onClick = onNext,
                    enabled = canProceed(viewModel, step) && !viewModel.isSaving
                ) {
                    Text(stringResource(R.string.next))
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = null
                    )
                }
            } else {
                Button(
                    onClick = onFinish,
                    enabled = viewModel.nameValid() && viewModel.providerValid() && !viewModel.isSaving
                ) {
                    if (viewModel.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .height(24.dp)
                                .padding(4.dp),
                            strokeWidth = 2.5.dp
                        )
                    } else {
                        Icon(imageVector = Icons.Rounded.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.finish))
                    }
                }
            }
        }
    }
}

private fun canProceed(viewModel: ChannelFormViewModel, step: Int): Boolean = when (step) {
    STEP_PROVIDER -> viewModel.providerType != null
    STEP_FORM -> viewModel.providerValid()
    else -> true
}


@Composable
private fun WizardErrorAlert(message: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.error)) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.ok))
            }
        }
    )
}

// MARK: - Step 0: Intro

@Composable
private fun ChannelWizardIntroStep() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        FormInfoBox(label = stringResource(R.string.channel_wizard_info_box))
        Spacer(modifier = Modifier.height(24.dp))
    }
}

// MARK: - Step 1: Provider grid

@Composable
private fun ChannelWizardProviderStep(viewModel: ChannelFormViewModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        if (viewModel.providers.isEmpty()) {
            FormInfoBox(label = stringResource(R.string.no_providers_available))
        } else {
            SectionHeader(
                text = stringResource(R.string.choose_provider_section),
                topPadding = Enums.SectionHeaderPaddingTop.SMALL
            )
            // FIXED 2-column rows built explicitly (gotcha 4): no lazy grid
            // inside this scrollable form content.
            viewModel.providers.chunked(2).forEachIndexed { rowIndex, row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = if (rowIndex == 0) 8.dp else 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    row.forEach { provider ->
                        ProviderCard(
                            provider = provider,
                            isSelected = viewModel.providerType == provider.type,
                            onSelect = { viewModel.selectProvider(provider.type) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (row.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun ProviderCard(
    provider: NotificationProvider,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outlineVariant
    }
    Column(
        modifier = modifier
            .background(
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                } else {
                    MaterialTheme.colorScheme.surfaceContainerHighest
                },
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                border = BorderStroke(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = borderColor
                ),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClickLabel = backendString(provider.labelKey), onClick = onSelect)
            .semantics {
                role = Role.RadioButton
                this.selected = isSelected
            }
            .padding(vertical = 16.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = channelIconVector(provider.icon),
            contentDescription = null,
            modifier = Modifier.size(44.dp),
            tint = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        )
        Text(
            text = backendString(provider.labelKey),
            style = MaterialTheme.typography.titleSmall,
            color = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            maxLines = 1
        )
    }
}

// MARK: - Step 2: Dynamic provider form

@Composable
private fun ChannelWizardFormStep(viewModel: ChannelFormViewModel) {
    val definition = viewModel.definition
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        if (definition == null) {
            FormInfoBox(label = stringResource(R.string.provider_form_no_provider))
        } else {
            definition.descriptionKey?.let { descriptionKey ->
                FormInfoBox(label = backendString(descriptionKey))
            }
            viewModel.groupedFields().forEach { (section, fields) ->
                if (section != null) {
                    SectionHeader(
                        text = backendString(section.labelKey),
                        topPadding = Enums.SectionHeaderPaddingTop.SMALL
                    )
                }
                fields.forEachIndexed { index, field ->
                    RoundedCornersListTile(index = index, totalItems = fields.size) {
                        ProviderFieldEditor(viewModel = viewModel, field = field)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

/**
 * Field editors mirroring the iOS ChannelWizardFormStep: text, password
 * (masked), number (numeric keyboard), boolean (switch), select (dropdown with
 * the current value injected as a temporary option when it is not among the
 * definition options — gotcha 11), email/url with the right keyboard and no
 * autocorrect/capitalization. Unknown field types are skipped safely.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProviderFieldEditor(viewModel: ChannelFormViewModel, field: ProviderField) {
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
        // Unknown field types are ignored with safe defaults, like on iOS.
    }
}

private fun fieldTextValue(viewModel: ChannelFormViewModel, field: ProviderField): String =
    (viewModel.values[field.key] as? JsonPrimitive)?.content ?: ""

private fun setTextValue(viewModel: ChannelFormViewModel, field: ProviderField, text: String) {
    viewModel.values = viewModel.values + (field.key to JsonPrimitive(text))
}

@Composable
private fun ProviderTextField(
    viewModel: ChannelFormViewModel,
    field: ProviderField,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: androidx.compose.ui.text.input.VisualTransformation =
        androidx.compose.ui.text.input.VisualTransformation.None
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
private fun ProviderBooleanField(viewModel: ChannelFormViewModel, field: ProviderField) {
    val checked = (viewModel.values[field.key] as? JsonPrimitive)?.content == "true"
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
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
    // Gotcha 11: inject the current value as a temporary option when it is not
    // among the definition options, so the selection never renders blank.
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
                .padding(horizontal = 16.dp, vertical = 8.dp)
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

// MARK: - Step 3: Name + Test

@Composable
private fun ChannelWizardFinalStep(viewModel: ChannelFormViewModel) {
    val scope = rememberCoroutineScope()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        FormInfoBox(label = stringResource(R.string.channel_final_step_info))
        SectionHeader(
            text = stringResource(R.string.name),
            topPadding = Enums.SectionHeaderPaddingTop.SMALL
        )
        RoundedCornersListTile(index = 0, totalItems = 1) {
            OutlinedTextField(
                value = viewModel.name,
                onValueChange = { viewModel.name = it },
                label = { Text(stringResource(R.string.name)) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
        SectionHeader(
            text = stringResource(R.string.test),
            topPadding = Enums.SectionHeaderPaddingTop.SMALL
        )
        RoundedCornersListTile(index = 0, totalItems = 1) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Button(
                    onClick = { scope.launch { viewModel.test() } },
                    enabled = viewModel.testState != ChannelTestState.Testing &&
                        viewModel.providerValid(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.send_test_notification))
                    Spacer(modifier = Modifier.weight(1f))
                    if (viewModel.testState == ChannelTestState.Testing) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .height(24.dp)
                                .padding(4.dp),
                            strokeWidth = 2.5.dp
                        )
                    }
                }
                when (val state = viewModel.testState) {
                    ChannelTestState.Idle, ChannelTestState.Testing -> Unit
                    ChannelTestState.Success -> TestFeedbackRow(
                        icon = Icons.Rounded.CheckCircle,
                        tint = Color.Green,
                        text = stringResource(R.string.test_notification_sent)
                    )
                    is ChannelTestState.Failure -> TestFeedbackRow(
                        icon = Icons.Rounded.Cancel,
                        tint = MaterialTheme.colorScheme.error,
                        text = stringResource(R.string.test_failed_fmt, state.detail)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun TestFeedbackRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    text: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = tint
        )
    }
}
