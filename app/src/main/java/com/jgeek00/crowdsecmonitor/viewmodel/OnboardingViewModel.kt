package com.jgeek00.crowdsecmonitor.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val onboardingStorage: OnboardingStorage
) : ViewModel() {

    var showOnboarding by mutableStateOf(
        !onboardingStorage.isOnboardingCompleted()
    )
        private set

    fun finishOnboarding() {
        onboardingStorage.setOnboardingCompleted()
        showOnboarding = false
    }
}
