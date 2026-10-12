package com.jgeek00.crowdsecmonitor.ui.screens.settings.notifications

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.jgeek00.crowdsecmonitor.R

/**
 * Progress capsules for wizard pagers, mirroring the iOS WizardStepIndicator:
 * completed and current steps are filled, current one is wider.
 */
@Composable
fun WizardStepIndicator(
    totalSteps: Int,
    currentStep: Int,
    modifier: Modifier = Modifier
) {
    val stepDescription = stringResource(R.string.step_of_fmt, currentStep + 1, totalSteps)
    Row(
        modifier = modifier
            .semantics { contentDescription = stepDescription },
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalSteps) { index ->
            val isCurrent = index == currentStep
            val width by animateDpAsState(
                targetValue = if (isCurrent) 28.dp else 12.dp,
                label = "StepCapsuleWidth"
            )
            val color by animateColorAsState(
                targetValue = if (index <= currentStep) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                },
                label = "StepCapsuleColor"
            )
            Box(
                modifier = Modifier
                    .width(width)
                    .height(6.dp)
                    .background(color, RoundedCornerShape(percent = 50))
            )
        }
    }
}
