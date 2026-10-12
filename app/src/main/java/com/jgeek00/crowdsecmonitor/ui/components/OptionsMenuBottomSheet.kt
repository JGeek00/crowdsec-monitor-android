package com.jgeek00.crowdsecmonitor.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

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
    val role: OptionsMenuBottomSheetItemRole = OptionsMenuBottomSheetItemRole.DEFAULT,
    val disabled: Boolean = false,
)

/**
 * Renders one flat list of options, or a list of sections when [sections] is provided.
 * Options within a section share the grouped rounded-corner shape; sections are
 * visually separated by spacing so that destructive actions (e.g. delete) can be
 * isolated from regular actions (e.g. toggle).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OptionsMenuBottomSheet(
    options: List<OptionsMenuBottomSheetItem>,
    showMenu: Boolean,
    onDismiss: () -> Unit,
    sections: List<List<OptionsMenuBottomSheetItem>> = emptyList()
) {
    val sheetState = rememberModalBottomSheetState()

    if (showMenu) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .size(width = 40.dp, height = 4.dp)
                        .background(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            shape = MaterialTheme.shapes.small
                        )
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
            ) {
                val effectiveSections = if (sections.isNotEmpty()) sections else listOf(options)
                effectiveSections.forEachIndexed { sectionIndex, section ->
                    if (sectionIndex > 0) {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    section.forEach { option ->
                    RoundedCornersListTile(
                        index = section.indexOf(option),
                        totalItems = section.size,
                        onClick = {
                            if (!option.disabled) {
                                option.onClick()
                                onDismiss()
                            }
                        },
                        enabled = !option.disabled
                    ) {
                        ListItemContent(
                            headlineText = option.title,
                            subHeadlineText = option.subtitle,
                            leadingContent = {
                                Icon(
                                    imageVector = option.icon,
                                    contentDescription = option.title,
                                    tint = if (!option.disabled) {
                                        when (option.role) {
                                            OptionsMenuBottomSheetItemRole.DESTRUCTIVE -> MaterialTheme.colorScheme.error
                                            else -> {
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                            }
                                        }
                                    }
                                    else {
                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                    }
                                )
                            },
                            color = if (!option.disabled) {
                                when (option.role) {
                                    OptionsMenuBottomSheetItemRole.DESTRUCTIVE -> MaterialTheme.colorScheme.error
                                    else -> {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                }
                            }
                            else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            }
                        )
                    }
                    }
                }
                Spacer(modifier = Modifier.navigationBarsPadding())
            }
        }
    }
}