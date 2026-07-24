package com.jgeek00.crowdsecmonitor.viewmodel

import com.jgeek00.crowdsecmonitor.MainDispatcherRule
import com.jgeek00.crowdsecmonitor.data.api.AllowlistsApiClient
import com.jgeek00.crowdsecmonitor.data.api.CrowdSecApiClient
import com.jgeek00.crowdsecmonitor.data.models.AllowlistsListResponse
import com.jgeek00.crowdsecmonitor.data.models.LoadingResult
import com.jgeek00.crowdsecmonitor.fixtures.TestFixtures
import com.jgeek00.crowdsecmonitor.session.SessionManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AllowlistsListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val sessionManager = mockk<SessionManager>(relaxed = true)
    private val apiClient = mockk<CrowdSecApiClient>(relaxed = true)
    private val allowlistsClient = mockk<AllowlistsApiClient>(relaxed = true)

    @Before
    fun setUp() {
        every { sessionManager.apiClient } returns null
    }

    @Test
    fun `initial state is Loading`() {
        val vm = AllowlistsListViewModel(sessionManager)
        assertEquals(LoadingResult.Loading, vm.state)
        assertFalse(vm.isRefreshing)
    }

    @Test
    fun `initialFetch sets state to Success`() = runTest {
        every { sessionManager.apiClient } returns apiClient
        every { apiClient.allowlists } returns allowlistsClient
        val response = mockk<AllowlistsListResponse>(relaxed = true)
        coEvery { allowlistsClient.fetchAllowlists() } returns TestFixtures.successResponse(response)

        val vm = AllowlistsListViewModel(sessionManager)
        vm.initialFetch()
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        val result = vm.state
        assertTrue(result is LoadingResult.Success)
        assertEquals(response, (result as LoadingResult.Success).value)
    }

    @Test
    fun `initialFetch sets state to Failure on exception`() = runTest {
        every { sessionManager.apiClient } returns apiClient
        every { apiClient.allowlists } returns allowlistsClient
        coEvery { allowlistsClient.fetchAllowlists() } throws Exception("test error")

        val vm = AllowlistsListViewModel(sessionManager)
        vm.initialFetch()
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        assertTrue(vm.state is LoadingResult.Failure)
    }

    @Test
    fun `initialFetch is no-op when data already loaded`() = runTest {
        every { sessionManager.apiClient } returns apiClient
        every { apiClient.allowlists } returns allowlistsClient
        val response = mockk<AllowlistsListResponse>(relaxed = true)
        coEvery { allowlistsClient.fetchAllowlists() } returns TestFixtures.successResponse(response)

        val vm = AllowlistsListViewModel(sessionManager)
        vm.initialFetch()
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        vm.initialFetch()
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { allowlistsClient.fetchAllowlists() }
    }

    @Test
    fun `refresh fetches data and toggles isRefreshing`() = runTest {
        every { sessionManager.apiClient } returns apiClient
        every { apiClient.allowlists } returns allowlistsClient
        val response = mockk<AllowlistsListResponse>(relaxed = true)
        coEvery { allowlistsClient.fetchAllowlists() } returns TestFixtures.successResponse(response)

        val vm = AllowlistsListViewModel(sessionManager)
        vm.refresh()
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        assertFalse(vm.isRefreshing)
        assertTrue(vm.state is LoadingResult.Success)
    }

    @Test
    fun `refresh sets state to Failure on exception`() = runTest {
        every { sessionManager.apiClient } returns apiClient
        every { apiClient.allowlists } returns allowlistsClient
        coEvery { allowlistsClient.fetchAllowlists() } throws Exception("test error")

        val vm = AllowlistsListViewModel(sessionManager)
        vm.refresh()
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        assertTrue(vm.state is LoadingResult.Failure)
        assertFalse(vm.isRefreshing)
    }

    @Test
    fun `reset clears state and isRefreshing`() = runTest {
        every { sessionManager.apiClient } returns apiClient
        every { apiClient.allowlists } returns allowlistsClient
        val response = mockk<AllowlistsListResponse>(relaxed = true)
        coEvery { allowlistsClient.fetchAllowlists() } returns TestFixtures.successResponse(response)

        val vm = AllowlistsListViewModel(sessionManager)
        vm.refresh()
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        vm.reset()
        assertEquals(LoadingResult.Loading, vm.state)
        assertFalse(vm.isRefreshing)
    }
}
