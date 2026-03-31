package com.jgeek00.crowdsecmonitor.ui.screens.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Update
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jgeek00.crowdsecmonitor.R
import com.jgeek00.crowdsecmonitor.constants.URLs
import com.jgeek00.crowdsecmonitor.data.models.LoadingResult
import com.jgeek00.crowdsecmonitor.ui.components.SectionHeader
import com.jgeek00.crowdsecmonitor.viewmodel.AuthViewModel
import com.jgeek00.crowdsecmonitor.viewmodel.ServerStatusViewModel

@Composable
fun ServerInformationSection(
    authViewModel: AuthViewModel = hiltViewModel(),
    serverStatusViewModel: ServerStatusViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val apiClient = authViewModel.apiClient

    LaunchedEffect(apiClient) {
        if (apiClient != null) {
            serverStatusViewModel.fetchStatus(apiClient)
        } else {
            serverStatusViewModel.reset()
        }
    }

    if (!authViewModel.hasServerConfigured) return

    SectionHeader(stringResource(R.string.information_section))

    ListItem(
        headlineContent = { Text(stringResource(R.string.lapi_status)) },
        trailingContent = {
            when (val s = serverStatusViewModel.status) {
                is LoadingResult.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                }
                is LoadingResult.Success -> {
                    if (s.data.csLapi.lapiConnected) {
                        StatusOnline()
                    } else {
                        StatusOffline()
                    }
                }
                is LoadingResult.Failure -> {
                    StatusOffline()
                }
            }
        }
    )

    ListItem(
        headlineContent = { Text(stringResource(R.string.api_version)) },
        trailingContent = {
            when (val s = serverStatusViewModel.status) {
                is LoadingResult.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                }
                is LoadingResult.Success -> {
                    Text(text = s.data.csMonitorApi.version)
                }
                is LoadingResult.Failure -> {
                    Text(text = stringResource(R.string.not_available))
                }
            }
        }
    )

    val newVersion = (serverStatusViewModel.status as? LoadingResult.Success)
        ?.data?.csMonitorApi?.newVersionAvailable

    if (newVersion != null) {
        ListItem(
            modifier = Modifier.clickable {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(URLs.API_PACKAGE_URL))
                context.startActivity(intent)
            },
            leadingContent = {
                Icon(
                    imageVector = Icons.Rounded.Update,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50)
                )
            },
            headlineContent = {
                Text(
                    text = stringResource(R.string.new_version_available),
                    color = Color(0xFF4CAF50),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                    )
                )
            },
            trailingContent = {
                Text(
                    text = newVersion,
                    color = Color(0xFF4CAF50),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                    )
                )
            }
        )
    }
}

@Composable
private fun StatusOnline() {
    androidx.compose.foundation.layout.Row(
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Rounded.Check,
            contentDescription = null,
            tint = Color(0xFF4CAF50),
            modifier = Modifier.size(18.dp)
        )
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(6.dp))
        Text(
            text = stringResource(R.string.online),
            color = Color(0xFF4CAF50),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun StatusOffline() {
    androidx.compose.foundation.layout.Row(
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Rounded.Close,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(18.dp)
        )
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(6.dp))
        Text(
            text = stringResource(R.string.offline),
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

