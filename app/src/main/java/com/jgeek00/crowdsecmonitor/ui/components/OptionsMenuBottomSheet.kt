package com.jgeek00.crowdsecmonitor.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

enum class OptionsMenuBottomSheetItemRole {
    DESTRUCTIVE,
    POSITIVE,
    DEFAULT
}

data class OptionsMenuBottomSheetItem(
    val title: String,
    val subtitle: String? = null,
    val icon: ImageVector,
    val onClick: () -> Unit,
    val role: OptionsMenuBottomSheetItemRole = OptionsMenuBottomSheetItemRole.DEFAULT
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OptionsMenuBottomSheet(
    options: List<OptionsMenuBottomSheetItem>,
    showMenu: Boolean,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    if (showMenu) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState
        ) {
            options.forEach { option ->
                ListItem(
                    headlineContent = {
                        Text(
                            option.title,
                            color = when (option.role) {
                                OptionsMenuBottomSheetItemRole.DESTRUCTIVE -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.onSurface
                            }
                        )
                    },
                    supportingContent = option.subtitle?.let { subtitle -> { Text(subtitle) } },
                    leadingContent = {
                        Icon(
                            imageVector = option.icon,
                            contentDescription = option.title,
                            tint = when (option.role) {
                                OptionsMenuBottomSheetItemRole.DESTRUCTIVE -> MaterialTheme.colorScheme.error
                                else -> {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            }
                        )
                    },
                    modifier = Modifier.clickable {
                        option.onClick()
                        onDismiss()
                    }
                )
            }
            Spacer(modifier = Modifier.navigationBarsPadding())
        }
    }
}