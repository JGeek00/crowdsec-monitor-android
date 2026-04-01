package com.jgeek00.crowdsecmonitor.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import uk.co.bocajsolutions.cardshape.Shape
import uk.co.bocajsolutions.cardshape.ShapeStyle

@Composable
fun RoundedCornersListTile(
    index: Int,
    totalItems: Int,
    content: @Composable () -> Unit,
) {
    Card(
        shape = Shape(groupSize = totalItems, index = index, style = ShapeStyle.Medium),
        modifier = Modifier.padding(bottom = if (index == totalItems - 1) 0.dp else 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
        ) {
            content()
        }
    }
}