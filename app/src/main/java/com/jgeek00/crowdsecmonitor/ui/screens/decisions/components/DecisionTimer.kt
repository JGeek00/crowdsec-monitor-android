package com.jgeek00.crowdsecmonitor.ui.screens.decisions.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.TimerOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jgeek00.crowdsecmonitor.R
import com.jgeek00.crowdsecmonitor.extensions.toInstant
import kotlinx.coroutines.delay
import java.time.Instant

private data class RemainingTime(val days: Long, val hours: Long, val minutes: Long, val seconds: Long)

private fun computeRemaining(expirationInstant: Instant): RemainingTime? {
    val diff = expirationInstant.epochSecond - Instant.now().epochSecond
    if (diff < 1) return null
    return RemainingTime(
        days = diff / 86400,
        hours = (diff % 86400) / 3600,
        minutes = (diff % 3600) / 60,
        seconds = diff % 60
    )
}

@Composable
fun DecisionTimer(expiration: String) {
    val expirationInstant = remember(expiration) { expiration.toInstant() }
    var remaining by remember { mutableStateOf(expirationInstant?.let { computeRemaining(it) }) }

    LaunchedEffect(expiration) {
        while (true) {
            remaining = expirationInstant?.let { computeRemaining(it) }
            delay(1_000)
        }
    }

    val isExpired = remaining == null
    val color = if (isExpired) Color.Gray else Color(0xFF43A047)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = if (isExpired) Icons.Rounded.TimerOff else Icons.Rounded.Schedule,
            contentDescription = null,
            tint = color,
            modifier = Modifier
                .size(16.dp)
        )
        Spacer(Modifier.width(0.dp))
        if (isExpired) {
            Text(
                text = stringResource(R.string.expired),
                color = color,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp
            )
        } else {
            val r = remaining!!
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                if (r.days > 0) TimerUnit("${r.days}d", color)
                if (r.hours > 0) TimerUnit("${r.hours}h", color)
                if (r.minutes > 0) TimerUnit("${r.minutes}m", color)
                if (r.seconds > 0 || (r.days == 0L && r.hours == 0L && r.minutes == 0L)) {
                    TimerUnit("${r.seconds}s", color)
                }
            }
        }
    }
}

@Composable
private fun TimerUnit(text: String, color: Color) {
    AnimatedContent(
        targetState = text,
        transitionSpec = {
            slideInVertically { it } togetherWith slideOutVertically { -it }
        },
        label = "TimerUnit"
    ) { value ->
        Text(
            text = value,
            color = color,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp
        )
    }
}

