package com.jgeek00.crowdsecmonitor.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.jgeek00.crowdsecmonitor.ui.components.CreateServerSheet
import com.jgeek00.crowdsecmonitor.ui.theme.CrowdSecMonitorTheme
import com.jgeek00.crowdsecmonitor.viewmodel.AuthViewModel

@Composable
fun HomeScreen(authViewModel: AuthViewModel) {
    if (!authViewModel.hasServerConfigured) {
        NoServerConfiguredContent()
    } else {
        Text("Dashboard Content - Server: ${authViewModel.currentServer?.name}")
    }
}

@Composable
fun NoServerConfiguredContent() {
    var showSheet by remember { mutableStateOf(false) }

    if (showSheet) {
        Dialog(
            onDismissRequest = { showSheet = false },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {
            CrowdSecMonitorTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CreateServerSheet(onClose = { showSheet = false })
                }
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No servers configured",
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = "Add a server to start monitoring your CrowdSec instance.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = { showSheet = true }) {
            Text("Add Server")
        }
    }
}
