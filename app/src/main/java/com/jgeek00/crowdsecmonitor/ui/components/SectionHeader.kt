package com.jgeek00.crowdsecmonitor.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun SectionHeader(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.titleSmall,
    fontWeight: FontWeight = FontWeight.SemiBold,
    color: Color = MaterialTheme.colorScheme.primary,
    smallTopPadding: Boolean = false
) {
    Text(
        text = text,
        style = style,
        fontWeight = fontWeight,
        color = color,
        modifier = modifier.padding(top = if (smallTopPadding) 12.dp else 24.dp, bottom = 12.dp)
    )
}