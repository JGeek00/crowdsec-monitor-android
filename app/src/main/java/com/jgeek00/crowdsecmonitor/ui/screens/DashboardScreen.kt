package com.jgeek00.crowdsecmonitor.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.jgeek00.crowdsecmonitor.viewmodel.AuthViewModel

@Composable
fun DashboardScreen(authViewModel: AuthViewModel) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Dashboard — ${authViewModel.currentServer?.name}")
    }
}

