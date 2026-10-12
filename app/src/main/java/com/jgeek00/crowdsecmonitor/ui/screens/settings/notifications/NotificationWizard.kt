package com.jgeek00.crowdsecmonitor.ui.screens.settings.notifications

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Circle
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.isImeVisible
import com.jgeek00.crowdsecmonitor.R
import com.jgeek00.crowdsecmonitor.data.models.NotificationChannelType
import com.jgeek00.crowdsecmonitor.data.models.UserNotification
import com.jgeek00.crowdsecmonitor.data.models.UserNotificationChannel
import com.jgeek00.crowdsecmonitor.ui.components.FullScreenDialog
import com.jgeek00.crowdsecmonitor.ui.components.ListItemContent
import com.jgeek00.crowdsecmonitor.ui.components.RoundedCornersListTile
import com.jgeek00.crowdsecmonitor.ui.components.SectionHeader
import com.jgeek00.crowdsecmonitor.constants.Enums
import com.jgeek00.crowdsecmonitor.viewmodel.NotificationFormViewModel
import kotlinx.coroutines.launch

private const val WIZARD_STEPS = 5

private const val STEP_INFO = 0
private const val STEP_CONDITION = 1
private const val STEP_MESSAGE = 2
private const val STEP_CHANNELS = 3
private const val STEP_REVIEW = 4

/**
 * Notification wizard: 5 steps (Info, Condition, Message, Channels, Review) in a
 * non-swipeable pager — navigation happens ONLY through the footer buttons
 * (gotcha 1). Hosts the rule editor dialog once at the root (gotcha 2) and
 * handles save/save-error state. `onClose(true)` means the notification was
 * saved; the caller refreshes its list.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NotificationWizard(
    viewModel: NotificationFormViewModel,
    editing: UserNotification? = null,
    onClose: (saved: Boolean) -> Unit
) {
    val keyboard = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { WIZARD_STEPS })

    LaunchedEffect(Unit) {
        editing?.let { viewModel.load(it) }
        viewModel.loadOptions()
    }

    // Close the keyboard on every step change (gotcha 2 companion).
    LaunchedEffect(viewModel.selectedStep) {
        keyboard?.hide()
        pagerState.animateScrollToPage(viewModel.selectedStep)
    }

    // Keyboard staging for the rule editor presentation (gotcha 2).
    val imeVisible = WindowInsets.isImeVisible
    LaunchedEffect(imeVisible) {
        if (imeVisible) {
            viewModel.keyboardWillShow()
        } else {
            viewModel.keyboardDidHide()
        }
    }

    FullScreenDialog(
        onClose = { onClose(false) },
        title = stringResource(
            if (viewModel.isEditing) R.string.notification_wizard_edit_title
            else R.string.notification_wizard_new_title
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
                    STEP_INFO -> WizardInfoStep(viewModel)
                    STEP_CONDITION -> ConditionEditor(viewModel)
                    STEP_MESSAGE -> WizardMessageStep(viewModel)
                    STEP_CHANNELS -> WizardChannelsStep(viewModel)
                    STEP_REVIEW -> WizardReviewStep(viewModel)
                }
            }

            WizardFooter(
                viewModel = viewModel,
                onBack = {
                    keyboard?.hide()
                    viewModel.selectedStep -= 1
                },
                onNext = {
                    keyboard?.hide()
                    viewModel.selectedStep += 1
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
            message = stringResource(R.string.error_save_notification),
            onDismiss = { viewModel.saveError = false }
        )
    }
    if (viewModel.loadError) {
        WizardErrorAlert(
            message = stringResource(R.string.error_load_filter_options),
            onDismiss = { viewModel.loadError = false }
        )
    }

    // Single atomic rule editor dialog, driven by vm.ruleSheet (gotcha 2).
    RuleEditDialog(viewModel = viewModel)
}

@Composable
private fun WizardFooter(
    viewModel: NotificationFormViewModel,
    onBack: () -> Unit,
    onNext: () -> Unit,
    onFinish: () -> Unit
) {
    val step = viewModel.selectedStep
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
                    enabled = viewModel.canProceed(step) && !viewModel.isSaving
                ) {
                    Text(stringResource(R.string.next))
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = null
                    )
                }
            } else {
                Button(onClick = onFinish, enabled = viewModel.canProceed(STEP_CHANNELS) && !viewModel.isSaving) {
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

// MARK: - Step 0: Info

@Composable
private fun WizardInfoStep(viewModel: NotificationFormViewModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        FormInfoBox(label = stringResource(R.string.wizard_info_box))
        SectionHeader(text = stringResource(R.string.details), topPadding = Enums.SectionHeaderPaddingTop.SMALL)
        RoundedCornersListTile(index = 0, totalItems = 2) {
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
        RoundedCornersListTile(index = 1, totalItems = 2) {
            OutlinedTextField(
                value = viewModel.notificationDescription,
                onValueChange = { viewModel.notificationDescription = it },
                label = { Text(stringResource(R.string.description_optional)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

// MARK: - Step 2: Message

@Composable
private fun WizardMessageStep(viewModel: NotificationFormViewModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        FormInfoBox(label = stringResource(R.string.message_step_info))
        SectionHeader(text = stringResource(R.string.content), topPadding = Enums.SectionHeaderPaddingTop.SMALL)
        RoundedCornersListTile(index = 0, totalItems = 1) {
            OutlinedTextField(
                value = viewModel.message,
                onValueChange = { viewModel.message = it },
                label = { Text(stringResource(R.string.message)) },
                minLines = 2,
                maxLines = 8,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
        NotificationAlertInfoSections(viewModel = viewModel)
        Spacer(modifier = Modifier.height(24.dp))
    }
}

// MARK: - Alert-info sections (shared with the detail edit form, A10)

/**
 * Opt-in alert-info toggle with explanatory footer, plus a collapsed-by-default
 * accordion listing the configured channels sorted supporting-first with
 * check/cross per `supportsAlertInfo` (gotcha 12: delivery is decided
 * per-channel by the backend).
 */
@Composable
fun NotificationAlertInfoSections(viewModel: NotificationFormViewModel) {
    var showSupportingChannels by rememberSaveable { mutableStateOf(false) }

    RoundedCornersListTile(index = 0, totalItems = 1) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.include_alert_info),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyLarge
                )
                Switch(
                    checked = viewModel.includeAlertInfo,
                    onCheckedChange = { viewModel.includeAlertInfo = it }
                )
            }
            Text(
                text = stringResource(R.string.include_alert_info_footer),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
    if (viewModel.channels.isNotEmpty()) {
        RoundedCornersListTile(index = 0, totalItems = 1) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.which_channels_support),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    IconButton(onClick = { showSupportingChannels = !showSupportingChannels }) {
                        Icon(
                            imageVector = if (showSupportingChannels) {
                                Icons.Rounded.KeyboardArrowUp
                            } else {
                                Icons.Rounded.KeyboardArrowDown
                            },
                            contentDescription = null
                        )
                    }
                }
                if (showSupportingChannels) {
                    channelsBySupport(viewModel.channels).forEach { channel ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (channel.supportsAlertInfo) Icons.Rounded.Check else Icons.Rounded.Close,
                                contentDescription = null,
                                tint = if (channel.supportsAlertInfo) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.error
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = channel.name,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun channelsBySupport(channels: List<UserNotificationChannel>) =
    channels.sortedWith(
        compareByDescending<UserNotificationChannel> { it.supportsAlertInfo }
            .thenBy { it.id }
    )

// MARK: - Step 3: Channels

@Composable
private fun WizardChannelsStep(viewModel: NotificationFormViewModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        FormInfoBox(label = stringResource(R.string.channels_step_info))
        SectionHeader(text = stringResource(R.string.channels), topPadding = Enums.SectionHeaderPaddingTop.SMALL)
        if (viewModel.channels.isEmpty()) {
            RoundedCornersListTile(index = 0, totalItems = 1) {
                Text(
                    text = stringResource(R.string.no_channels_configured),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            val channels = viewModel.channels
            channels.forEachIndexed { index, channel ->
                val selected = viewModel.selectedChannelIds.contains(channel.id)
                RoundedCornersListTile(index = index, totalItems = channels.size, onClick = {
                    viewModel.selectedChannelIds = if (selected) {
                        viewModel.selectedChannelIds - channel.id
                    } else {
                        viewModel.selectedChannelIds + channel.id
                    }
                }) {
                    ListItemContent(
                        headlineText = channel.name,
                        subHeadlineText = stringResource(
                            if (channel.type == NotificationChannelType.EMAIL) {
                                R.string.channel_type_email
                            } else {
                                R.string.channel_type_ntfy
                            }
                        ),
                        trailingContent = {
                            Icon(
                                imageVector = if (selected) Icons.Rounded.CheckCircle else Icons.Rounded.Circle,
                                contentDescription = null,
                                tint = if (selected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

// MARK: - Step 4: Review

@Composable
private fun WizardReviewStep(viewModel: NotificationFormViewModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        FormInfoBox(label = stringResource(R.string.review_step_info))
        SectionHeader(text = stringResource(R.string.summary), topPadding = Enums.SectionHeaderPaddingTop.SMALL)

        val channelNames = viewModel.channels
            .filter { viewModel.selectedChannelIds.contains(it.id) }
            .joinToString(", ") { it.name }

        SummaryRow(label = stringResource(R.string.name), value = viewModel.name)
        if (viewModel.notificationDescription.isNotEmpty()) {
            SummaryRow(label = stringResource(R.string.description), value = viewModel.notificationDescription)
        }
        SummaryRow(
            label = stringResource(R.string.condition_blocks),
            value = viewModel.rules.size.toString()
        )
        SummaryRow(
            label = stringResource(R.string.frequency),
            value = frequencyLabel(viewModel.count, viewModel.parsedWindowSeconds)
        )
        SummaryRow(label = stringResource(R.string.message), value = viewModel.message)
        SummaryRow(
            label = stringResource(R.string.channels),
            value = channelNames.ifEmpty { stringResource(R.string.review_none) }
        )
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    RoundedCornersListTile(index = 0, totalItems = 1) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1.4f),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Pluralized frequency label: "%d time(s) in %d second(s)", with human window
 * labels for common windows (e.g. "1 minute").
 */
@Composable
fun frequencyLabel(count: Int, windowSeconds: Int): String {
    val times = pluralStringResource(R.plurals.notification_times, count, count)
    val window = windowLabel(windowSeconds)
    return stringResource(R.string.frequency_fmt, times, window)
}
