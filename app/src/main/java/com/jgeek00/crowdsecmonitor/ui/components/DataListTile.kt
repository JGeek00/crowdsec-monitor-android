package com.jgeek00.crowdsecmonitor.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uk.co.bocajsolutions.cardshape.Shape
import uk.co.bocajsolutions.cardshape.ShapeStyle

@Composable
fun DataListTile(
    tileIndex: Int = 0,
    groupTiles: Int = 1,
    title: String,
    subtitle: String? = null,
    subtitleComponent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = {},
) {
    Card(
        shape = Shape(groupSize = groupTiles, index = tileIndex, style = ShapeStyle.Medium),
        modifier = Modifier.padding(bottom = if (tileIndex == groupTiles - 1) 0.dp else 2.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    title,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp
                )
                if (subtitle != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        subtitle,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp,
                    )
                }
                if (subtitleComponent != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    subtitleComponent()
                }
            }
            if (trailingContent != null) {
                trailingContent()
            }
        }
    }
}