package com.jgeek00.crowdsecmonitor.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.jgeek00.crowdsecmonitor.constants.StorageKeys
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import androidx.core.content.edit

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val sharedPreferences =
        context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)

    var showOnboarding by mutableStateOf(
        !sharedPreferences.getBoolean(StorageKeys.ONBOARDING_COMPLETED, false)
    )
        private set

    fun finishOnboarding() {
        sharedPreferences.edit {
            putBoolean(StorageKeys.ONBOARDING_COMPLETED, true)
        }
        showOnboarding = false
    }
}

