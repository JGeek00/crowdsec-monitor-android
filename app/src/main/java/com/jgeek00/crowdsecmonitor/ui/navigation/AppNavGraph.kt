package com.jgeek00.crowdsecmonitor.ui.navigation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.toRoute
import com.jgeek00.crowdsecmonitor.constants.Enums
import com.jgeek00.crowdsecmonitor.ui.screens.DashboardScreen
import com.jgeek00.crowdsecmonitor.ui.screens.HomeScreen
import com.jgeek00.crowdsecmonitor.ui.screens.SettingsScreen
import com.jgeek00.crowdsecmonitor.ui.screens.dashboard.FullListDashboardScreen
import com.jgeek00.crowdsecmonitor.ui.screens.settings.AppConfigurationScreen
import com.jgeek00.crowdsecmonitor.ui.screens.settings.ServerConfigurationScreen
import com.jgeek00.crowdsecmonitor.viewmodel.AuthViewModel

private val rootRouteClasses = setOf(
    Route.Home::class,
    Route.Dashboard::class,
    Route.Alerts::class,
    Route.Decisions::class,
    Route.Settings::class
)

private fun NavDestination.isRootRoute(): Boolean =
    rootRouteClasses.any { hasRoute(it) }

private const val ANIM_DURATION = 350
private const val FADE_DURATION = 250
private val easingSpec = FastOutSlowInEasing
private const val SLIDE_RATIO = 0.10f

@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: Route = Route.Home,
    themeMode: Enums.ThemeMode,
    onThemeModeChange: (Enums.ThemeMode) -> Unit,
    authViewModel: AuthViewModel,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,

        enterTransition = {
            if (initialState.destination.isRootRoute() && targetState.destination.isRootRoute()) {
                fadeIn(tween(FADE_DURATION))
            } else {
                slideInHorizontally(
                    initialOffsetX = { (it * SLIDE_RATIO).toInt() },
                    animationSpec = tween(ANIM_DURATION, easing = easingSpec)
                ) + fadeIn(tween(ANIM_DURATION, easing = easingSpec))
            }
        },
        exitTransition = {
            if (initialState.destination.isRootRoute() && targetState.destination.isRootRoute()) {
                fadeOut(tween(FADE_DURATION))
            } else {
                slideOutHorizontally(
                    targetOffsetX = { -(it * SLIDE_RATIO).toInt() },
                    animationSpec = tween(ANIM_DURATION, easing = easingSpec)
                ) + fadeOut(tween(ANIM_DURATION, easing = easingSpec))
            }
        },
        popEnterTransition = {
            if (initialState.destination.isRootRoute() && targetState.destination.isRootRoute()) {
                fadeIn(tween(FADE_DURATION))
            } else {
                slideInHorizontally(
                    initialOffsetX = { -(it * SLIDE_RATIO).toInt() },
                    animationSpec = tween(ANIM_DURATION, easing = easingSpec)
                ) + fadeIn(tween(ANIM_DURATION, easing = easingSpec))
            }
        },
        popExitTransition = {
            if (initialState.destination.isRootRoute() && targetState.destination.isRootRoute()) {
                fadeOut(tween(FADE_DURATION))
            } else {
                slideOutHorizontally(
                    targetOffsetX = { (it * SLIDE_RATIO).toInt() },
                    animationSpec = tween(ANIM_DURATION, easing = easingSpec)
                ) + fadeOut(tween(ANIM_DURATION, easing = easingSpec))
            }
        }
    ) {
        composable<Route.Home> {
            HomeScreen(authViewModel)
        }

        composable<Route.Dashboard> {
            DashboardScreen(
                onNavigateToFullList = { type ->
                    navController.navigate(Route.FullListDashboard(type.name))
                }
            )
        }

        composable<Route.FullListDashboard> { backStackEntry ->
            val route = backStackEntry.toRoute<Route.FullListDashboard>()
            FullListDashboardScreen(
                itemType = Enums.DashboardItemType.valueOf(route.itemType),
                onBack = { navController.popBackStack() }
            )
        }

        composable<Route.Alerts> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Alerts Screen")
            }
        }

        composable<Route.Decisions> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Decisions Screen")
            }
        }

        navigation<Route.SettingsGraph>(startDestination = Route.Settings) {
            composable<Route.Settings> {
                SettingsScreen(
                    themeMode = themeMode,
                    onThemeModeChange = onThemeModeChange,
                    onNavigateToAppConfiguration = {
                        navController.navigate(Route.AppConfiguration)
                    },
                    onNavigateToServerConfiguration = {
                        navController.navigate(Route.ServerConfiguration)
                    }
                )
            }
            composable<Route.AppConfiguration> {
                AppConfigurationScreen(onBack = { navController.popBackStack() })
            }
            composable<Route.ServerConfiguration> {
                ServerConfigurationScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
