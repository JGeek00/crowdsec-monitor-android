package com.jgeek00.crowdsecmonitor.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Smartphone
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jgeek00.crowdsecmonitor.R
import com.jgeek00.crowdsecmonitor.constants.Enums
import com.jgeek00.crowdsecmonitor.ui.components.NavigationListItem
import com.jgeek00.crowdsecmonitor.ui.components.SectionHeader

private data class ThemeOption(
    val mode: Enums.ThemeMode,
    val labelRes: Int,
    val icon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
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
            LargeTopAppBar(
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

@Composable
private fun SettingsContent(
    themeMode: Enums.ThemeMode,
    onThemeModeChange: (Enums.ThemeMode) -> Unit,
    onNavigateToAppConfiguration: () -> Unit,
    onNavigateToServerConfiguration: () -> Unit,
    innerPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    val appVersion = rememberAppVersionName()
    val themeOptions = listOf(
        ThemeOption(Enums.ThemeMode.SYSTEM, R.string.theme_system_default, Icons.Rounded.Smartphone),
        ThemeOption(Enums.ThemeMode.LIGHT, R.string.theme_light, Icons.Rounded.LightMode),
        ThemeOption(Enums.ThemeMode.DARK, R.string.theme_dark, Icons.Rounded.DarkMode)
    )

    LazyColumn(
        modifier = modifier
            .padding(innerPadding)
            .fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item { SectionHeader(stringResource(R.string.theme_section)) }

        items(themeOptions) { option ->
            ListItem(
                leadingContent = { Icon(option.icon, contentDescription = null) },
                headlineContent = { Text(stringResource(option.labelRes)) },
                trailingContent = {
                    RadioButton(
                        selected = themeMode == option.mode,
                        onClick = { onThemeModeChange(option.mode) }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onThemeModeChange(option.mode) }
            )
        }

        item { SectionHeader(stringResource(R.string.configuration_section)) }

        item {
            NavigationListItem(
                title = stringResource(R.string.app_configuration),
                onClick = onNavigateToAppConfiguration
            )
        }

        item {
            NavigationListItem(
                title = stringResource(R.string.server_configuration),
                onClick = onNavigateToServerConfiguration
            )
        }

        item { SectionHeader(stringResource(R.string.about_section)) }

        item {
            ListItem(
                headlineContent = { Text(stringResource(R.string.app_version)) },
                supportingContent = { Text(appVersion) }
            )
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
