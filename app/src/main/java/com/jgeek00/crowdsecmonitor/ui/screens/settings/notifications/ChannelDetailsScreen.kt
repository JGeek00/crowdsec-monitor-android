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
import androidx.compose.foundation.layout.width
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
import com.jgeek00.crowdsecmonitor.data.models.LoadingResult
import com.jgeek00.crowdsecmonitor.data.models.NotificationProvider
import com.jgeek00.crowdsecmonitor.data.models.UserNotificationChannel
import com.jgeek00.crowdsecmonitor.ui.components.ListItemContent
import com.jgeek00.crowdsecmonitor.ui.components.RoundedCornersListTile
import com.jgeek00.crowdsecmonitor.ui.components.SectionHeader
import androidx.compose.foundation.layout.Arrangement
import com.jgeek00.crowdsecmonitor.viewmodel.ChannelDetailsViewModel
import com.jgeek00.crowdsecmonitor.viewmodel.ChannelFormViewModel
import kotlinx.coroutines.launch

/**
 * Channel details: read-only summary with an in-place edit mode. Read-only rows
 * are derived from the provider definition (value falls back to the field
 * default), with a raw-config-keys fallback when the definition is unavailable;
 * secrets are never displayed. Editing reuses the wizard's dynamic field
 * editors; `isSaving` blocks input without a modal blocker. Delete stays out of
 * edit mode.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChannelDetailsScreen(
    channelId: Int,
    onBack: () -> Unit,
    detailsViewModel: ChannelDetailsViewModel =
        hiltViewModel(key = "channel-details-$channelId"),
    formViewModel: ChannelFormViewModel =
        hiltViewModel(key = "channel-details-form-$channelId")
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var isEditing by remember { mutableStateOf(false) }
    var loadedForId by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(channelId) {
        detailsViewModel.initialize(channelId)
        formViewModel.loadProviders()
    }

    val currentChannel = (detailsViewModel.state as? LoadingResult.Success)?.value

    // Prefill the shared form ViewModel once the channel arrives (edit mode is
    // instant; the definition drives the read-only rows too).
    LaunchedEffect(currentChannel?.id) {
        val channel = currentChannel ?: return@LaunchedEffect
        if (loadedForId != channel.id) {
            loadedForId = channel.id
            formViewModel.load(channel)
        }
    }

    val savedMessage = stringResource(R.string.channel_saved)
    val saveErrorMessage = stringResource(R.string.error_save_channel)

    fun cancelEditing() {
        // Cancel reloads from the server object.
        currentChannel?.let { formViewModel.load(it) }
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

    val canSave = formViewModel.nameValid() &&
        formViewModel.providerValid() &&
        formViewModel.definition != null

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
                            isEditing -> stringResource(R.string.channel_wizard_edit_title)
                            else -> currentChannel?.name
                                ?: stringResource(R.string.channel_details_title, channelId)
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
                            enabled = currentChannel != null
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
                currentChannel != null -> {
                    Box {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 16.dp)
                        ) {
                            if (isEditing) {
                                ChannelDetailsEditContent(formViewModel)
                            } else {
                                ChannelDetailsSummaryContent(
                                    channel = currentChannel,
                                    definition = formViewModel.definition
                                )
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
            message = stringResource(R.string.error_save_channel),
            onDismiss = { formViewModel.saveError = false }
        )
    }
    if (formViewModel.loadError) {
        EditorErrorAlert(
            message = stringResource(R.string.error_load_providers),
            onDismiss = { formViewModel.loadError = false }
        )
    }
}

/**
 * Read-only summary: provider row with the channel icon, plus configuration
 * rows derived from the provider definition — secrets hidden, value falling
 * back to the field default; raw config keys fallback when the definition is
 * unavailable.
 */
@Composable
private fun ChannelDetailsSummaryContent(
    channel: UserNotificationChannel,
    definition: NotificationProvider?
) {
    val yesLabel = stringResource(R.string.common_yes)
    val noLabel = stringResource(R.string.common_no)

    SectionHeader(text = stringResource(R.string.channel_section))
    RoundedCornersListTile(index = 0, totalItems = 1) {
        ListItemContent(
            headlineText = stringResource(R.string.provider),
            subHeadlineText = channel.type.value,
            leadingContent = {
                Icon(
                    imageVector = channelIconVector(definition?.icon ?: channel.type.value),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        )
    }

    SectionHeader(text = stringResource(R.string.configuration_section))
    val definitionLabels = definition?.fields
        ?.associate { it.key to backendString(it.labelKey) }
        ?: emptyMap()
    val rows = ChannelDetailsSummary.buildRows(
        channel = channel,
        definition = definition,
        definitionLabels = definitionLabels,
        yesLabel = yesLabel,
        noLabel = noLabel
    )
    if (rows.isEmpty()) {
        RoundedCornersListTile(index = 0, totalItems = 1) {
            Text(
                text = stringResource(R.string.no_configuration_values),
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        rows.forEachIndexed { index, row ->
            RoundedCornersListTile(index = index, totalItems = rows.size) {
                ListItemContent(headlineText = row.label, subHeadlineText = row.value)
            }
        }
    }
}

/**
 * Edit form reusing the wizard's dynamic field editors; when the provider
 * definition could not be loaded, editing the configuration is unavailable.
 */
@Composable
private fun ChannelDetailsEditContent(viewModel: ChannelFormViewModel) {
    SectionHeader(text = stringResource(R.string.name))
    OutlinedTextField(
        value = viewModel.name,
        onValueChange = { viewModel.name = it },
        label = { Text(stringResource(R.string.name)) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
    if (viewModel.definition == null) {
        FormInfoBox(label = stringResource(R.string.error_load_providers))
    } else {
        viewModel.groupedFields().forEach { (section, fields) ->
            if (section != null) {
                SectionHeader(text = backendString(section.labelKey))
            }
            Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                fields.forEach { field ->
                    ProviderFieldEditor(viewModel = viewModel, field = field)
                }
            }
        }
    }
}
