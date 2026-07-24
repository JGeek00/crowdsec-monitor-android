package com.jgeek00.crowdsecmonitor.viewmodel

import com.jgeek00.crowdsecmonitor.MainDispatcherRule
import com.jgeek00.crowdsecmonitor.data.api.CrowdSecApiClient
import com.jgeek00.crowdsecmonitor.data.api.DecisionsApiClient
import com.jgeek00.crowdsecmonitor.data.models.DecisionItemResponse
import com.jgeek00.crowdsecmonitor.data.models.LoadingResult
import com.jgeek00.crowdsecmonitor.data.repository.PreferencesRepository
import com.jgeek00.crowdsecmonitor.fixtures.TestFixtures
import com.jgeek00.crowdsecmonitor.session.SessionManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class DecisionDetailsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val sessionManager = mockk<SessionManager>(relaxed = true)
    private val preferencesRepository = mockk<PreferencesRepository>(relaxed = true)
    private val apiClient = mockk<CrowdSecApiClient>(relaxed = true)
    private val decisionsClient = mockk<DecisionsApiClient>(relaxed = true)
    private lateinit var vm: DecisionDetailsViewModel

    @Before
    fun setUp() {
        every { sessionManager.apiClient } returns apiClient
        every { apiClient.decisions } returns decisionsClient
        every { preferencesRepository.disableDecisionTimerAnimation } returns flowOf(false)
        vm = DecisionDetailsViewModel(sessionManager, preferencesRepository)
    }

    @Test
    fun `initialize fetches data and sets state to Success`() = runTest {
        val decision = mockk<DecisionItemResponse>(relaxed = true)
        coEvery { decisionsClient.fetchDecisionDetails(1) } returns TestFixtures.successResponse(decision)

        vm.initialize(1)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        val result = vm.state
        assertTrue(result is LoadingResult.Success)
        assertEquals(decision, (result as LoadingResult.Success).value)
    }

    @Test
    fun `initialize duplicate call is no-op`() = runTest {
        val decision = mockk<DecisionItemResponse>(relaxed = true)
        coEvery { decisionsClient.fetchDecisionDetails(1) } returns TestFixtures.successResponse(decision)

        vm.initialize(1)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        vm.initialize(1)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { decisionsClient.fetchDecisionDetails(1) }
    }

    @Test
    fun `initialize sets state to Failure on exception`() = runTest {
        coEvery { decisionsClient.fetchDecisionDetails(1) } throws Exception("test error")

        vm.initialize(1)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        assertTrue(vm.state is LoadingResult.Failure)
    }

    @Test
    fun `refresh calls fetchDecisionDetails and toggles isRefreshing`() = runTest {
        val decision = mockk<DecisionItemResponse>(relaxed = true)
        coEvery { decisionsClient.fetchDecisionDetails(any()) } returns TestFixtures.successResponse(decision)

        vm.initialize(1)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        vm.refresh(1)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        assertFalse(vm.isRefreshing)
        val result = vm.state
        assertTrue(result is LoadingResult.Success)
    }

    @Test
    fun `expireDecision succeeds and triggers refresh`() = runTest {
        val decision = mockk<DecisionItemResponse>(relaxed = true)
        coEvery { decisionsClient.fetchDecisionDetails(any()) } returns TestFixtures.successResponse(decision)
        coEvery { decisionsClient.deleteDecision(1) } returns TestFixtures.successResponse(mockk(relaxed = true))
        every { sessionManager.triggerDecisionsRefresh() } just runs

        vm.initialize(1)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        var callbackResult = false
        vm.expireDecision(1) { callbackResult = it }
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        assertTrue(callbackResult)
        assertFalse(vm.expiringDecisionProcess)
        coVerify { decisionsClient.deleteDecision(1) }
        verify { sessionManager.triggerDecisionsRefresh() }
    }

    @Test
    fun `expireDecision calls onResult with false on exception`() = runTest {
        val decision = mockk<DecisionItemResponse>(relaxed = true)
        coEvery { decisionsClient.fetchDecisionDetails(any()) } returns TestFixtures.successResponse(decision)
        coEvery { decisionsClient.deleteDecision(1) } throws Exception("test error")

        vm.initialize(1)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        var callbackResult = true
        vm.expireDecision(1) { callbackResult = it }
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        assertFalse(callbackResult)
        assertFalse(vm.expiringDecisionProcess)
    }

    @Test
    fun `disableDecisionTimerAnimation is collected from preferences`() = runTest {
        every { preferencesRepository.disableDecisionTimerAnimation } returns flowOf(true)

        val vm2 = DecisionDetailsViewModel(sessionManager, preferencesRepository)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        assertTrue(vm2.disableDecisionTimerAnimation)
    }
}
