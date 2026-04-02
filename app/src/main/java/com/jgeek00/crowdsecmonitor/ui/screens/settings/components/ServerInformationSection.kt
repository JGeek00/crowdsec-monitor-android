package com.jgeek00.crowdsecmonitor.ui.screens.settings.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Update
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import com.jgeek00.crowdsecmonitor.R
import com.jgeek00.crowdsecmonitor.constants.URLs
import com.jgeek00.crowdsecmonitor.data.models.LoadingResult
import com.jgeek00.crowdsecmonitor.ui.components.ListItemContent
import com.jgeek00.crowdsecmonitor.ui.components.RoundedCornersListTile
import com.jgeek00.crowdsecmonitor.ui.components.SectionHeader
import com.jgeek00.crowdsecmonitor.viewmodel.AuthViewModel
import com.jgeek00.crowdsecmonitor.viewmodel.ServerStatusViewModel
import androidx.core.net.toUri

@Composable
fun ServerInformationSection(
    authViewModel: AuthViewModel = hiltViewModel(),
    serverStatusViewModel: ServerStatusViewModel = hiltViewModel()
) {
    @Composable
    fun getStatusSubtitle(): String {
        return when (val s = serverStatusViewModel.status) {
            is LoadingResult.Loading -> {
                stringResource(R.string.loading)
            }

            is LoadingResult.Success -> {
                if (s.value.csLapi.lapiConnected) {
                    stringResource(R.string.online)
                } else {
                    stringResource(R.string.offline)
                }
            }

            is LoadingResult.Failure -> {
                "N/A"
            }
        }
    }

    @Composable
    fun getVersionSubtitle(): String {
        return when (val s = serverStatusViewModel.status) {
            is LoadingResult.Loading -> {
                stringResource(R.string.loading)
            }

            is LoadingResult.Success -> {
                s.value.csMonitorApi.version
            }

            is LoadingResult.Failure -> {
                "N/A"
            }
        }
    }

    val context = LocalContext.current

    if (!authViewModel.hasServerConfigured) return

    SectionHeader(stringResource(R.string.information_section))

    RoundedCornersListTile(
        index = 0,
        totalItems = 2,
    ) {
        ListItemContent(
            headlineText = stringResource(R.string.lapi_status),
            subHeadlineText = getStatusSubtitle()
        )
    }

    RoundedCornersListTile(
        index = 1,
        totalItems = 2,
    ) {
        ListItemContent(
            headlineText = stringResource(R.string.api_version),
            subHeadlineText = getVersionSubtitle()
        )
    }

    val newVersion = serverStatusViewModel.status.data?.csMonitorApi?.newVersionAvailable

    if (newVersion != null) {
        ListItem(
            modifier = Modifier.clickable {
                val intent = Intent(Intent.ACTION_VIEW, URLs.API_PACKAGE.toUri())
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
                        fontWeight = FontWeight.SemiBold
                    )
                )
            },
            trailingContent = {
                Text(
                    text = newVersion,
                    color = Color(0xFF4CAF50),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        )
    }
}
