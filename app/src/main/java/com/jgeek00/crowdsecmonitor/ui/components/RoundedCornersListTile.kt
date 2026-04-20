package com.jgeek00.crowdsecmonitor.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jgeek00.crowdsecmonitor.ui.theme.LocalDarkTheme


// Code from https://github.com/bocajthomas/ExpressiveCardShape

private sealed class ShapeStyle(
    val topRadius: Dp,
    val bottomRadius: Dp
) {
    data object Default : ShapeStyle(topRadius = 15.dp, bottomRadius = 5.dp)
    data object Selected : ShapeStyle(topRadius = 15.dp, bottomRadius = 15.dp)
}

private fun generateShape(
    groupSize: Int,
    index: Int,
    style: ShapeStyle = ShapeStyle.Default
): RoundedCornerShape {
    require(groupSize >= 1) { "groupSize must be 1 or greater." }
    require(index in 0 until groupSize) { "index must be within the group bounds [0, $groupSize)." }

    val large = style.topRadius
    val small = style.bottomRadius

    val cardShapeSingle = RoundedCornerShape(large)
    val cardShapeGroupedTop = RoundedCornerShape(
        topStart = large,
        topEnd = large,
        bottomEnd = small,
        bottomStart = small
    )
    val cardShapeGroupedMiddle = RoundedCornerShape(small)
    val cardShapeGroupedBottom = RoundedCornerShape(
        topStart = small,
        topEnd = small,
        bottomEnd = large,
        bottomStart = large
    )

    return when (groupSize) {
        1 -> cardShapeSingle
        else -> {
            when (index) {
                0 -> cardShapeGroupedTop
                groupSize - 1 -> cardShapeGroupedBottom
                else -> cardShapeGroupedMiddle
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun RoundedCornersListTile(
    index: Int,
    totalItems: Int,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    selected: Boolean = false,
    customBackground: Color? = null,
    content: @Composable () -> Unit,
) {
    val defaultShape = generateShape(groupSize = totalItems, index = index)
    val selectedShape = generateShape(groupSize = totalItems, index = index, style = ShapeStyle.Selected)
    val shapes = ListItemShapes(
        shape = defaultShape,
        selectedShape = selectedShape,
        pressedShape = selectedShape,
        focusedShape = selectedShape,
        hoveredShape = selectedShape,
        draggedShape = selectedShape,
    )

    val containerColor = customBackground
        ?: if (LocalDarkTheme.current)
            MaterialTheme.colorScheme.surfaceContainerHigh
        else
            MaterialTheme.colorScheme.surface

    if (onClick != null) {
        SegmentedListItem(
            onClick = onClick,
            onLongClick = onLongClick,
            shapes = shapes,
            colors = ListItemColors(
                containerColor = containerColor,
                contentColor = MaterialTheme.colorScheme.onSurface,
                leadingContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                trailingContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                overlineContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                supportingContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledContainerColor = MaterialTheme.colorScheme.onSurface,
                disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                disabledLeadingContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                disabledTrailingContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                disabledOverlineContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                disabledSupportingContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                selectedContentColor = MaterialTheme.colorScheme.onSurface,
                selectedLeadingContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                selectedTrailingContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                selectedOverlineContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                selectedSupportingContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                draggedContainerColor = MaterialTheme.colorScheme.onSurface,
                draggedContentColor = MaterialTheme.colorScheme.onSurface,
                draggedLeadingContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                draggedTrailingContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                draggedOverlineContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                draggedSupportingContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
            enabled = enabled,
            selected = selected,
            modifier = Modifier
                .padding(bottom = if (index == totalItems - 1) 0.dp else 2.dp)
        ) {
            content()
        }
    } else {
        Card(
            shape = defaultShape,
            colors = CardDefaults.cardColors(
                containerColor = containerColor
            ),
            modifier = Modifier
                .padding(bottom = if (index == totalItems - 1) 0.dp else 2.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
                content()
            }
        }
    }
}