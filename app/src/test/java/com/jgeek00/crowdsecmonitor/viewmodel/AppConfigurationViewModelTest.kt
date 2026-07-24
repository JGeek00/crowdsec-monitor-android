package com.jgeek00.crowdsecmonitor.viewmodel

import com.jgeek00.crowdsecmonitor.MainDispatcherRule
import com.jgeek00.crowdsecmonitor.constants.Defaults
import com.jgeek00.crowdsecmonitor.data.repository.PreferencesRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AppConfigurationViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val preferencesRepository = mockk<PreferencesRepository>(relaxed = true)
    private lateinit var vm: AppConfigurationViewModel

    @Before
    fun setUp() {
        every { preferencesRepository.topItemsDashboard } returns flowOf(7)
        every { preferencesRepository.showDefaultActiveDecisions } returns flowOf(false)
        every { preferencesRepository.disableDecisionTimerAnimation } returns flowOf(true)
        every { preferencesRepository.showDefaultDecisionsGroupedByIP } returns flowOf(true)
        vm = AppConfigurationViewModel(preferencesRepository)
    }

    @Test
    fun `init reads preferences from repository`() = runTest {
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        assertEquals(7, vm.topItemsDashboard)
        assertEquals(false, vm.showDefaultActiveDecisions)
        assertEquals(true, vm.disableDecisionTimerAnimation)
        assertEquals(true, vm.showDefaultDecisionsGroupedByIP)
    }

    @Test
    fun `init uses default values when preferences return defaults`() = runTest {
        every { preferencesRepository.topItemsDashboard } returns flowOf(Defaults.TOP_ITEMS_DASHBOARD)
        every { preferencesRepository.showDefaultActiveDecisions } returns flowOf(Defaults.SHOW_DEFAULT_ACTIVE_DECISIONS)
        every { preferencesRepository.disableDecisionTimerAnimation } returns flowOf(Defaults.DISABLE_DECISION_TIMER_ANIMATION)
        every { preferencesRepository.showDefaultDecisionsGroupedByIP } returns flowOf(Defaults.SHOW_DEFAULT_DECISIONS_GROUPED_BY_IP)

        val vm2 = AppConfigurationViewModel(preferencesRepository)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        assertEquals(5, vm2.topItemsDashboard)
        assertTrue(vm2.showDefaultActiveDecisions)
        assertFalse(vm2.disableDecisionTimerAnimation)
        assertFalse(vm2.showDefaultDecisionsGroupedByIP)
    }

    @Test
    fun `updateTopItemsDashboard clamps to min when value is too low`() = runTest {
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        vm.updateTopItemsDashboard(3)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        assertEquals(Defaults.TOP_ITEMS_DASHBOARD_MIN, vm.topItemsDashboard)
    }

    @Test
    fun `updateTopItemsDashboard clamps to max when value is too high`() = runTest {
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        vm.updateTopItemsDashboard(20)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        assertEquals(Defaults.TOP_ITEMS_DASHBOARD_MAX, vm.topItemsDashboard)
    }

    @Test
    fun `updateTopItemsDashboard sets value when within range`() = runTest {
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        vm.updateTopItemsDashboard(8)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        assertEquals(8, vm.topItemsDashboard)
    }

    @Test
    fun `updateShowDefaultActiveDecisions updates value`() = runTest {
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        vm.updateShowDefaultActiveDecisions(true)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        assertTrue(vm.showDefaultActiveDecisions)
    }

    @Test
    fun `updateDisableDecisionTimerAnimation updates value`() = runTest {
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        vm.updateDisableDecisionTimerAnimation(false)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        assertFalse(vm.disableDecisionTimerAnimation)
    }

    @Test
    fun `updateShowDefaultDecisionsGroupedByIP updates value`() = runTest {
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        vm.updateShowDefaultDecisionsGroupedByIP(false)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        assertFalse(vm.showDefaultDecisionsGroupedByIP)
    }
}
