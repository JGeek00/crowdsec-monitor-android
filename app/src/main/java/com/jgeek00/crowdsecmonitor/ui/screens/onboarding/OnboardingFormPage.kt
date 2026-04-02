package com.jgeek00.crowdsecmonitor.ui.screens.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jgeek00.crowdsecmonitor.R
import com.jgeek00.crowdsecmonitor.ui.components.connectionForm.ConnectionForm
import com.jgeek00.crowdsecmonitor.viewmodel.ConnectionFormViewModel
import kotlinx.coroutines.launch

@Composable
fun OnboardingFormPage(
    viewModel: ConnectionFormViewModel,
    onBack: () -> Unit,
    onFinish: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val navBarHeight = with(LocalDensity.current) {
        WindowInsets.navigationBars.getBottom(this).toDp()
    }

    Box(modifier = Modifier.fillMaxSize()) {

        ConnectionForm(
            viewModel = viewModel,
            showHeader = true,
            scrollState = scrollState,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 84.dp + navBarHeight)
        )

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 3.dp,
            shadowElevation = 0.dp
        ) {
            Column {
                HorizontalDivider()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(84.dp)
                        .padding(horizontal = 24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(
                        onClick = onBack,
                        enabled = !viewModel.connecting,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(text = stringResource(R.string.back))
                    }

                    Spacer(modifier = Modifier.size(16.dp))

                    Button(
                        onClick = {
                            scope.launch {
                                val result = viewModel.connect()
                                if (result) {
                                    onFinish()
                                }
                            }
                        },
                        enabled = !viewModel.connecting,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (viewModel.connecting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.5.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text(
                                text = stringResource(R.string.connect),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(navBarHeight))
            }
        }
    }
}
