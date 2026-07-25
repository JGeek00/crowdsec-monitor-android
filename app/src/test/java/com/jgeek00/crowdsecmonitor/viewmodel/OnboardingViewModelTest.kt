package com.jgeek00.crowdsecmonitor.viewmodel

import com.jgeek00.crowdsecmonitor.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class OnboardingViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val onboardingStorage = mockk<OnboardingStorage>(relaxed = true)
    private lateinit var vm: OnboardingViewModel

    @Before
    fun setUp() {
        every { onboardingStorage.isOnboardingCompleted() } returns false
        vm = OnboardingViewModel(onboardingStorage)
    }

    @Test
    fun `showOnboarding is true when onboarding not completed`() {
        assertTrue(vm.showOnboarding)
    }

    @Test
    fun `showOnboarding is false when onboarding already completed`() {
        every { onboardingStorage.isOnboardingCompleted() } returns true

        val vm2 = OnboardingViewModel(onboardingStorage)

        assertFalse(vm2.showOnboarding)
    }

    @Test
    fun `finishOnboarding sets sharedPref and hides onboarding`() {
        vm.finishOnboarding()

        assertFalse(vm.showOnboarding)
        verify { onboardingStorage.setOnboardingCompleted() }
    }
}
