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
import com.jgeek00.crowdsecmonitor.constants.Enums
import com.jgeek00.crowdsecmonitor.ui.screens.HomeScreen
import com.jgeek00.crowdsecmonitor.ui.screens.SettingsScreen
import com.jgeek00.crowdsecmonitor.ui.screens.settings.AppConfigurationScreen
import com.jgeek00.crowdsecmonitor.ui.screens.settings.ServerConfigurationScreen
import com.jgeek00.crowdsecmonitor.viewmodel.AuthViewModel

// Rutas raíz de cada grafo (las pantallas "landing" de cada tab)
private val rootRouteClasses = setOf(
    Route.Home::class,
    Route.Alerts::class,
    Route.Decisions::class,
    Route.Settings::class
)

private fun NavDestination.isRootRoute(): Boolean =
    rootRouteClasses.any { hasRoute(it) }

private const val ANIM_DURATION = 350
private const val FADE_DURATION = 250
private val easingSpec = FastOutSlowInEasing
// Material 3 shared axis: desplazamiento sutil del 10% del ancho
private const val SLIDE_RATIO = 0.10f

@Composable
fun AppNavGraph(
    navController: NavHostController,
    themeMode: Enums.ThemeMode,
    onThemeModeChange: (Enums.ThemeMode) -> Unit,
    authViewModel: AuthViewModel,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Route.Home,
        modifier = modifier,

        // ── Fade through entre tabs, Shared Axis X al profundizar ──
        enterTransition = {
            if (initialState.destination.isRootRoute() && targetState.destination.isRootRoute()) {
                // Cambio de tab: Material 3 Fade through
                fadeIn(tween(FADE_DURATION))
            } else {
                // Navegar hacia adelante: M3 Shared Axis X
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

        // ── Volver atrás: Shared Axis X invertido ──
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { -(it * SLIDE_RATIO).toInt() },
                animationSpec = tween(ANIM_DURATION, easing = easingSpec)
            ) + fadeIn(tween(ANIM_DURATION, easing = easingSpec))
        },
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { (it * SLIDE_RATIO).toInt() },
                animationSpec = tween(ANIM_DURATION, easing = easingSpec)
            ) + fadeOut(tween(ANIM_DURATION, easing = easingSpec))
        }
    ) {
        composable<Route.Home> {
            HomeScreen(authViewModel)
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

