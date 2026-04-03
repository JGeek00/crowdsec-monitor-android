package com.jgeek00.crowdsecmonitor.ui.screens.onboarding

import android.graphics.Color
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.jgeek00.crowdsecmonitor.viewmodel.ConnectionFormViewModel
import com.jgeek00.crowdsecmonitor.ui.theme.LocalDarkTheme
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    onFinish: () -> Unit,
    connectionFormViewModel: ConnectionFormViewModel = hiltViewModel(key = "onboarding")
) {
    val darkTheme = LocalDarkTheme.current
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { 3 })

    fun navigateTo(page: Int) {
        scope.launch { pagerState.animateScrollToPage(page) }
    }

    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            decorFitsSystemWindows = false
        )
    ) {
        BackHandler {
            when (pagerState.currentPage) {
                1 -> navigateTo(0)
                2 -> navigateTo(1)
            }
        }

        val dialogWindow = (LocalView.current.parent as? DialogWindowProvider)?.window
        SideEffect {
            dialogWindow?.let { window ->
                WindowCompat.setDecorFitsSystemWindows(window, false)
                val controller = WindowInsetsControllerCompat(window, window.decorView)
                controller.isAppearanceLightStatusBars = !darkTheme
                controller.isAppearanceLightNavigationBars = !darkTheme
                @Suppress("DEPRECATION")
                window.statusBarColor = Color.TRANSPARENT
                @Suppress("DEPRECATION")
                window.navigationBarColor = Color.TRANSPARENT
            }
        }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.surface,
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { innerPadding ->
            HorizontalPager(
                state = pagerState,
                userScrollEnabled = false,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = innerPadding.calculateTopPadding())
            ) { page ->
                when (page) {
                    0 -> WelcomePage(onNext = { navigateTo(1) })
                    1 -> ApiInformationPage(
                        onBack = { navigateTo(0) },
                        onNext = { navigateTo(2) }
                    )
                    2 -> OnboardingFormPage(
                        viewModel = connectionFormViewModel,
                        onBack = { navigateTo(1) },
                        onFinish = onFinish
                    )
                }
            }
        }
    }
}

