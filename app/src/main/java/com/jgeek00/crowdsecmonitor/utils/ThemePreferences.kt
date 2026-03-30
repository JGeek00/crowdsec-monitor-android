package com.jgeek00.crowdsecmonitor.utils

import android.content.SharedPreferences
import com.jgeek00.crowdsecmonitor.constants.Enums
import com.jgeek00.crowdsecmonitor.constants.StorageKeys

fun SharedPreferences.readThemeMode(): Enums.ThemeMode {
    val storedValue = getString(StorageKeys.THEME, null) ?: return Enums.ThemeMode.SYSTEM
    return runCatching { Enums.ThemeMode.valueOf(storedValue) }
        .getOrDefault(Enums.ThemeMode.SYSTEM)
}

fun SharedPreferences.writeThemeMode(themeMode: Enums.ThemeMode) {
    edit().putString(StorageKeys.THEME, themeMode.name).apply()
}

