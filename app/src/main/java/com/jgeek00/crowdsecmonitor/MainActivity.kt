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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.jgeek00.crowdsecmonitor.constants.Enums
import com.jgeek00.crowdsecmonitor.ui.navigation.AppNavGraph
import com.jgeek00.crowdsecmonitor.ui.navigation.Route
import com.jgeek00.crowdsecmonitor.ui.navigation.topLevelRoutesNoServer
import com.jgeek00.crowdsecmonitor.ui.navigation.topLevelRoutesWithServer
import com.jgeek00.crowdsecmonitor.ui.screens.onboarding.OnboardingScreen
import com.jgeek00.crowdsecmonitor.ui.theme.CrowdSecMonitorTheme
import com.jgeek00.crowdsecmonitor.utils.readThemeMode
import com.jgeek00.crowdsecmonitor.utils.writeThemeMode
import com.jgeek00.crowdsecmonitor.viewmodel.OnboardingViewModel
import com.jgeek00.crowdsecmonitor.viewmodel.ServersManagerViewModel
import com.jgeek00.crowdsecmonitor.viewmodel.ServiceStatusViewModel
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
    serversManagerViewModel: ServersManagerViewModel = hiltViewModel(),
    serviceStatusViewModel: ServiceStatusViewModel = hiltViewModel(),
    onboardingViewModel: OnboardingViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStackEntry?.destination

    // Close/open WebSocket when the app goes to background/foreground
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (!serversManagerViewModel.hasServerConfigured) return@LifecycleEventObserver
            when (event) {
                Lifecycle.Event.ON_STOP -> serviceStatusViewModel.closeWebSocket()
                Lifecycle.Event.ON_START -> serviceStatusViewModel.openWebSocket()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val visibleTopLevelRoutes = if (serversManagerViewModel.isLoading || serversManagerViewModel.hasServerConfigured) {
        topLevelRoutesWithServer
    } else {
        topLevelRoutesNoServer
    }

    // Redirects automatically when tab is no longer visible (when a server is added or deleted)
    LaunchedEffect(serversManagerViewModel.hasServerConfigured, serversManagerViewModel.isLoading) {
        if (serversManagerViewModel.isLoading) return@LaunchedEffect
        if (currentDestination == null) return@LaunchedEffect  // NavHost not ready yet (initial startup)
        val isCurrentTabVisible = visibleTopLevelRoutes.any { tab ->
            currentDestination.hierarchy.any { it.hasRoute(tab.route::class) }
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
                    icon = { Icon(topLevel.icon, contentDescription = stringResource(topLevel.label)) },
                    label = { Text(stringResource(topLevel.label)) },
                    selected = currentDestination?.hierarchy?.any {
                        it.hasRoute(topLevel.route::class)
                    } == true,
                    onClick = {
                        val isAlreadySelected = currentDestination?.hierarchy?.any {
                            it.hasRoute(topLevel.route::class)
                        } == true
                        if (isAlreadySelected) {
                            val isAtRoot = currentDestination.hasRoute(topLevel.startRoute::class)
                            if (!isAtRoot) {
                                navController.popBackStack(topLevel.startRoute, inclusive = false)
                            }
                        } else {
                            navController.navigate(topLevel.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
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
                if (serversManagerViewModel.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else {
                    AppNavGraph(
                        navController = navController,
                        startDestination = if (serversManagerViewModel.hasServerConfigured) Route.DashboardGraph else Route.HomeGraph,
                        themeMode = themeMode,
                        onThemeModeChange = onThemeModeChange,
                        serversManagerViewModel = serversManagerViewModel,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }

    if (onboardingViewModel.showOnboarding) {
        OnboardingScreen(onFinish = { onboardingViewModel.finishOnboarding() })
    }
}
