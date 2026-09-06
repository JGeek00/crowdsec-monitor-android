package com.jgeek00.crowdsecmonitor.ui.screens.settings

import android.app.LocaleManager
import android.os.LocaleList
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jgeek00.crowdsecmonitor.R
import com.jgeek00.crowdsecmonitor.constants.Languages
import com.jgeek00.crowdsecmonitor.ui.components.ListItemContent
import com.jgeek00.crowdsecmonitor.ui.components.RoundedCornersListTile

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LanguageSelectionScreen(
    onBack: () -> Unit
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
                title = { Text(stringResource(R.string.language_section)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        val context = LocalContext.current
        val localeManager = remember {
            context.getSystemService(LocaleManager::class.java)
        }
        var selectedTag by remember {
            mutableStateOf(
                localeManager.applicationLocales.toLanguageTags().takeIf { it.isNotEmpty() }
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                RoundedCornersListTile(
                    index = 0,
                    totalItems = 1,
                    selected = selectedTag == null,
                    onClick = {
                        localeManager.applicationLocales = LocaleList.getEmptyLocaleList()
                        selectedTag = null
                    }
                ) {
                    ListItemContent(
                        headlineText = stringResource(R.string.language_system_default)
                    )
                }
            }
            item {
                Spacer(modifier = Modifier.height(12.dp))
            }
            item {
                Languages.available.forEachIndexed { index, language ->
                    RoundedCornersListTile(
                        index = index,
                        totalItems = Languages.available.size,
                        selected = selectedTag?.startsWith(language.tag) == true,
                        onClick = {
                            localeManager.applicationLocales =
                                LocaleList.forLanguageTags(language.tag)
                            selectedTag = language.tag
                        }
                    ) {
                        ListItemContent(headlineText = language.name)
                    }
                }
            }
        }
    }
}
