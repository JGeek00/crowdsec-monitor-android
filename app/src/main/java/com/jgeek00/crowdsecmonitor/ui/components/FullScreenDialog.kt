package com.jgeek00.crowdsecmonitor.ui.components

import android.graphics.Color
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.jgeek00.crowdsecmonitor.R
import com.jgeek00.crowdsecmonitor.ui.theme.LocalDarkTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullScreenDialog(
    onClose: () -> Unit,
    title: String,
    allowClose: Boolean = true,
    dismissConfirmation: Boolean = false,
    navigationIcon: ImageVector = Icons.Rounded.Close,
    navigationIconContentDescription: String? = null,
    actions: @Composable () -> Unit,
    content: @Composable (innerPadding: PaddingValues) -> Unit,
    ) {
    val darkTheme = LocalDarkTheme.current
    var showDiscardConfirmation by remember { mutableStateOf(false) }

    fun handleClose() {
        if (dismissConfirmation) {
            showDiscardConfirmation = true
        } else {
            onClose()
        }
    }

    Dialog(
        onDismissRequest = { handleClose() },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            decorFitsSystemWindows = false
        )
    ) {
        BackHandler { handleClose() }

        val dialogWindow = (LocalView.current.parent as? DialogWindowProvider)?.window
        SideEffect {
            dialogWindow?.let { window ->
                WindowCompat.setDecorFitsSystemWindows(window, false)
                val controller = WindowInsetsControllerCompat(window, window.decorView)
                controller.isAppearanceLightStatusBars = !darkTheme
                controller.isAppearanceLightNavigationBars = !darkTheme
                @Suppress("DEPRECATION")
                window.statusBarColor = Color.TRANSPARENT
                @Suppress("DEPRECATION")
                window.navigationBarColor = Color.TRANSPARENT
            }
        }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = { Text(title) },
                    navigationIcon = {
                        val navDesc = navigationIconContentDescription ?: stringResource(R.string.close)
                        TooltipBox(
                            positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                                TooltipAnchorPosition.Below
                            ),
                            tooltip = { PlainTooltip { Text(navDesc) } },
                            state = rememberTooltipState()
                        ) {
                            IconButton(
                                onClick = { handleClose() },
                                enabled = allowClose
                            ) {
                                Icon(
                                    imageVector = navigationIcon,
                                    contentDescription = navDesc
                                )
                            }
                        }
                    },
                    actions = {
                        actions()
                    },
                    colors = TopAppBarDefaults.topAppBarColors().copy(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                    )
                )
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ) { innerPadding ->
           content(innerPadding)

            if (dismissConfirmation && showDiscardConfirmation) {
                AlertDialog(
                    onDismissRequest = { showDiscardConfirmation = false },
                    title = { Text(stringResource(R.string.discard_changes)) },
                    text = { Text(stringResource(R.string.discard_changes_msg)) },
                    confirmButton = {
                        TextButton(onClick = {
                            showDiscardConfirmation = false
                            onClose()
                        }) {
                            Text(stringResource(R.string.discard_changes))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDiscardConfirmation = false }) {
                            Text(stringResource(R.string.cancel))
                        }
                    }
                )
            }
        }
    }
}