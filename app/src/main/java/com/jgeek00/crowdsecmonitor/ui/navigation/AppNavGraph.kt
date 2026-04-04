package com.jgeek00.crowdsecmonitor.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.toRoute
import com.jgeek00.crowdsecmonitor.constants.Enums
import com.jgeek00.crowdsecmonitor.ui.screens.alerts.list.AlertsListScreen
import com.jgeek00.crowdsecmonitor.ui.screens.decisions.list.DecisionsListScreen
import com.jgeek00.crowdsecmonitor.ui.screens.lists.ListsScreen
import com.jgeek00.crowdsecmonitor.ui.screens.dashboard.DashboardScreen
import com.jgeek00.crowdsecmonitor.ui.screens.settings.SettingsScreen
import com.jgeek00.crowdsecmonitor.ui.screens.dashboard.FullListDashboardScreen
import com.jgeek00.crowdsecmonitor.ui.screens.noServer.NoServerScreen
import com.jgeek00.crowdsecmonitor.ui.screens.settings.AppConfigurationScreen
import com.jgeek00.crowdsecmonitor.ui.screens.settings.ServerConfigurationScreen
import com.jgeek00.crowdsecmonitor.viewmodel.AuthViewModel

private fun NavDestination.topLevelAncestorRoute(): String? {
    var dest: NavDestination = this
    while (dest.parent?.parent != null) {
        dest = dest.parent!!
    }
    return dest.route
}

@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: Route = Route.HomeGraph,
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
            if (initialState.destination.topLevelAncestorRoute() != targetState.destination.topLevelAncestorRoute()) {
                fadeIn(tween(NAV_FADE_DURATION))
            } else {
                detailPaneEnterTransition
            }
        },
        exitTransition = {
            if (initialState.destination.topLevelAncestorRoute() != targetState.destination.topLevelAncestorRoute()) {
                fadeOut(tween(NAV_FADE_DURATION))
            } else {
                listPaneExitTransition
            }
        },
        popEnterTransition = {
            if (initialState.destination.topLevelAncestorRoute() != targetState.destination.topLevelAncestorRoute()) {
                fadeIn(tween(NAV_FADE_DURATION))
            } else {
                listPaneEnterTransition
            }
        },
        popExitTransition = {
            if (initialState.destination.topLevelAncestorRoute() != targetState.destination.topLevelAncestorRoute()) {
                fadeOut(tween(NAV_FADE_DURATION))
            } else {
                detailPaneExitTransition
            }
        }
    ) {
        navigation<Route.HomeGraph>(startDestination = Route.Home) {
            composable<Route.Home> {
                NoServerScreen(authViewModel)
            }
        }

        navigation<Route.DashboardGraph>(startDestination = Route.Dashboard) {
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
        }

        navigation<Route.AlertsGraph>(startDestination = Route.Alerts) {
            composable<Route.Alerts> {
                AlertsListScreen()
            }
        }

        navigation<Route.DecisionsGraph>(startDestination = Route.Decisions) {
            composable<Route.Decisions> {
                DecisionsListScreen()
            }
        }

        navigation<Route.ListsGraph>(startDestination = Route.Lists) {
            composable<Route.Lists> {
                ListsScreen()
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
