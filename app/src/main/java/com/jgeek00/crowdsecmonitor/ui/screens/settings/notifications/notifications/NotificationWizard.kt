package com.jgeek00.crowdsecmonitor.ui.screens.settings.notifications.notifications

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
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.jgeek00.crowdsecmonitor.R
import com.jgeek00.crowdsecmonitor.data.models.UserNotification
import com.jgeek00.crowdsecmonitor.data.models.UserNotificationChannel
import com.jgeek00.crowdsecmonitor.ui.components.FullScreenDialog
import com.jgeek00.crowdsecmonitor.ui.components.ListItemContent
import com.jgeek00.crowdsecmonitor.ui.components.RoundedCornersListTile
import com.jgeek00.crowdsecmonitor.ui.components.SectionHeader
import com.jgeek00.crowdsecmonitor.ui.screens.settings.notifications.generic.FormInfoBox
import com.jgeek00.crowdsecmonitor.ui.screens.settings.notifications.generic.RuleEditorKeyboardStaging
import com.jgeek00.crowdsecmonitor.ui.screens.settings.notifications.generic.WizardStepIndicator
import com.jgeek00.crowdsecmonitor.viewmodel.NotificationFormViewModel
import kotlinx.coroutines.launch

private const val WIZARD_STEPS = 5

private const val STEP_INFO = 0
private const val STEP_CONDITION = 1
private const val STEP_MESSAGE = 2
private const val STEP_CHANNELS = 3
private const val STEP_REVIEW = 4

/**
 * Notification wizard: 5 steps (Info, Condition, Message, Channels, Review) in
 * a non-swipeable pager — navigation happens only through the footer buttons.
 * `onClose(true)` means the notification was saved; the caller refreshes its
 * list.
 */
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

    // Close the keyboard on every step change.
    LaunchedEffect(viewModel.selectedStep) {
        keyboard?.hide()
        pagerState.animateScrollToPage(viewModel.selectedStep)
    }

    // Keyboard staging for the rule editor presentation.
    RuleEditorKeyboardStaging(viewModel = viewModel)

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
        // Apply the dialog Scaffold's inner padding so the pager content
        // clears the top app bar and the footer clears the navigation bars.
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            HorizontalPager(
                state = pagerState,
                userScrollEnabled = false,
                // Pager centers wrapped-height pages by default.
                verticalAlignment = Alignment.Top,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) { page ->
                when (page) {
                    STEP_INFO -> WizardInfoStep(viewModel)
                    STEP_CONDITION -> ConditionEditor(viewModel)
                    STEP_MESSAGE -> WizardMessageStep(viewModel)
                    STEP_CHANNELS -> Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp)
                    ) {
                        ChannelsSelectorSection(viewModel)
                    }
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

    // Single atomic rule editor dialog, driven by vm.ruleSheet.
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

@Composable
private fun WizardInfoStep(viewModel: NotificationFormViewModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        FormInfoBox(label = stringResource(R.string.wizard_info_box))
        SectionHeader(text = stringResource(R.string.details))
        OutlinedTextField(
            value = viewModel.name,
            onValueChange = { viewModel.name = it },
            label = { Text(stringResource(R.string.name)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedTextField(
            value = viewModel.notificationDescription,
            onValueChange = { viewModel.notificationDescription = it },
            label = { Text(stringResource(R.string.description_optional)) },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun WizardMessageStep(viewModel: NotificationFormViewModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        FormInfoBox(label = stringResource(R.string.message_step_info))
        SectionHeader(text = stringResource(R.string.content))
        OutlinedTextField(
            value = viewModel.message,
            onValueChange = { viewModel.message = it },
            label = { Text(stringResource(R.string.message)) },
            minLines = 2,
            maxLines = 8,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))
        NotificationAlertInfoSections(viewModel = viewModel)
        Spacer(modifier = Modifier.height(24.dp))
    }
}

/**
 * Opt-in alert-info toggle with explanatory footer, plus a collapsed-by-default
 * accordion listing the configured channels sorted supporting-first with
 * check/cross per `supportsAlertInfo` (delivery is decided per-channel by the
 * backend).
 */
@Composable
fun NotificationAlertInfoSections(viewModel: NotificationFormViewModel) {
    var showSupportingChannels by rememberSaveable { mutableStateOf(false) }

    RoundedCornersListTile(index = 0, totalItems = 1) {
        Column(modifier = Modifier.fillMaxWidth()) {
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
        Spacer(modifier = Modifier.height(24.dp))
        Column(modifier = Modifier.fillMaxWidth()) {
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

private fun channelsBySupport(channels: List<UserNotificationChannel>) =
    channels.sortedWith(
        compareByDescending<UserNotificationChannel> { it.supportsAlertInfo }
            .thenBy { it.id }
    )

@Composable
private fun WizardReviewStep(viewModel: NotificationFormViewModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        FormInfoBox(label = stringResource(R.string.review_step_info))
        SectionHeader(text = stringResource(R.string.summary))

        val channelNames = viewModel.channels
            .filter { viewModel.selectedChannelIds.contains(it.id) }
            .joinToString(", ") { it.name }

        val summaryRows = buildList {
            add(stringResource(R.string.name) to viewModel.name)
            if (viewModel.notificationDescription.isNotEmpty()) {
                add(stringResource(R.string.description) to viewModel.notificationDescription)
            }
            add(stringResource(R.string.condition_blocks) to viewModel.rules.size.toString())
            add(
                stringResource(R.string.frequency) to
                    frequencyLabel(viewModel.count, viewModel.parsedWindowSeconds)
            )
            add(stringResource(R.string.message) to viewModel.message)
            add(
                stringResource(R.string.channels) to
                    channelNames.ifEmpty { stringResource(R.string.review_none) }
            )
        }
        summaryRows.forEachIndexed { index, (label, value) ->
            SummaryRow(label = label, value = value, index = index, totalItems = summaryRows.size)
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun SummaryRow(label: String, value: String, index: Int, totalItems: Int) {
    RoundedCornersListTile(index = index, totalItems = totalItems) {
        ListItemContent(headlineText = label, subHeadlineText = value)
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
