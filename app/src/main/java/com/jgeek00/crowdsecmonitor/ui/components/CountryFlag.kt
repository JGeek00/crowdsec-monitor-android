package com.jgeek00.crowdsecmonitor.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale


@Composable
fun CountryFlag(
    countryCode: String,
    onlyFlag: Boolean = false,
    fontSize: Int = 14
) {
    val flag = remember(countryCode) { countryCode.toFlagEmoji() }
    val name = remember(countryCode) {
        Locale("", countryCode.uppercase()).getDisplayCountry(Locale.getDefault())
            .ifBlank { countryCode.uppercase() }
    }

    Row(verticalAlignment = Alignment.Companion.CenterVertically) {
        Text(text = flag, fontSize = fontSize.sp)
        if (!onlyFlag) {
            Spacer(modifier = Modifier.Companion.width(8.dp))
            Text(text = name, fontSize = fontSize.sp)
        }
    }
}

fun String.toFlagEmoji(): String {
    val base = 0x1F1E6 - 'A'.code
    return this.uppercase().map { char ->
        String(Character.toChars(base + char.code))
    }.joinToString("")
}