package com.jgeek00.crowdsecmonitor.ui.components

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
import com.jgeek00.crowdsecmonitor.data.models.Enums
import com.jgeek00.crowdsecmonitor.viewmodel.ConnectionFormViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectionForm(
    viewModel: ConnectionFormViewModel,
    modifier: Modifier = Modifier,
    showHeader: Boolean = true
) {
    val clipboardManager = LocalClipboardManager.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
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
        }

        // Server Information Section
        SectionTitle(stringResource(R.string.server_information))
        OutlinedTextField(
            value = viewModel.name,
            onValueChange = { viewModel.name = it },
            label = { Text(stringResource(R.string.name)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
            enabled = !viewModel.connecting
        )

        // Server Route Section
        SectionTitle(stringResource(R.string.server_route))
        
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = stringResource(R.string.connection_method), style = MaterialTheme.typography.labelMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Enums.ConnectionMethod.entries.forEach { method ->
                    FilterChip(
                        selected = viewModel.connectionMethod == method,
                        onClick = { viewModel.connectionMethod = method },
                        label = { Text(method.name) },
                        enabled = !viewModel.connecting
                    )
                }
            }
        }

        OutlinedTextField(
            value = viewModel.ipDomain,
            onValueChange = { viewModel.ipDomain = it },
            label = { Text(stringResource(R.string.ip_address_or_domain)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Uri,
                capitalization = KeyboardCapitalization.None
            ),
            enabled = !viewModel.connecting
        )

        OutlinedTextField(
            value = viewModel.port,
            onValueChange = { viewModel.port = it },
            label = { Text(stringResource(R.string.port)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            enabled = !viewModel.connecting
        )

        OutlinedTextField(
            value = viewModel.path,
            onValueChange = { viewModel.path = it },
            label = { Text(stringResource(R.string.path)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Uri,
                capitalization = KeyboardCapitalization.None
            ),
            enabled = !viewModel.connecting
        )

        // Authentication Section
        SectionTitle(stringResource(R.string.authentication))
        
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
                    value = viewModel.basicUser,
                    onValueChange = { viewModel.basicUser = it },
                    label = { Text(stringResource(R.string.username)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.None),
                    enabled = !viewModel.connecting
                )
                OutlinedTextField(
                    value = viewModel.basicPassword,
                    onValueChange = { viewModel.basicPassword = it },
                    label = { Text(stringResource(R.string.password)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        capitalization = KeyboardCapitalization.None
                    ),
                    enabled = !viewModel.connecting
                )
            }
            Enums.AuthMethod.BEARER -> {
                OutlinedTextField(
                    value = viewModel.bearerToken,
                    onValueChange = { viewModel.bearerToken = it },
                    label = { Text(stringResource(R.string.token)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    trailingIcon = {
                        IconButton(onClick = {
                            clipboardManager.getText()?.let {
                                viewModel.bearerToken = it.text
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
                    enabled = !viewModel.connecting
                )
            }
            else -> {}
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    // Alerts
    if (viewModel.invalidValuesAlert) {
        AlertDialog(
            onDismissRequest = { viewModel.invalidValuesAlert = false },
            title = { Text(stringResource(R.string.invalid_values)) },
            text = { Text(viewModel.invalidValuesMessage) },
            confirmButton = {
                TextButton(onClick = { viewModel.invalidValuesAlert = false }) {
                    Text(stringResource(R.string.close))
                }
            }
        )
    }

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

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold
    )
}
