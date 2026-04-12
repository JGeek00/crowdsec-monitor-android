package com.jgeek00.crowdsecmonitor.ui.screens.lists.ipsChecker

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jgeek00.crowdsecmonitor.R
import com.jgeek00.crowdsecmonitor.constants.Enums
import com.jgeek00.crowdsecmonitor.ui.components.FullScreenDialog
import com.jgeek00.crowdsecmonitor.ui.components.SectionHeader
import com.jgeek00.crowdsecmonitor.viewmodel.IPsCheckerViewModel

private const val IPS_CHECKER_ANIM_DURATION = 350

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IPsCheckerScreen(
    onClose: () -> Unit,
    viewModel: IPsCheckerViewModel = hiltViewModel()
) {
    var showResults by remember { mutableStateOf(false) }
    var showInvalidIpAlert by remember { mutableStateOf(false) }

    FullScreenDialog(
        title = stringResource(R.string.ip_addresses_checker),
        navigationIcon = if (showResults) Icons.AutoMirrored.Rounded.ArrowBack else Icons.Rounded.Close,
        navigationIconContentDescription = if (showResults) stringResource(R.string.back) else null,
        onClose = {
            if (showResults) {
                showResults = false
            } else {
                viewModel.resetAfterClose()
                onClose()
            }
        },
        actions = {}
    ) { innerPadding ->
        AnimatedContent(
            targetState = showResults,
            transitionSpec = {
                val direction = if (targetState) 1 else -1
                slideInHorizontally(
                    initialOffsetX = { (it * 0.10f * direction).toInt() },
                    animationSpec = tween(IPS_CHECKER_ANIM_DURATION, easing = FastOutSlowInEasing)
                ) + fadeIn(tween(IPS_CHECKER_ANIM_DURATION, easing = FastOutSlowInEasing)) togetherWith
                        slideOutHorizontally(
                            targetOffsetX = { -(it * 0.10f * direction).toInt() },
                            animationSpec = tween(IPS_CHECKER_ANIM_DURATION, easing = FastOutSlowInEasing)
                        ) + fadeOut(tween(IPS_CHECKER_ANIM_DURATION, easing = FastOutSlowInEasing))
            },
            label = "IPsCheckerContent",
            modifier = Modifier.fillMaxSize()
        ) { displayResults ->
            if (displayResults) {
                BackHandler { showResults = false }
                IPsCheckerResultsContent(
                    viewModel = viewModel,
                    innerPadding = innerPadding
                )
            } else {
                IPsCheckerFormContent(
                    viewModel = viewModel,
                    innerPadding = innerPadding,
                    onShowInvalidIpAlert = { showInvalidIpAlert = true },
                    onCheckIps = {
                        viewModel.checkIps()
                        showResults = true
                    }
                )
            }
        }
    }

    if (showInvalidIpAlert) {
        AlertDialog(
            onDismissRequest = { showInvalidIpAlert = false },
            title = { Text(stringResource(R.string.invalid_ip_address)) },
            text = { Text(stringResource(R.string.invalid_ip_address_msg)) },
            confirmButton = {
                TextButton(onClick = { showInvalidIpAlert = false }) {
                    Text(stringResource(R.string.ok))
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun IPsCheckerFormContent(
    viewModel: IPsCheckerViewModel,
    innerPadding: PaddingValues,
    onShowInvalidIpAlert: () -> Unit,
    onCheckIps: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(innerPadding)
            .padding(horizontal = 16.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        val blocklistsLabel = stringResource(R.string.blocklists)
        val allowlistsLabel = stringResource(R.string.allowlists)
        Spacer(modifier = Modifier.height(12.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = stringResource(R.string.list_type),
                style = MaterialTheme.typography.labelMedium
            )
            ButtonGroup(
                modifier = Modifier.fillMaxWidth(),
                overflowIndicator = {}
            ) {
                toggleableItem(
                    checked = viewModel.selectedListType == Enums.ListType.BLOCKLIST,
                    onCheckedChange = { viewModel.selectedListType = Enums.ListType.BLOCKLIST },
                    label = blocklistsLabel,
                    weight = 1f
                )
                toggleableItem(
                    checked = viewModel.selectedListType == Enums.ListType.ALLOWLIST,
                    onCheckedChange = { viewModel.selectedListType = Enums.ListType.ALLOWLIST },
                    label = allowlistsLabel,
                    weight = 1f
                )
            }
        }

        SectionHeader(text = stringResource(R.string.ip_addresses_to_validate))

        viewModel.ipsToCheck.forEachIndexed { index, field ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                OutlinedTextField(
                    value = field.value,
                    onValueChange = { viewModel.updateEntry(index, it) },
                    label = { Text(stringResource(R.string.enter_ip_address_hint)) },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    isError = field.invalid,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Uri,
                        capitalization = KeyboardCapitalization.None,
                        autoCorrectEnabled = false
                    ),
                    trailingIcon = if (field.invalid) {
                        {
                            IconButton(onClick = onShowInvalidIpAlert) {
                                Icon(
                                    imageVector = Icons.Rounded.Warning,
                                    contentDescription = stringResource(R.string.invalid_ip_address),
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    } else null
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = { viewModel.removeEntry(index) }) {
                    Icon(
                        imageVector = Icons.Rounded.Delete,
                        contentDescription = stringResource(R.string.remove)
                    )
                }
            }
        }

        OutlinedButton(
            onClick = { viewModel.addEntry() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(imageVector = Icons.Rounded.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = stringResource(R.string.add))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onCheckIps,
            enabled = viewModel.ipsToCheck.isNotEmpty(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = stringResource(R.string.check_ip_addresses),
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun IPsCheckerResultsContent(
    viewModel: IPsCheckerViewModel,
    innerPadding: PaddingValues
) {
    when (viewModel.selectedListType) {
        Enums.ListType.BLOCKLIST -> BlocklistsResultContent(viewModel, innerPadding)
        Enums.ListType.ALLOWLIST -> AllowlistsResultContent(viewModel, innerPadding)
    }
}
