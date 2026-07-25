package com.jgeek00.crowdsecmonitor.viewmodel

import com.jgeek00.crowdsecmonitor.MainDispatcherRule
import com.jgeek00.crowdsecmonitor.data.api.CrowdSecApiClient
import com.jgeek00.crowdsecmonitor.data.api.DecisionsApiClient
import com.jgeek00.crowdsecmonitor.data.models.*
import com.jgeek00.crowdsecmonitor.data.repository.PreferencesRepository
import com.jgeek00.crowdsecmonitor.fixtures.TestFixtures
import com.jgeek00.crowdsecmonitor.session.SessionManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class DecisionsListViewModelBranchTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun mockEnv(
        apiClient: CrowdSecApiClient? = null,
        showActive: Boolean = true,
        showGrouped: Boolean = false,
        disableTimer: Boolean = false
    ): Triple<SessionManager, CrowdSecApiClient, DecisionsApiClient> {
        val decisionsClient = mockk<DecisionsApiClient>(relaxed = true)
        val ac = apiClient ?: mockk<CrowdSecApiClient>(relaxed = true).also { every { it.decisions } returns decisionsClient }
        val sm = mockk<SessionManager>(relaxed = true).also {
            every { it.apiClient } returns if (apiClient != null) apiClient else ac
            every { it.decisionsRefreshEvent } returns MutableSharedFlow<Unit>()
        }
        return Triple(sm, ac, decisionsClient)
    }

    private fun mockPrefs(
        showActive: Boolean = true,
        showGrouped: Boolean = false,
        disableTimer: Boolean = false
    ): PreferencesRepository {
        val repo = mockk<PreferencesRepository>(relaxed = true)
        every { repo.showDefaultActiveDecisions } returns flowOf(showActive)
        every { repo.showDefaultDecisionsGroupedByIP } returns flowOf(showGrouped)
        every { repo.disableDecisionTimerAnimation } returns flowOf(disableTimer)
        return repo
    }

    @Test
    fun `initialFetchDecisions returns early when data loaded non-grouped`() = runTest {
        val (sm, _, decisionsClient) = mockEnv(showGrouped = false)
        coEvery { decisionsClient.fetchDecisions(any()) } returns HttpResponse(
            successful = true, statusCode = 200,
            body = com.jgeek00.crowdsecmonitor.fixtures.TestFixtures.decisionsListResponse()
        )
        val vm = DecisionsListViewModel(sm, mockPrefs(showGrouped = false))
        advanceUntilIdle()

        vm.initialFetchDecisions()
        advanceUntilIdle()

        // Only one fetch (from init)
        coVerify(exactly = 1) { decisionsClient.fetchDecisions(any()) }
    }

    @Test
    fun `initialFetchDecisions returns early when data loaded grouped`() = runTest {
        val (sm, _, decisionsClient) = mockEnv(showGrouped = true)
        coEvery { decisionsClient.fetchDecisionsByIP(any(), any(), any()) } returns HttpResponse(
            successful = true, statusCode = 200,
            body = com.jgeek00.crowdsecmonitor.fixtures.TestFixtures.decisionsByIPResponse()
        )
        val vm = DecisionsListViewModel(sm, mockPrefs(showGrouped = true))
        advanceUntilIdle()

        vm.initialFetchDecisions()
        advanceUntilIdle()

        assertEquals(1, vm.stateByIP.data?.groups?.size)
    }

    @Test
    fun `fetchMore does not crash when apiClient null`() {
        val (sm) = mockEnv(apiClient = null)
        val vm = DecisionsListViewModel(sm, mockPrefs())
        vm.fetchMore()
    }

    @Test
    fun `expireDecision handles apiClient null`() {
        val sm = mockk<SessionManager>(relaxed = true)
        every { sm.apiClient } returns null
        every { sm.decisionsRefreshEvent } returns MutableSharedFlow<Unit>()

        val vm = DecisionsListViewModel(sm, mockPrefs())

        var result = true
        vm.expireDecision(1) { result = it }

        assertFalse(result)
    }

    @Test
    fun `expireDecision calls deleteDecision`() = runTest {
        val (sm, _, decisionsClient) = mockEnv()
        coEvery { decisionsClient.deleteDecision(any()) } returns mockk(relaxed = true)
        coEvery { decisionsClient.fetchDecisions(any()) } returns HttpResponse(
            successful = true, statusCode = 200,
            body = TestFixtures.decisionsListResponse()
        )
        val vm = DecisionsListViewModel(sm, mockPrefs())
        advanceUntilIdle()

        var result = false
        vm.expireDecision(1) { result = it }
        advanceUntilIdle()

        assertTrue(result)
    }

    @Test
    fun `expireDecision handles error`() = runTest {
        val (sm, _, decisionsClient) = mockEnv()
        coEvery { decisionsClient.deleteDecision(any()) } throws Exception("API error")
        val vm = DecisionsListViewModel(sm, mockPrefs())
        advanceUntilIdle()

        var result = true
        vm.expireDecision(1) { result = it }
        advanceUntilIdle()

        assertFalse(result)
    }

    @Test
    fun `updateFilters sets filters`() {
        val (sm) = mockEnv()
        val vm = DecisionsListViewModel(sm, mockPrefs())

        val filters = DecisionsRequestFilters(onlyActive = false, groupByIP = true)
        vm.updateFilters(filters)

        assertEquals(false, vm.filters.onlyActive)
        assertEquals(true, vm.filters.groupByIP)
    }

    @Test
    fun `resetFiltersPanelToAppliedOnes resets panel`() = runTest {
        val (sm, _, decisionsClient) = mockEnv()
        coEvery { decisionsClient.fetchDecisions(any()) } returns HttpResponse(
            successful = true, statusCode = 200,
            body = com.jgeek00.crowdsecmonitor.fixtures.TestFixtures.decisionsListResponse()
        )
        val vm = DecisionsListViewModel(sm, mockPrefs())
        advanceUntilIdle()

        vm.updateFilters(DecisionsRequestFilters(onlyActive = false, groupByIP = null))
        vm.applyFilters()
        advanceUntilIdle()

        vm.updateFilters(DecisionsRequestFilters(onlyActive = true, groupByIP = null))
        vm.resetFiltersPanelToAppliedOnes()

        assertEquals(false, vm.filters.onlyActive)
    }
}
