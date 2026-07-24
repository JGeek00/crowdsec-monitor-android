package com.jgeek00.crowdsecmonitor.viewmodel

import com.jgeek00.crowdsecmonitor.MainDispatcherRule
import com.jgeek00.crowdsecmonitor.constants.Defaults
import com.jgeek00.crowdsecmonitor.data.api.CrowdSecApiClient
import com.jgeek00.crowdsecmonitor.data.api.DecisionsApiClient
import com.jgeek00.crowdsecmonitor.data.models.DecisionsByIPResponse
import com.jgeek00.crowdsecmonitor.data.models.DecisionsListResponse
import com.jgeek00.crowdsecmonitor.data.models.DecisionsRequestFilters
import com.jgeek00.crowdsecmonitor.data.models.EmptyResponse
import com.jgeek00.crowdsecmonitor.data.models.HttpClientException
import com.jgeek00.crowdsecmonitor.data.models.LoadingResult
import com.jgeek00.crowdsecmonitor.data.repository.PreferencesRepository
import com.jgeek00.crowdsecmonitor.fixtures.TestFixtures.decisionsByIPResponse
import com.jgeek00.crowdsecmonitor.fixtures.TestFixtures.decisionsByIPResponseGroup
import com.jgeek00.crowdsecmonitor.fixtures.TestFixtures.decisionsListResponse
import com.jgeek00.crowdsecmonitor.fixtures.TestFixtures.decisionsListResponseItem
import com.jgeek00.crowdsecmonitor.fixtures.TestFixtures.successResponse
import com.jgeek00.crowdsecmonitor.session.SessionManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class DecisionsListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun mockSessionManager(
        apiClient: CrowdSecApiClient? = null,
        decisionsRefreshFlow: MutableSharedFlow<Unit> = MutableSharedFlow()
    ): SessionManager {
        val sm = mockk<SessionManager>(relaxed = true)
        every { sm.apiClient } returns apiClient
        every { sm.decisionsRefreshEvent } returns decisionsRefreshFlow
        return sm
    }

    private fun mockPreferencesRepo(
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
    fun `initial state is Loading for both state and stateByIP`() = runTest {
        val viewModel = DecisionsListViewModel(mockSessionManager(), mockPreferencesRepo())
        advanceUntilIdle()
        assertTrue(viewModel.state is LoadingResult.Loading)
        assertTrue(viewModel.stateByIP is LoadingResult.Loading)
    }

    @Test
    fun `initialFetchDecisions with apiClient present calls fetchDecisions and state becomes Success`() = runTest {
        val decisionsClient = mockk<DecisionsApiClient>(relaxed = true)
        val apiClient = mockk<CrowdSecApiClient>(relaxed = true).also { every { it.decisions } returns decisionsClient }
        val body = decisionsListResponse()
        coEvery { decisionsClient.fetchDecisions(any()) } returns successResponse(body)

        val viewModel = DecisionsListViewModel(mockSessionManager(apiClient), mockPreferencesRepo())
        advanceUntilIdle()

        coVerify(exactly = 1) { decisionsClient.fetchDecisions(any()) }
        assertEquals(body, viewModel.state.data)
    }

    @Test
    fun `initialFetchDecisions when state data already exists is no-op`() = runTest {
        val decisionsClient = mockk<DecisionsApiClient>(relaxed = true)
        val apiClient = mockk<CrowdSecApiClient>(relaxed = true).also { every { it.decisions } returns decisionsClient }
        val body = decisionsListResponse()
        coEvery { decisionsClient.fetchDecisions(any()) } returns successResponse(body)

        val viewModel = DecisionsListViewModel(mockSessionManager(apiClient), mockPreferencesRepo())
        advanceUntilIdle()

        viewModel.initialFetchDecisions()
        advanceUntilIdle()

        coVerify(exactly = 1) { decisionsClient.fetchDecisions(any()) }
    }

    @Test
    fun `fetchDecisions failure when API throws makes state become Failure`() = runTest {
        val decisionsClient = mockk<DecisionsApiClient>(relaxed = true)
        val apiClient = mockk<CrowdSecApiClient>(relaxed = true).also { every { it.decisions } returns decisionsClient }
        val exception = HttpClientException.NetworkError(Exception("API error"))
        coEvery { decisionsClient.fetchDecisions(any()) } throws exception

        val viewModel = DecisionsListViewModel(mockSessionManager(apiClient), mockPreferencesRepo())
        advanceUntilIdle()

        assertTrue(viewModel.state is LoadingResult.Failure)
        assertEquals(exception, (viewModel.state as LoadingResult.Failure).throwable)
    }

    @Test
    fun `refreshDecisions sets isRefreshing toggles and fetches`() = runTest {
        val decisionsClient = mockk<DecisionsApiClient>(relaxed = true)
        val apiClient = mockk<CrowdSecApiClient>(relaxed = true).also { every { it.decisions } returns decisionsClient }
        coEvery { decisionsClient.fetchDecisions(any()) } returns successResponse(decisionsListResponse())

        val viewModel = DecisionsListViewModel(mockSessionManager(apiClient), mockPreferencesRepo())
        advanceUntilIdle()

        viewModel.refreshDecisions()
        advanceUntilIdle()

        assertFalse(viewModel.isRefreshing)
        coVerify(exactly = 2) { decisionsClient.fetchDecisions(any()) }
    }

    @Test
    fun `fetchMore paginates flat list and deduplicates by id`() = runTest {
        val decisionsClient = mockk<DecisionsApiClient>(relaxed = true)
        val apiClient = mockk<CrowdSecApiClient>(relaxed = true).also { every { it.decisions } returns decisionsClient }

        val item1 = decisionsListResponseItem(id = 1)
        val item2 = decisionsListResponseItem(id = 2)
        val page1 = decisionsListResponse(items = listOf(item1, item2), page = 1, total = 200)

        val item3 = decisionsListResponseItem(id = 3)
        val page2 = decisionsListResponse(items = listOf(item1, item3), page = 2, total = 200)

        coEvery { decisionsClient.fetchDecisions(any()) } returnsMany listOf(
            successResponse(page1),
            successResponse(page2)
        )

        val viewModel = DecisionsListViewModel(mockSessionManager(apiClient), mockPreferencesRepo())
        advanceUntilIdle()

        viewModel.fetchMore()
        advanceUntilIdle()

        assertFalse(viewModel.isLoadingMore)
        val result = viewModel.state.data
        assertNotNull(result)
        assertEquals(listOf(1, 2, 3), result!!.items.map { it.id })
        assertEquals(3, result.items.size)
    }

    @Test
    fun `fetchMore when at end of list is no-op`() = runTest {
        val decisionsClient = mockk<DecisionsApiClient>(relaxed = true)
        val apiClient = mockk<CrowdSecApiClient>(relaxed = true).also { every { it.decisions } returns decisionsClient }

        val body = decisionsListResponse(items = listOf(decisionsListResponseItem(id = 1)), page = 1, total = 30)
        coEvery { decisionsClient.fetchDecisions(any()) } returns successResponse(body)

        val viewModel = DecisionsListViewModel(mockSessionManager(apiClient), mockPreferencesRepo())
        advanceUntilIdle()

        viewModel.fetchMore()
        advanceUntilIdle()

        coVerify(exactly = 1) { decisionsClient.fetchDecisions(any()) }
    }

    @Test
    fun `fetchMore grouped by IP paginates with groupByIP filter`() = runTest {
        val decisionsClient = mockk<DecisionsApiClient>(relaxed = true)
        val apiClient = mockk<CrowdSecApiClient>(relaxed = true).also { every { it.decisions } returns decisionsClient }

        val group1 = decisionsByIPResponseGroup(ip = "1.2.3.4")
        val page1 = decisionsByIPResponse(groups = listOf(group1), page = 1, total = 200)
        val group3 = decisionsByIPResponseGroup(ip = "9.9.9.9")
        val page2 = decisionsByIPResponse(groups = listOf(group1, group3), page = 2, total = 200)

        coEvery { decisionsClient.fetchDecisionsByIP(any(), any(), any()) } returnsMany listOf(
            successResponse(page1),
            successResponse(page2)
        )

        val viewModel = DecisionsListViewModel(
            mockSessionManager(apiClient),
            mockPreferencesRepo(showGrouped = true)
        )
        advanceUntilIdle()

        viewModel.fetchMore()
        advanceUntilIdle()

        val result = viewModel.stateByIP.data
        assertNotNull(result)
        assertEquals(listOf("1.2.3.4", "9.9.9.9"), result!!.groups.map { it.ip })
        assertEquals(2, result.groups.size)
    }

    @Test
    fun `expireDecision success calls deleteDecision triggers refresh and calls onResult true`() = runTest {
        val decisionsClient = mockk<DecisionsApiClient>(relaxed = true)
        val apiClient = mockk<CrowdSecApiClient>(relaxed = true).also { every { it.decisions } returns decisionsClient }
        coEvery { decisionsClient.fetchDecisions(any()) } returns successResponse(decisionsListResponse())
        coEvery { decisionsClient.deleteDecision(any()) } returns successResponse(EmptyResponse())

        val viewModel = DecisionsListViewModel(mockSessionManager(apiClient), mockPreferencesRepo())
        advanceUntilIdle()

        var result: Boolean? = null
        viewModel.expireDecision(42) { result = it }
        advanceUntilIdle()

        coVerify(exactly = 1) { decisionsClient.deleteDecision(42) }
        assertEquals(true, result)
        assertFalse(viewModel.expiringDecisionProcess)
    }

    @Test
    fun `expireDecision failure calls onResult false`() = runTest {
        val decisionsClient = mockk<DecisionsApiClient>(relaxed = true)
        val apiClient = mockk<CrowdSecApiClient>(relaxed = true).also { every { it.decisions } returns decisionsClient }
        coEvery { decisionsClient.fetchDecisions(any()) } returns successResponse(decisionsListResponse())
        coEvery { decisionsClient.deleteDecision(any()) } throws HttpClientException.NetworkError(Exception("fail"))

        val viewModel = DecisionsListViewModel(mockSessionManager(apiClient), mockPreferencesRepo())
        advanceUntilIdle()

        var result: Boolean? = null
        viewModel.expireDecision(42) { result = it }
        advanceUntilIdle()

        assertEquals(false, result)
        assertFalse(viewModel.expiringDecisionProcess)
    }

    @Test
    fun `expireDecision with null apiClient calls onResult false immediately`() = runTest {
        val viewModel = DecisionsListViewModel(mockSessionManager(), mockPreferencesRepo())
        advanceUntilIdle()

        var result: Boolean? = null
        viewModel.expireDecision(42) { result = it }
        advanceUntilIdle()

        assertEquals(false, result)
        assertFalse(viewModel.expiringDecisionProcess)
    }

    @Test
    fun `reset restores all state to Loading and defaults`() = runTest {
        val decisionsClient = mockk<DecisionsApiClient>(relaxed = true)
        val apiClient = mockk<CrowdSecApiClient>(relaxed = true).also { every { it.decisions } returns decisionsClient }
        coEvery { decisionsClient.fetchDecisions(any()) } returns successResponse(decisionsListResponse())

        val viewModel = DecisionsListViewModel(mockSessionManager(apiClient), mockPreferencesRepo())
        advanceUntilIdle()

        viewModel.reset()

        assertTrue(viewModel.state is LoadingResult.Loading)
        assertTrue(viewModel.stateByIP is LoadingResult.Loading)
        assertFalse(viewModel.isRefreshing)
        assertFalse(viewModel.isLoadingMore)
        assertFalse(viewModel.expiringDecisionProcess)
        assertEquals(Defaults.DECISIONS_AMOUNT_BATCH, viewModel.requestParams.pagination.limit)
        assertEquals(0, viewModel.requestParams.pagination.offset)
    }

    @Test
    fun `resetFilters reads preferences updates defaultRequest and triggers fetch`() = runTest {
        val decisionsClient = mockk<DecisionsApiClient>(relaxed = true)
        val apiClient = mockk<CrowdSecApiClient>(relaxed = true).also { every { it.decisions } returns decisionsClient }
        coEvery { decisionsClient.fetchDecisions(any()) } returns successResponse(decisionsListResponse())

        val viewModel = DecisionsListViewModel(mockSessionManager(apiClient), mockPreferencesRepo())
        advanceUntilIdle()

        viewModel.resetFilters()
        advanceUntilIdle()

        coVerify(exactly = 2) { decisionsClient.fetchDecisions(any()) }
    }

    @Test
    fun `applyFilters updates requestParams with current filters and triggers fetch`() = runTest {
        val decisionsClient = mockk<DecisionsApiClient>(relaxed = true)
        val apiClient = mockk<CrowdSecApiClient>(relaxed = true).also { every { it.decisions } returns decisionsClient }
        coEvery { decisionsClient.fetchDecisions(any()) } returns successResponse(decisionsListResponse())

        val viewModel = DecisionsListViewModel(mockSessionManager(apiClient), mockPreferencesRepo())
        advanceUntilIdle()

        viewModel.updateFilters(DecisionsRequestFilters(onlyActive = false, groupByIP = null))
        viewModel.applyFilters()
        advanceUntilIdle()

        assertEquals(false, viewModel.filters.onlyActive)
        assertEquals(false, viewModel.requestParams.filters.onlyActive)
        coVerify(exactly = 2) { decisionsClient.fetchDecisions(any()) }
    }
}
