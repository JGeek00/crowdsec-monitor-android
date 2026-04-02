package com.jgeek00.crowdsecmonitor.ui.screens.lists.blocklists

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jgeek00.crowdsecmonitor.R
import com.jgeek00.crowdsecmonitor.constants.Enums
import com.jgeek00.crowdsecmonitor.ui.components.FullScreenDialog
import com.jgeek00.crowdsecmonitor.ui.components.SectionHeader
import com.jgeek00.crowdsecmonitor.viewmodel.AddBlocklistFormViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBlocklistFormScreen(
    onClose: (blocklistAdded: Boolean) -> Unit,
    viewModel: AddBlocklistFormViewModel = hiltViewModel()
) {
    FullScreenDialog(
        title = stringResource(R.string.add_blocklist),
        onClose = { onClose(false) },
        allowClose = !viewModel.isSaving,
        dismissConfirmation = false,
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                SectionHeader(
                    text = stringResource(R.string.blocklist_data),
                    topPadding = Enums.SectionHeaderPaddingTop.SMALL
                )
                OutlinedTextField(
                    value = viewModel.name,
                    onValueChange = { viewModel.name = it },
                    label = { Text(stringResource(R.string.name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !viewModel.isSaving,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.None,
                        imeAction = ImeAction.Next
                    )
                )
                Spacer(modifier = Modifier.height(24.dp))
                OutlinedTextField(
                    value = viewModel.url,
                    onValueChange = { viewModel.url = it },
                    label = { Text(stringResource(R.string.url)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !viewModel.isSaving,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Uri,
                        capitalization = KeyboardCapitalization.None,
                        autoCorrectEnabled = false,
                        imeAction = ImeAction.Done
                    )
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
                onClick = {
                    viewModel.save(onSuccess = { onClose(true) })
                },
                enabled = !viewModel.isSaving
            ) {
                if (viewModel.isSaving) {
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

    // Required fields error
    if (viewModel.requiredFieldsError) {
        AlertDialog(
            onDismissRequest = { viewModel.requiredFieldsError = false },
            title = { Text(stringResource(R.string.fill_required_fields)) },
            text = { Text(stringResource(R.string.fill_required_fields_msg)) },
            confirmButton = {
                TextButton(onClick = { viewModel.requiredFieldsError = false }) {
                    Text(stringResource(R.string.ok))
                }
            }
        )
    }

    // Invalid URL error
    if (viewModel.invalidUrlError) {
        AlertDialog(
            onDismissRequest = { viewModel.invalidUrlError = false },
            title = { Text(stringResource(R.string.url_not_valid)) },
            text = { Text(stringResource(R.string.url_not_valid_msg)) },
            confirmButton = {
                TextButton(onClick = { viewModel.invalidUrlError = false }) {
                    Text(stringResource(R.string.ok))
                }
            }
        )
    }

    // Generic saving error
    if (viewModel.savingError) {
        AlertDialog(
            onDismissRequest = { viewModel.savingError = false },
            title = { Text(stringResource(R.string.error)) },
            text = { Text(stringResource(R.string.error_adding_blocklist)) },
            confirmButton = {
                TextButton(onClick = { viewModel.savingError = false }) {
                    Text(stringResource(R.string.ok))
                }
            }
        )
    }
}

