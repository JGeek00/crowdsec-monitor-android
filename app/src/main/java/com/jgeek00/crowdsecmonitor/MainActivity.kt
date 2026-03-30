package com.jgeek00.crowdsecmonitor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.jgeek00.crowdsecmonitor.ui.AppDestinations
import com.jgeek00.crowdsecmonitor.ui.screens.HomeScreen
import com.jgeek00.crowdsecmonitor.ui.theme.CrowdSecMonitorTheme
import com.jgeek00.crowdsecmonitor.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CrowdSecMonitorTheme {
                CrowdSecMonitorApp()
            }
        }
    }
}

@Composable
fun CrowdSecMonitorApp(
    authViewModel: AuthViewModel = hiltViewModel()
) {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }

    val visibleDestinations = if (!authViewModel.hasServerConfigured) {
        listOf(AppDestinations.HOME, AppDestinations.SETTINGS)
    } else {
        AppDestinations.entries.toList()
    }

    if (currentDestination !in visibleDestinations) {
        currentDestination = AppDestinations.HOME
    }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            visibleDestinations.forEach {
                item(
                    icon = {
                        Icon(
                            it.icon,
                            contentDescription = it.label
                        )
                    },
                    label = { Text(it.label) },
                    selected = it == currentDestination,
                    onClick = { currentDestination = it }
                )
            }
        }
    ) {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                if (authViewModel.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else {
                    ScreenContent(currentDestination, authViewModel)
                }
            }
        }
    }
}

@Composable
fun ScreenContent(destination: AppDestinations, authViewModel: AuthViewModel) {
    when (destination) {
        AppDestinations.HOME -> HomeScreen(authViewModel)
        AppDestinations.ALERTS -> Text("Alerts Screen")
        AppDestinations.DECISIONS -> Text("Decisions Screen")
        AppDestinations.SETTINGS -> Text("Settings Screen")
    }
}
