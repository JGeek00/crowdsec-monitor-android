package com.jgeek00.crowdsecmonitor.viewmodel

import com.jgeek00.crowdsecmonitor.MainDispatcherRule
import com.jgeek00.crowdsecmonitor.data.api.CrowdSecApiClient
import com.jgeek00.crowdsecmonitor.data.api.DecisionsApiClient
import com.jgeek00.crowdsecmonitor.data.models.LoadingResult
import com.jgeek00.crowdsecmonitor.data.repository.PreferencesRepository
import com.jgeek00.crowdsecmonitor.fixtures.TestFixtures
import com.jgeek00.crowdsecmonitor.session.SessionManager
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class DecisionDetailsViewModelBranchTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val sessionManager = mockk<SessionManager>(relaxed = true)
    private val preferencesRepository = mockk<PreferencesRepository>(relaxed = true)
    private val apiClient = mockk<CrowdSecApiClient>(relaxed = true)
    private val decisionsClient = mockk<DecisionsApiClient>(relaxed = true)

    @Before
    fun setUp() {
        every { sessionManager.apiClient } returns apiClient
        every { apiClient.decisions } returns decisionsClient
        every { preferencesRepository.disableDecisionTimerAnimation } returns flowOf(false)
    }

    @Test
    fun `initialize with null apiClient does not crash`() = runTest {
        every { sessionManager.apiClient } returns null
        val vm = DecisionDetailsViewModel(sessionManager, preferencesRepository)

        vm.initialize(1)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        assertTrue(vm.state is LoadingResult.Loading)
    }

    @Test
    fun `expireDecision with null apiClient calls onResult false`() {
        every { sessionManager.apiClient } returns null
        val vm = DecisionDetailsViewModel(sessionManager, preferencesRepository)

        var result = true
        vm.expireDecision(1) { result = it }

        assertFalse(result)
    }

    @Test
    fun `initialize called with different id re-fetches`() = runTest {
        val vm = DecisionDetailsViewModel(sessionManager, preferencesRepository)
        coEvery { decisionsClient.fetchDecisionDetails(any()) } returns TestFixtures.successResponse(mockk(relaxed = true))

        vm.initialize(1)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        vm.initialize(2)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        assertTrue(vm.state is LoadingResult.Success)
    }
}
