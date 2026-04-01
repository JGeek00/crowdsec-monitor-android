package com.jgeek00.crowdsecmonitor.ui.screens.decisions.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.Extension
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jgeek00.crowdsecmonitor.R

@Composable
fun DecisionTypeChip(decisionType: String) {
    data class ChipStyle(val labelRes: Int?, val rawLabel: String, val color: Color, val icon: ImageVector)

    val style = remember(decisionType) {
        when (decisionType.lowercase()) {
            "ban" -> ChipStyle(R.string.ban, "Ban", Color(0xFFE53935), Icons.Rounded.Block)
            "captcha" -> ChipStyle(R.string.captcha, "Captcha", Color(0xFFF57C00), Icons.Rounded.Extension)
            else -> ChipStyle(null, decisionType.replaceFirstChar { it.uppercase() }, Color(0xFF1E88E5), Icons.Rounded.Security)
        }
    }

    Surface(
        color = style.color.copy(alpha = 0.12f),
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = style.icon,
                contentDescription = null,
                tint = style.color,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = if (style.labelRes != null) stringResource(style.labelRes) else style.rawLabel,
                color = style.color,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp
            )
        }
    }
}
