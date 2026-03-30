package com.jgeek00.crowdsecmonitor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.jgeek00.crowdsecmonitor.constants.Enums
import com.jgeek00.crowdsecmonitor.ui.navigation.AppNavGraph
import com.jgeek00.crowdsecmonitor.ui.navigation.Route
import com.jgeek00.crowdsecmonitor.ui.navigation.topLevelRoutesNoServer
import com.jgeek00.crowdsecmonitor.ui.navigation.topLevelRoutesWithServer
import com.jgeek00.crowdsecmonitor.ui.theme.CrowdSecMonitorTheme
import com.jgeek00.crowdsecmonitor.utils.readThemeMode
import com.jgeek00.crowdsecmonitor.utils.writeThemeMode
import com.jgeek00.crowdsecmonitor.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val sharedPreferences = getSharedPreferences(packageName, MODE_PRIVATE)
        val initialThemeMode = sharedPreferences.readThemeMode()

        setContent {
            var themeMode by rememberSaveable { mutableStateOf(initialThemeMode) }
            val darkTheme = when (themeMode) {
                Enums.ThemeMode.SYSTEM -> isSystemInDarkTheme()
                Enums.ThemeMode.LIGHT -> false
                Enums.ThemeMode.DARK -> true
            }

            CrowdSecMonitorTheme(darkTheme = darkTheme) {
                CrowdSecMonitorApp(
                    themeMode = themeMode,
                    onThemeModeChange = {
                        themeMode = it
                        sharedPreferences.writeThemeMode(it)
                    }
                )
            }
        }
    }
}

@Composable
fun CrowdSecMonitorApp(
    themeMode: Enums.ThemeMode,
    onThemeModeChange: (Enums.ThemeMode) -> Unit,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStackEntry?.destination

    val visibleTopLevelRoutes = if (authViewModel.hasServerConfigured) {
        topLevelRoutesWithServer
    } else {
        topLevelRoutesNoServer
    }

    // Redirige automáticamente cuando el tab activo deja de ser visible
    // (p.ej.: Home al configurar un servidor, Dashboard al eliminarlo)
    LaunchedEffect(authViewModel.hasServerConfigured) {
        if (authViewModel.isLoading) return@LaunchedEffect
        val isCurrentTabVisible = visibleTopLevelRoutes.any { tab ->
            currentDestination?.hierarchy?.any { it.hasRoute(tab.route::class) } == true
        }
        if (!isCurrentTabVisible) {
            navController.navigate(visibleTopLevelRoutes.first().route) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            visibleTopLevelRoutes.forEach { topLevel ->
                item(
                    icon = { Icon(topLevel.icon, contentDescription = topLevel.label) },
                    label = { Text(topLevel.label) },
                    selected = currentDestination?.hierarchy?.any {
                        it.hasRoute(topLevel.route::class)
                    } == true,
                    onClick = {
                        navController.navigate(topLevel.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                if (authViewModel.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else {
                    AppNavGraph(
                        navController = navController,
                        startDestination = if (authViewModel.hasServerConfigured) Route.Dashboard else Route.Home,
                        themeMode = themeMode,
                        onThemeModeChange = onThemeModeChange,
                        authViewModel = authViewModel,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}
