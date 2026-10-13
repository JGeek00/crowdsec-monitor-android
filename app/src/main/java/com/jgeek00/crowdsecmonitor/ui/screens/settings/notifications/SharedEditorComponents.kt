package com.jgeek00.crowdsecmonitor.ui.screens.settings.notifications

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.res.stringResource
import com.jgeek00.crowdsecmonitor.R
import com.jgeek00.crowdsecmonitor.viewmodel.NotificationFormViewModel

/**
 * Error alert shared by the wizards and the details edit forms (same copy the
 * wizards render for save/load failures).
 */
@Composable
fun EditorErrorAlert(message: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.error)) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.ok))
            }
        }
    )
}

/**
 * Keyboard staging feed for the rule editor: reports IME visibility changes to
 * the form ViewModel, which stages the atomic rule dialog until the keyboard
 * has hidden plus a settle delay.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RuleEditorKeyboardStaging(viewModel: NotificationFormViewModel) {
    val imeVisible = WindowInsets.isImeVisible
    LaunchedEffect(imeVisible) {
        if (imeVisible) {
            viewModel.keyboardWillShow()
        } else {
            viewModel.keyboardDidHide()
        }
    }
}
