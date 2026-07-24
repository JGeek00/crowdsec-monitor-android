package com.jgeek00.crowdsecmonitor.viewmodel

import android.content.Context
import androidx.core.content.edit
import com.jgeek00.crowdsecmonitor.constants.StorageKeys
import com.jgeek00.crowdsecmonitor.MainDispatcherRule
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class OnboardingViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val context: Context = RuntimeEnvironment.getApplication()
    private lateinit var vm: OnboardingViewModel

    @Before
    fun setUp() {
        val prefs = context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)
        prefs.edit { clear() }
        vm = OnboardingViewModel(context)
    }

    @Test
    fun `showOnboarding is true when onboarding not completed`() {
        assertTrue(vm.showOnboarding)
    }

    @Test
    fun `showOnboarding is false when onboarding already completed`() {
        val prefs = context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)
        prefs.edit { putBoolean(StorageKeys.ONBOARDING_COMPLETED, true) }

        val vm2 = OnboardingViewModel(context)

        assertFalse(vm2.showOnboarding)
    }

    @Test
    fun `finishOnboarding sets sharedPref and hides onboarding`() {
        vm.finishOnboarding()

        assertFalse(vm.showOnboarding)
        val prefs = context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)
        assertTrue(prefs.getBoolean(StorageKeys.ONBOARDING_COMPLETED, false))
    }
}
