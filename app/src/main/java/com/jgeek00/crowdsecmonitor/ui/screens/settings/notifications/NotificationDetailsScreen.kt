package com.jgeek00.crowdsecmonitor.ui.screens.settings.notifications

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jgeek00.crowdsecmonitor.R
import com.jgeek00.crowdsecmonitor.constants.Enums
import com.jgeek00.crowdsecmonitor.constants.NotificationDefaults
import com.jgeek00.crowdsecmonitor.data.models.LoadingResult
import com.jgeek00.crowdsecmonitor.data.models.UserNotification
import com.jgeek00.crowdsecmonitor.ui.components.ListItemContent
import com.jgeek00.crowdsecmonitor.ui.components.RoundedCornersListTile
import com.jgeek00.crowdsecmonitor.ui.components.SectionHeader
import com.jgeek00.crowdsecmonitor.viewmodel.NotificationDetailsViewModel
import com.jgeek00.crowdsecmonitor.viewmodel.NotificationFormViewModel
import kotlinx.coroutines.launch

/**
 * Notification details: read-only summary with an in-place edit mode. The top
 * bar switches from a pencil to separate Cancel/Save actions while editing;
 * `isSaving` blocks input without any modal blocker. Delete stays out of edit
 * mode — deletion lives in the list menus. The atomic rule editor dialog is
 * hosted once at the screen root with the same keyboard staging as the wizard.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationDetailsScreen(
    notificationId: Int,
    onBack: () -> Unit,
    detailsViewModel: NotificationDetailsViewModel =
        hiltViewModel(key = "notification-details-$notificationId"),
    formViewModel: NotificationFormViewModel =
        hiltViewModel(key = "notification-details-form-$notificationId")
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var isEditing by remember { mutableStateOf(false) }
    var loadedForId by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(notificationId) {
        detailsViewModel.initialize(notificationId)
    }

    val currentNotification = (detailsViewModel.state as? LoadingResult.Success)?.value

    // Prefill the shared form ViewModel (also used by the read summary) once
    // the notification arrives, and reload it whenever the loaded item changes.
    LaunchedEffect(currentNotification?.id) {
        val notification = currentNotification ?: return@LaunchedEffect
        if (loadedForId != notification.id) {
            loadedForId = notification.id
            formViewModel.load(notification)
            formViewModel.loadOptions()
        }
    }

    val savedMessage = stringResource(R.string.notification_saved)
    val saveErrorMessage = stringResource(R.string.error_save_notification)

    fun cancelEditing() {
        // Cancel reloads from the server object.
        currentNotification?.let { formViewModel.load(it) }
        isEditing = false
    }

    fun save() {
        scope.launch {
            val saved = formViewModel.save()
            if (saved != null) {
                detailsViewModel.update(saved)
                formViewModel.load(saved)
                isEditing = false
                snackbarHostState.showSnackbar(savedMessage)
            } else {
                snackbarHostState.showSnackbar(saveErrorMessage)
            }
        }
    }

    val canSave = (0..3).all { formViewModel.canProceed(it) }

    BackHandler(enabled = isEditing && !formViewModel.isSaving) {
        cancelEditing()
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            LargeFlexibleTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                ),
                scrollBehavior = scrollBehavior,
                title = {
                    Text(
                        text = when {
                            isEditing -> stringResource(R.string.notification_wizard_edit_title)
                            else -> currentNotification?.name
                                ?: stringResource(R.string.notification_details_title, notificationId)
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                actions = {
                    if (isEditing) {
                        // Cancel/Save as separate top-bar actions.
                        IconButton(
                            onClick = { cancelEditing() },
                            enabled = !formViewModel.isSaving
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = stringResource(R.string.cancel)
                            )
                        }
                        IconButton(
                            onClick = { save() },
                            enabled = canSave && !formViewModel.isSaving
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = stringResource(R.string.save)
                            )
                        }
                    } else {
                        IconButton(
                            onClick = { isEditing = true },
                            enabled = currentNotification != null
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Edit,
                                contentDescription = stringResource(R.string.edit)
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                detailsViewModel.state is LoadingResult.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                detailsViewModel.state is LoadingResult.Failure -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Rounded.Error,
                                contentDescription = null,
                                modifier = Modifier.size(56.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = stringResource(R.string.error_fetching_data),
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center
                            )
                            IconButton(onClick = { detailsViewModel.refresh() }) {
                                Icon(
                                    imageVector = Icons.Rounded.Refresh,
                                    contentDescription = null
                                )
                            }
                        }
                    }
                }
                currentNotification != null -> {
                    Box {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 16.dp)
                        ) {
                            if (isEditing) {
                                NotificationDetailsEditContent(formViewModel)
                            } else {
                                NotificationDetailsSummary(formViewModel, currentNotification)
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                        // While saving, block input silently — no modal
                        // progress blocker.
                        if (formViewModel.isSaving) {
                            val interactionSource = remember { MutableInteractionSource() }
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .pointerInput(Unit) {}
                                    .clickable(
                                        interactionSource = interactionSource,
                                        indication = null,
                                        onClick = {}
                                    )
                            )
                        }
                    }
                }
            }
        }
    }

    if (formViewModel.saveError) {
        EditorErrorAlert(
            message = stringResource(R.string.error_save_notification),
            onDismiss = { formViewModel.saveError = false }
        )
    }
    if (formViewModel.loadError) {
        EditorErrorAlert(
            message = stringResource(R.string.error_load_filter_options),
            onDismiss = { formViewModel.loadError = false }
        )
    }

    // Single atomic rule editor dialog at the screen root, driven by
    // vm.ruleSheet with keyboard staging.
    RuleEditorKeyboardStaging(viewModel = formViewModel)
    RuleEditDialog(viewModel = formViewModel)
}

/**
 * Read-only summary: rules with their values underneath, pluralized frequency,
 * cooldown, message and channels — no redundant labels.
 */
@Composable
private fun NotificationDetailsSummary(
    viewModel: NotificationFormViewModel,
    notification: UserNotification
) {
    SectionHeader(
        text = stringResource(R.string.condition),
        topPadding = Enums.SectionHeaderPaddingTop.NONE
    )
    if (viewModel.noCondition) {
        SummaryTile(text = stringResource(R.string.any_alert))
    } else if (viewModel.advancedCondition != null) {
        SummaryTile(
            text = stringResource(R.string.advanced_condition_warning),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    } else {
        viewModel.rules.forEachIndexed { index, rule ->
            RoundedCornersListTile(index = index, totalItems = viewModel.rules.size) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(
                            R.string.rule_label_fmt,
                            fieldLabel(rule.field),
                            operatorPhrase(rule.op)
                        ),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = valuesSummary(rule.values),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
    SummaryLabeledRow(
        label = stringResource(R.string.frequency),
        value = notification.threshold?.let { frequencyLabel(it.count, it.windowSeconds) }
            ?: stringResource(R.string.not_configured)
    )
    SummaryLabeledRow(
        label = stringResource(R.string.cooldown),
        value = cooldownLabel(
            notification.threshold?.cooldownSeconds
                ?: NotificationDefaults.DEFAULT_COOLDOWN_SECONDS
        )
    )
    SectionHeader(text = stringResource(R.string.content))
    SummaryTile(text = viewModel.message)
    SummaryLabeledRow(
        label = stringResource(R.string.channels),
        value = viewModel.channels
            .filter { viewModel.selectedChannelIds.contains(it.id) }
            .joinToString(", ") { it.name }
            .ifEmpty { stringResource(R.string.review_none) }
    )
}

@Composable
private fun SummaryTile(
    text: String,
    tint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    RoundedCornersListTile(index = 0, totalItems = 1) {
        Text(
            text = text,
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.bodyLarge,
            color = tint
        )
    }
}

@Composable
private fun SummaryLabeledRow(label: String, value: String) {
    RoundedCornersListTile(index = 0, totalItems = 1) {
        ListItemContent(headlineText = label, subHeadlineText = value)
    }
}

/**
 * Full edit form reusing the exact wizard components: condition sections,
 * message, alert-info sections and the channels selector.
 */
@Composable
private fun NotificationDetailsEditContent(viewModel: NotificationFormViewModel) {
    SectionHeader(
        text = stringResource(R.string.details),
        topPadding = Enums.SectionHeaderPaddingTop.NONE
    )
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
    ConditionEditorSections(viewModel = viewModel)
    SectionHeader(text = stringResource(R.string.content))
    OutlinedTextField(
        value = viewModel.message,
        onValueChange = { viewModel.message = it },
        label = { Text(stringResource(R.string.message)) },
        minLines = 2,
        maxLines = 8,
        modifier = Modifier.fillMaxWidth()
    )
    NotificationAlertInfoSections(viewModel = viewModel)
    ChannelsSelectorSection(viewModel = viewModel)
}
