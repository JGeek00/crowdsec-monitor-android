package com.jgeek00.crowdsecmonitor.ui.components.connectionForm

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.sp
import com.jgeek00.crowdsecmonitor.R
import com.jgeek00.crowdsecmonitor.constants.Enums
import com.jgeek00.crowdsecmonitor.ui.components.SectionHeader
import com.jgeek00.crowdsecmonitor.viewmodel.ConnectionFormViewModel
import com.jgeek00.crowdsecmonitor.viewmodel.MAX_CUSTOM_HEADERS

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
            topPadding = Enums.SectionHeaderPaddingTop.NONE
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
            enabled = viewModel.name.enabled,
            maxLines = 1,
        )

        // Server Route Section
        SectionHeader(
            text = stringResource(R.string.server_route),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
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
            enabled = viewModel.ipDomain.enabled,
            maxLines = 1,
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
            enabled = viewModel.port.enabled,
            maxLines = 1,
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
            enabled = viewModel.path.enabled,
            maxLines = 1,
        )

        // Authentication Section
        SectionHeader(
            text = stringResource(R.string.authentication),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
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
                enabled = !viewModel.connecting,
                maxLines = 1,
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
                    enabled = viewModel.basicUser.enabled,
                    maxLines = 1,
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
                    enabled = viewModel.basicPassword.enabled,
                    maxLines = 1,
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
                    enabled = viewModel.bearerToken.enabled,
                    maxLines = 1,
                )
            }
            else -> {}
        }

        SectionHeader(
            text = stringResource(R.string.custom_headers),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
        )

        viewModel.customHeaders.forEachIndexed { index, header ->
            Card(
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 8.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(R.string.custom_header_n, index + 1),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { viewModel.removeCustomHeader(index) },
                            enabled = !viewModel.connecting
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = stringResource(R.string.remove_custom_header)
                            )
                        }
                    }
                    OutlinedTextField(
                        value = header.key,
                        onValueChange = { viewModel.updateCustomHeaderKey(index, it) },
                        label = { Text(stringResource(R.string.header_name)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = header.keyError != null,
                        supportingText = header.keyError?.let { { Text(it) } },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Ascii,
                            capitalization = KeyboardCapitalization.None
                        ),
                        enabled = !viewModel.connecting,
                        maxLines = 1,
                        placeholder = { Text("X-My-Header") }
                    )
                    OutlinedTextField(
                        value = header.value,
                        onValueChange = { viewModel.updateCustomHeaderValue(index, it) },
                        label = { Text(stringResource(R.string.header_value)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = header.valueError != null,
                        supportingText = header.valueError?.let { { Text(it) } },
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.None
                        ),
                        enabled = !viewModel.connecting,
                        maxLines = 1,
                    )
                }
            }
        }

        if (viewModel.customHeaders.size < MAX_CUSTOM_HEADERS) {
            OutlinedButton(
                onClick = { viewModel.addCustomHeader() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !viewModel.connecting
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.add_custom_header))
            }
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

