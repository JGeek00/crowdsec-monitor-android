package com.jgeek00.crowdsecmonitor.ui.screens.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jgeek00.crowdsecmonitor.R

@Composable
fun WelcomePage(onNext: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp)
                .padding(top = 64.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Surface(
                modifier = Modifier.size(120.dp),
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                shadowElevation = 8.dp,
                tonalElevation = 6.dp
            ) {
                Icon(
                    imageVector = Icons.Rounded.Security,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(24.dp)
                        .size(72.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = stringResource(R.string.onboarding_welcome_title),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                fontSize = 34.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.onboarding_welcome_subtitle),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium
            )
        }

        Button(
            onClick = onNext,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
                .padding(horizontal = 32.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 16.dp)
        ) {
            Text(
                text = stringResource(R.string.onboarding_get_started),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}


