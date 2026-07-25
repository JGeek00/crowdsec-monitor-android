package com.jgeek00.crowdsecmonitor.viewmodel

import android.content.Context
import android.content.SharedPreferences
import com.jgeek00.crowdsecmonitor.constants.StorageKeys
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

class SharedPreferencesOnboardingStorage @Inject constructor(
    @ApplicationContext context: Context
) : OnboardingStorage {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)

    override fun isOnboardingCompleted(): Boolean =
        prefs.getBoolean(StorageKeys.ONBOARDING_COMPLETED, false)

    override fun setOnboardingCompleted() {
        prefs.edit().putBoolean(StorageKeys.ONBOARDING_COMPLETED, true).apply()
    }
}
