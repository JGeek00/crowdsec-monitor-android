package com.jgeek00.crowdsecmonitor.ui.components.connectionForm

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jgeek00.crowdsecmonitor.R
import com.jgeek00.crowdsecmonitor.constants.Enums
import com.jgeek00.crowdsecmonitor.ui.components.SectionHeader
import com.jgeek00.crowdsecmonitor.viewmodel.ConnectionFormViewModel

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ConnectionForm(
    viewModel: ConnectionFormViewModel,
    modifier: Modifier = Modifier,
    showHeader: Boolean = true,
    scrollState: androidx.compose.foundation.ScrollState = rememberScrollState()
) {
    val clipboardManager = LocalClipboardManager.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .imePadding()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        if (showHeader) {
            Column(
                modifier = Modifier.padding(vertical = 24.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.List,
                    contentDescription = null,
                    modifier = Modifier.size(60.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = stringResource(R.string.setup_server_connection),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 30.sp
                )
            }
        } else {
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Server Information Section
        SectionHeader(
            text = stringResource(R.string.server_information),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            paddingValues = if (showHeader) PaddingValues(top = 12.dp) else PaddingValues(0.dp)
        )
        OutlinedTextField(
            value = viewModel.name.value,
            onValueChange = { viewModel.validateName(it) },
            label = { Text(stringResource(R.string.name)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = viewModel.name.error != null,
            supportingText = viewModel.name.error?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
            enabled = viewModel.name.enabled
        )

        // Server Route Section
        SectionHeader(
            text = stringResource(R.string.server_route),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            paddingValues = PaddingValues(top = 12.dp)
        )
        
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = stringResource(R.string.connection_method), style = MaterialTheme.typography.labelMedium)
            ButtonGroup(
                modifier = Modifier.fillMaxWidth(),
                overflowIndicator = {}
            ) {
                Enums.ConnectionMethod.entries.forEach { method ->
                    toggleableItem(
                        checked = viewModel.connectionMethod == method,
                        onCheckedChange = { viewModel.connectionMethod = method },
                        label = method.name,
                        weight = 1f,
                        enabled = !viewModel.connecting
                    )
                }
            }
        }

        OutlinedTextField(
            value = viewModel.ipDomain.value,
            onValueChange = { viewModel.validateIpDomain(it) },
            label = { Text(stringResource(R.string.ip_address_or_domain)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = viewModel.ipDomain.error != null,
            supportingText = viewModel.ipDomain.error?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Uri,
                capitalization = KeyboardCapitalization.None
            ),
            enabled = viewModel.ipDomain.enabled
        )

        OutlinedTextField(
            value = viewModel.port.value,
            onValueChange = { viewModel.validatePort(it) },
            label = { Text(stringResource(R.string.port)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = viewModel.port.error != null,
            supportingText = viewModel.port.error?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            enabled = viewModel.port.enabled
        )

        OutlinedTextField(
            value = viewModel.path.value,
            onValueChange = { viewModel.validatePath(it) },
            label = { Text(stringResource(R.string.path)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = viewModel.path.error != null,
            supportingText = viewModel.path.error?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Uri,
                capitalization = KeyboardCapitalization.None
            ),
            enabled = viewModel.path.enabled
        )

        // Authentication Section
        SectionHeader(
            text = stringResource(R.string.authentication),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            paddingValues = PaddingValues(top = 12.dp)
        )
        
        var expanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { if (!viewModel.connecting) expanded = !expanded }
        ) {
            OutlinedTextField(
                value = when (viewModel.authMethod) {
                    Enums.AuthMethod.NONE -> stringResource(R.string.none)
                    Enums.AuthMethod.BASIC -> stringResource(R.string.username_password)
                    Enums.AuthMethod.BEARER -> stringResource(R.string.access_token)
                },
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.method)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                enabled = !viewModel.connecting
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.none)) },
                    onClick = {
                        viewModel.authMethod = Enums.AuthMethod.NONE
                        expanded = false
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.username_password)) },
                    onClick = {
                        viewModel.authMethod = Enums.AuthMethod.BASIC
                        expanded = false
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.access_token)) },
                    onClick = {
                        viewModel.authMethod = Enums.AuthMethod.BEARER
                        expanded = false
                    }
                )
            }
        }

        when (viewModel.authMethod) {
            Enums.AuthMethod.BASIC -> {
                OutlinedTextField(
                    value = viewModel.basicUser.value,
                    onValueChange = { viewModel.validateBasicUser(it) },
                    label = { Text(stringResource(R.string.username)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = viewModel.basicUser.error != null,
                    supportingText = viewModel.basicUser.error?.let { { Text(it) } },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.None),
                    enabled = viewModel.basicUser.enabled
                )
                OutlinedTextField(
                    value = viewModel.basicPassword.value,
                    onValueChange = { viewModel.validateBasicPassword(it) },
                    label = { Text(stringResource(R.string.password)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = viewModel.basicPassword.error != null,
                    supportingText = viewModel.basicPassword.error?.let { { Text(it) } },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        capitalization = KeyboardCapitalization.None
                    ),
                    enabled = viewModel.basicPassword.enabled
                )
            }
            Enums.AuthMethod.BEARER -> {
                OutlinedTextField(
                    value = viewModel.bearerToken.value,
                    onValueChange = { viewModel.validateBearerToken(it) },
                    label = { Text(stringResource(R.string.token)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = viewModel.bearerToken.error != null,
                    supportingText = viewModel.bearerToken.error?.let { { Text(it) } },
                    trailingIcon = {
                        IconButton(onClick = {
                            clipboardManager.getText()?.let {
                                viewModel.validateBearerToken(it.text)
                            }
                        }) {
                            Icon(Icons.Default.Add, contentDescription = "Paste")
                        }
                    },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        capitalization = KeyboardCapitalization.None
                    ),
                    enabled = viewModel.bearerToken.enabled
                )
            }
            else -> {}
        }

        Spacer(modifier = Modifier.height(48.dp))
    }

    // Alerts
    if (viewModel.connectionErrorAlert) {
        AlertDialog(
            onDismissRequest = { viewModel.connectionErrorAlert = false },
            title = { Text(stringResource(R.string.connection_error)) },
            text = { Text(viewModel.connectionErrorMessage) },
            confirmButton = {
                TextButton(onClick = { viewModel.connectionErrorAlert = false }) {
                    Text(stringResource(R.string.close))
                }
            }
        )
    }
}

