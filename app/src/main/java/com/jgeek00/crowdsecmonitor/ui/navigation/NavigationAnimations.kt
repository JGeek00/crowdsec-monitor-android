package com.jgeek00.crowdsecmonitor.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally

internal const val NAV_ANIM_DURATION = 350
internal const val NAV_FADE_DURATION = 250
internal val NAV_EASING = FastOutSlowInEasing
internal const val NAV_SLIDE_RATIO = 0.10f

/**
 * Detail pane enters from the right — matches [AppNavGraph]'s enterTransition
 * (forward navigation: list → detail).
 */
internal val detailPaneEnterTransition: EnterTransition
    get() = slideInHorizontally(
        initialOffsetX = { (it * NAV_SLIDE_RATIO).toInt() },
        animationSpec = tween(NAV_ANIM_DURATION, easing = NAV_EASING)
    ) + fadeIn(tween(NAV_ANIM_DURATION, easing = NAV_EASING))

/**
 * Detail pane exits to the right — matches [AppNavGraph]'s popExitTransition
 * (back navigation: detail → list).
 */
internal val detailPaneExitTransition: ExitTransition
    get() = slideOutHorizontally(
        targetOffsetX = { (it * NAV_SLIDE_RATIO).toInt() },
        animationSpec = tween(NAV_ANIM_DURATION, easing = NAV_EASING)
    ) + fadeOut(tween(NAV_ANIM_DURATION, easing = NAV_EASING))

/**
 * List pane enters from the left — matches [AppNavGraph]'s popEnterTransition
 * (back navigation: detail → list).
 */
internal val listPaneEnterTransition: EnterTransition
    get() = slideInHorizontally(
        initialOffsetX = { -(it * NAV_SLIDE_RATIO).toInt() },
        animationSpec = tween(NAV_ANIM_DURATION, easing = NAV_EASING)
    ) + fadeIn(tween(NAV_ANIM_DURATION, easing = NAV_EASING))

/**
 * List pane exits to the left — matches [AppNavGraph]'s exitTransition
 * (forward navigation: list → detail).
 */
internal val listPaneExitTransition: ExitTransition
    get() = slideOutHorizontally(
        targetOffsetX = { -(it * NAV_SLIDE_RATIO).toInt() },
        animationSpec = tween(NAV_ANIM_DURATION, easing = NAV_EASING)
    ) + fadeOut(tween(NAV_ANIM_DURATION, easing = NAV_EASING))

