package com.jgeek00.crowdsecmonitor.viewmodel

import com.jgeek00.crowdsecmonitor.MainDispatcherRule
import com.jgeek00.crowdsecmonitor.data.api.CrowdSecApiClient
import com.jgeek00.crowdsecmonitor.data.api.DecisionsApiClient
import com.jgeek00.crowdsecmonitor.data.models.DecisionsByIPDetailResponse
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

class DecisionIPGroupDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val sessionManager = mockk<SessionManager>(relaxed = true)
    private val preferencesRepository = mockk<PreferencesRepository>(relaxed = true)
    private val apiClient = mockk<CrowdSecApiClient>(relaxed = true)
    private val decisionsClient = mockk<DecisionsApiClient>(relaxed = true)
    private lateinit var vm: DecisionIPGroupDetailViewModel

    @Before
    fun setUp() {
        every { sessionManager.apiClient } returns apiClient
        every { apiClient.decisions } returns decisionsClient
        every { preferencesRepository.disableDecisionTimerAnimation } returns flowOf(false)
        vm = DecisionIPGroupDetailViewModel(sessionManager, preferencesRepository)
    }

    @Test
    fun `initialize with onlyActive null fetches data`() = runTest {
        val detail = mockk<DecisionsByIPDetailResponse>(relaxed = true)
        coEvery { decisionsClient.fetchDecisionsByIPDetail(ip = "1.2.3.4", onlyActive = null) } returns TestFixtures.successResponse(detail)

        vm.initialize("1.2.3.4")
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        val result = vm.state
        assertTrue(result is LoadingResult.Success)
        assertEquals(detail, (result as LoadingResult.Success).value)
    }

    @Test
    fun `initialize with onlyActive true fetches data`() = runTest {
        val detail = mockk<DecisionsByIPDetailResponse>(relaxed = true)
        coEvery { decisionsClient.fetchDecisionsByIPDetail(ip = "1.2.3.4", onlyActive = true) } returns TestFixtures.successResponse(detail)

        vm.initialize("1.2.3.4", onlyActive = true)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        assertTrue(vm.state is LoadingResult.Success)
    }

    @Test
    fun `duplicate init is no-op`() = runTest {
        val detail = mockk<DecisionsByIPDetailResponse>(relaxed = true)
        coEvery { decisionsClient.fetchDecisionsByIPDetail(ip = "1.2.3.4", onlyActive = null) } returns TestFixtures.successResponse(detail)

        vm.initialize("1.2.3.4")
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        vm.initialize("1.2.3.4")
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { decisionsClient.fetchDecisionsByIPDetail(ip = "1.2.3.4", onlyActive = null) }
    }

    @Test
    fun `duplicate init with different params is not no-op`() = runTest {
        val detail = mockk<DecisionsByIPDetailResponse>(relaxed = true)
        coEvery { decisionsClient.fetchDecisionsByIPDetail(ip = "1.2.3.4", onlyActive = any()) } returns TestFixtures.successResponse(detail)

        vm.initialize("1.2.3.4")
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        vm.initialize("1.2.3.4", onlyActive = true)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 2) { decisionsClient.fetchDecisionsByIPDetail(ip = "1.2.3.4", onlyActive = any()) }
    }

    @Test
    fun `initialize sets Failure on exception`() = runTest {
        coEvery { decisionsClient.fetchDecisionsByIPDetail(ip = "1.2.3.4", onlyActive = null) } throws Exception("test error")

        vm.initialize("1.2.3.4")
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        assertTrue(vm.state is LoadingResult.Failure)
    }

    @Test
    fun `refresh fetches data and toggles isRefreshing`() = runTest {
        val detail = mockk<DecisionsByIPDetailResponse>(relaxed = true)
        coEvery { decisionsClient.fetchDecisionsByIPDetail(ip = "1.2.3.4", onlyActive = null) } returns TestFixtures.successResponse(detail)

        vm.initialize("1.2.3.4")
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        vm.refresh()
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        assertFalse(vm.isRefreshing)
        assertTrue(vm.state is LoadingResult.Success)
    }

    @Test
    fun `expireDecision succeeds and triggers refresh`() = runTest {
        val detail = mockk<DecisionsByIPDetailResponse>(relaxed = true)
        coEvery { decisionsClient.fetchDecisionsByIPDetail(ip = "1.2.3.4", onlyActive = null) } returns TestFixtures.successResponse(detail)
        coEvery { decisionsClient.deleteDecision(1) } returns TestFixtures.successResponse(mockk(relaxed = true))
        every { sessionManager.triggerDecisionsRefresh() } just runs

        vm.initialize("1.2.3.4")
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
        val detail = mockk<DecisionsByIPDetailResponse>(relaxed = true)
        coEvery { decisionsClient.fetchDecisionsByIPDetail(ip = "1.2.3.4", onlyActive = null) } returns TestFixtures.successResponse(detail)
        coEvery { decisionsClient.deleteDecision(1) } throws Exception("test error")

        vm.initialize("1.2.3.4")
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        var callbackResult = true
        vm.expireDecision(1) { callbackResult = it }
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        assertFalse(callbackResult)
        assertFalse(vm.expiringDecisionProcess)
    }
}
