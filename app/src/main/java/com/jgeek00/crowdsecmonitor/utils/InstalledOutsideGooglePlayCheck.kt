package com.jgeek00.crowdsecmonitor.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
fun rememberIsInstalledFromOutsideGooglePlay(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        runCatching {
            val installerPackageName =
                context.packageManager.getInstallSourceInfo(context.packageName).installingPackageName
            installerPackageName != "com.android.vending"
        }.getOrDefault(true)
    }
}
