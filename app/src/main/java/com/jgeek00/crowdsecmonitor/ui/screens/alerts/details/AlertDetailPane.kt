package com.jgeek00.crowdsecmonitor.ui.screens.alerts.details

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.jgeek00.crowdsecmonitor.R
import com.jgeek00.crowdsecmonitor.ui.screens.decisions.details.DecisionDetailsScreen
import kotlinx.serialization.Serializable

@Serializable private data object AlertDetailRoot
@Serializable private data class AlertDetailDecisionRoute(val decisionId: Int)

@Composable
fun AlertDetailPane(
    alertId: Int?,
    showBackButton: Boolean,
    onNavigateBack: () -> Unit
) {
    if (alertId != null) {
        key(alertId) {
            val detailNavController = rememberNavController()
            NavHost(
                navController = detailNavController,
                startDestination = AlertDetailRoot,
                enterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { (it * 0.10f).toInt() },
                        animationSpec = tween(350, easing = FastOutSlowInEasing)
                    ) + fadeIn(tween(350, easing = FastOutSlowInEasing))
                },
                exitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { -(it * 0.10f).toInt() },
                        animationSpec = tween(350, easing = FastOutSlowInEasing)
                    ) + fadeOut(tween(350, easing = FastOutSlowInEasing))
                },
                popEnterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { -(it * 0.10f).toInt() },
                        animationSpec = tween(350, easing = FastOutSlowInEasing)
                    ) + fadeIn(tween(350, easing = FastOutSlowInEasing))
                },
                popExitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { (it * 0.10f).toInt() },
                        animationSpec = tween(350, easing = FastOutSlowInEasing)
                    ) + fadeOut(tween(350, easing = FastOutSlowInEasing))
                }
            ) {
                composable<AlertDetailRoot> {
                    AlertDetailsScreen(
                        alertId = alertId,
                        showBackButton = showBackButton,
                        onNavigateBack = onNavigateBack,
                        onNavigateToDecision = { decisionId ->
                            detailNavController.navigate(AlertDetailDecisionRoute(decisionId))
                        }
                    )
                }
                composable<AlertDetailDecisionRoute> { backStackEntry ->
                    val route = backStackEntry.toRoute<AlertDetailDecisionRoute>()
                    DecisionDetailsScreen(
                        decisionId = route.decisionId,
                        showBackButton = true,
                        onNavigateBack = { detailNavController.popBackStack() },
                        onNavigateToAlert = null
                    )
                }
            }
        }
    } else {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.FilterList,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = stringResource(R.string.select_alert),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}