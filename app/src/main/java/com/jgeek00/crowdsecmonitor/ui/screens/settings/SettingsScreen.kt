package com.jgeek00.crowdsecmonitor.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Smartphone
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import android.os.Build
import androidx.core.net.toUri
import com.jgeek00.crowdsecmonitor.R
import com.jgeek00.crowdsecmonitor.constants.Enums
import com.jgeek00.crowdsecmonitor.constants.URLs
import com.jgeek00.crowdsecmonitor.ui.components.ListItemContent
import com.jgeek00.crowdsecmonitor.ui.components.RoundedCornersListTile
import com.jgeek00.crowdsecmonitor.ui.components.SectionHeader
import com.jgeek00.crowdsecmonitor.utils.rememberIsInstalledFromOutsideGooglePlay

private data class ThemeOption(
    val mode: Enums.ThemeMode,
    val labelRes: Int,
    val icon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsScreen(
    themeMode: Enums.ThemeMode,
    onThemeModeChange: (Enums.ThemeMode) -> Unit,
    onNavigateToAppConfiguration: () -> Unit,
    onNavigateToServerConfiguration: () -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        topBar = {
            LargeFlexibleTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                ),
                scrollBehavior = scrollBehavior,
                title = { Text(stringResource(R.string.settings)) }
            )
        }
    ) { innerPadding ->
        SettingsContent(
            themeMode = themeMode,
            onThemeModeChange = onThemeModeChange,
            onNavigateToAppConfiguration = onNavigateToAppConfiguration,
            onNavigateToServerConfiguration = onNavigateToServerConfiguration,
            innerPadding = innerPadding,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SettingsContent(
    themeMode: Enums.ThemeMode,
    onThemeModeChange: (Enums.ThemeMode) -> Unit,
    onNavigateToAppConfiguration: () -> Unit,
    onNavigateToServerConfiguration: () -> Unit,
    innerPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val appVersion = rememberAppVersionName()
    val themeOptions = listOf(
        ThemeOption(Enums.ThemeMode.SYSTEM, R.string.theme_system_default, Icons.Rounded.Smartphone),
        ThemeOption(Enums.ThemeMode.LIGHT, R.string.theme_light, Icons.Rounded.LightMode),
        ThemeOption(Enums.ThemeMode.DARK, R.string.theme_dark, Icons.Rounded.DarkMode)
    )

    LazyColumn(
        modifier = modifier
            .padding(innerPadding)
            .padding(horizontal = 16.dp)
            .fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item { SectionHeader(stringResource(R.string.theme_section), topPadding = Enums.SectionHeaderPaddingTop.SMALL) }

        items(themeOptions) { option ->
            val index = themeOptions.indexOf(option)
            RoundedCornersListTile(
                index = index,
                totalItems = themeOptions.size,
                selected = themeMode == option.mode,
                onClick = { onThemeModeChange(option.mode) },
                content = {
                    ListItemContent(
                        headlineText = stringResource(option.labelRes),
                        leadingContent = { Icon(option.icon, contentDescription = null) },
                    )
                },
            )
        }

        item { SectionHeader(stringResource(R.string.configuration_section)) }

        item {
            RoundedCornersListTile(
                index = 0,
                totalItems = 2,
                onClick = onNavigateToAppConfiguration,
            ) {
                Text(stringResource(R.string.app_configuration))
            }
        }

        item {
            RoundedCornersListTile(
                index = 1,
                totalItems = 2,
                onClick = onNavigateToServerConfiguration,
            ) {
                Text(stringResource(R.string.server_configuration))
            }
        }

        item { SectionHeader(stringResource(R.string.about_section)) }

        item {
            val isOutsideGooglePlay = rememberIsInstalledFromOutsideGooglePlay()
            val aboutAppItems = if (isOutsideGooglePlay) 4 else 3
            var index = 0
            RoundedCornersListTile(
                index = index++,
                totalItems = aboutAppItems,
                onClick = {
                    CustomTabsIntent.Builder()
                        .setShowTitle(true)
                        .build()
                        .launchUrl(context, URLs.APP_DETAILS.toUri())
                }
            ) {
                ListItemContent(
                    headlineText = stringResource(R.string.more_info_app),
                    subHeadlineText = stringResource(R.string.more_info_app_description),
                    trailingContent = {
                        Icon(Icons.Rounded.OpenInNew, contentDescription = null)
                    }
                )
            }
            RoundedCornersListTile(
                index = index++,
                totalItems = aboutAppItems,
                onClick = {
                    CustomTabsIntent.Builder()
                        .setShowTitle(true)
                        .build()
                        .launchUrl(context, URLs.REST_OF_APPS.toUri())
                }
            ) {
                ListItemContent(
                    headlineText = stringResource(R.string.my_other_apps),
                    subHeadlineText = stringResource(R.string.my_other_apps_description),
                    trailingContent = {
                        Icon(Icons.Rounded.OpenInNew, contentDescription = null)
                    }
                )
            }
            if (isOutsideGooglePlay) {
                RoundedCornersListTile(
                    index = index++,
                    totalItems = aboutAppItems,
                    onClick = {
                        CustomTabsIntent.Builder()
                            .setShowTitle(true)
                            .build()
                            .launchUrl(context, URLs.PAYPAL.toUri())
                    }
                ) {
                    ListItemContent(
                        headlineText = stringResource(R.string.donate),
                        subHeadlineText = stringResource(R.string.donate_description),
                        trailingContent = {
                            Icon(Icons.Rounded.OpenInNew, contentDescription = null)
                        }
                    )
                }
            }
            RoundedCornersListTile(
                index = index++,
                totalItems = aboutAppItems,
            ) {
                ListItemContent(
                    headlineText = stringResource(R.string.app_version),
                    subHeadlineText = appVersion
                )
            }
        }

        item {
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Text(stringResource(R.string.created_by))
            }
        }
    }
}

@Composable
private fun rememberAppVersionName(): String {
    val context = LocalContext.current
    return remember(context) {
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "-"
        }.getOrDefault("-")
    }
}


