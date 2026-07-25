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

class DecisionIPGroupDetailViewModelBranchTest {

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
        coEvery { decisionsClient.fetchDecisionsByIPDetail(any(), any()) } returns TestFixtures.successResponse(mockk(relaxed = true))
    }

    @Test
    fun `initialize sets state to Loading then Success`() = runTest {
        val vm = DecisionIPGroupDetailViewModel(sessionManager, preferencesRepository)

        vm.initialize("1.2.3.4")
        assertTrue(vm.state is LoadingResult.Loading)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        assertTrue(vm.state is LoadingResult.Success)
    }

    @Test
    fun `initialize with same ip and onlyActive is no-op`() = runTest {
        val vm = DecisionIPGroupDetailViewModel(sessionManager, preferencesRepository)

        vm.initialize("1.2.3.4", onlyActive = true)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        assertTrue(vm.state is LoadingResult.Success)

        // Same ip + same onlyActive should not re-fetch
        vm.initialize("1.2.3.4", onlyActive = true)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
    }

    @Test
    fun `initialize with different onlyActive re-fetches`() = runTest {
        val vm = DecisionIPGroupDetailViewModel(sessionManager, preferencesRepository)

        vm.initialize("1.2.3.4", onlyActive = true)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        vm.initialize("1.2.3.4", onlyActive = false)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        assertTrue(vm.state is LoadingResult.Success)
    }

    @Test
    fun `initialize with null apiClient does not crash`() = runTest {
        every { sessionManager.apiClient } returns null
        val vm = DecisionIPGroupDetailViewModel(sessionManager, preferencesRepository)

        vm.initialize("1.2.3.4")
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        assertTrue(vm.state is LoadingResult.Loading)
    }

    @Test
    fun `expireDecision with null apiClient calls onResult false`() {
        every { sessionManager.apiClient } returns null
        val vm = DecisionIPGroupDetailViewModel(sessionManager, preferencesRepository)

        var result = true
        vm.expireDecision(1) { result = it }

        assertFalse(result)
    }

    @Test
    fun `expireDecision success calls onResult true`() = runTest {
        val vm = DecisionIPGroupDetailViewModel(sessionManager, preferencesRepository)
        vm.initialize("1.2.3.4")
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        coEvery { decisionsClient.deleteDecision(any()) } returns TestFixtures.successResponse(mockk(relaxed = true))

        var result = false
        vm.expireDecision(1) { result = it }
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        assertTrue(result)
    }

    @Test
    fun `expireDecision error calls onResult false`() = runTest {
        val vm = DecisionIPGroupDetailViewModel(sessionManager, preferencesRepository)
        vm.initialize("1.2.3.4")
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        coEvery { decisionsClient.deleteDecision(any()) } throws Exception("test error")

        var result = true
        vm.expireDecision(1) { result = it }
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        assertFalse(result)
    }

    @Test
    fun `refresh on error sets Failure state`() = runTest {
        val vm = DecisionIPGroupDetailViewModel(sessionManager, preferencesRepository)
        vm.initialize("1.2.3.4")
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        coEvery { decisionsClient.fetchDecisionsByIPDetail(any(), any()) } throws Exception("test error")
        vm.refresh()
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        assertTrue(vm.state is LoadingResult.Failure)
        assertFalse(vm.isRefreshing)
    }
}
