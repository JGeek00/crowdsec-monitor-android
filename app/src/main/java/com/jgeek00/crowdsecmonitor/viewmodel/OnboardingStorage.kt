package com.jgeek00.crowdsecmonitor.viewmodel

interface OnboardingStorage {
    fun isOnboardingCompleted(): Boolean
    fun setOnboardingCompleted()
}
