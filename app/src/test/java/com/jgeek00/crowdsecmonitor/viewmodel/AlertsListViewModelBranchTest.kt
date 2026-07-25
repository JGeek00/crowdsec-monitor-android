package com.jgeek00.crowdsecmonitor.viewmodel

import com.jgeek00.crowdsecmonitor.MainDispatcherRule
import com.jgeek00.crowdsecmonitor.data.api.AlertsApiClient
import com.jgeek00.crowdsecmonitor.data.api.CrowdSecApiClient
import com.jgeek00.crowdsecmonitor.data.models.*
import com.jgeek00.crowdsecmonitor.fixtures.TestFixtures
import com.jgeek00.crowdsecmonitor.session.SessionManager
import io.mockk.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class AlertsListViewModelBranchTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun mockEnv(
        apiClient: CrowdSecApiClient? = null
    ): Triple<SessionManager, CrowdSecApiClient, AlertsApiClient> {
        val alertsClient = mockk<AlertsApiClient>(relaxed = true)
        val ac = apiClient ?: mockk<CrowdSecApiClient>(relaxed = true).also { every { it.alerts } returns alertsClient }
        val sm = mockk<SessionManager>(relaxed = true).also {
            every { it.apiClient } returns if (apiClient != null) apiClient else ac
            every { it.alertsRefreshEvent } returns MutableSharedFlow<Unit>()
        }
        return Triple(sm, ac, alertsClient)
    }

    @Test
    fun `initialFetchAlerts returns early when data already loaded`() = runTest {
        val (sm, _, alertsClient) = mockEnv()
        val response = TestFixtures.alertsListResponse()
        coEvery { alertsClient.fetchAlerts(any()) } returns HttpResponse(
            successful = true, statusCode = 200, body = response
        )
        val vm = AlertsListViewModel(sm)
        advanceUntilIdle()

        vm.initialFetchAlerts()
        advanceUntilIdle()

        coVerify(exactly = 1) { alertsClient.fetchAlerts(any()) }
    }

    @Test
    fun `fetchMore does not crash when apiClient null`() {
        val (sm) = mockEnv(apiClient = null)
        val vm = AlertsListViewModel(sm)
        vm.fetchMore()
    }

    @Test
    fun `refreshAlerts resets pagination`() = runTest {
        val (sm, _, alertsClient) = mockEnv()
        coEvery { alertsClient.fetchAlerts(any()) } returns HttpResponse(
            successful = true, statusCode = 200,
            body = TestFixtures.alertsListResponse()
        )
        val vm = AlertsListViewModel(sm)
        advanceUntilIdle()

        vm.refreshAlerts()
        advanceUntilIdle()

        assertFalse(vm.isRefreshing)
        assertEquals(0, vm.requestParams.pagination.offset)
    }

    @Test
    fun `applyFilters triggers fetch with updated filters`() = runTest {
        val (sm, _, alertsClient) = mockEnv()
        coEvery { alertsClient.fetchAlerts(any()) } returns HttpResponse(
            successful = true, statusCode = 200,
            body = TestFixtures.alertsListResponse()
        )
        val vm = AlertsListViewModel(sm)
        advanceUntilIdle()

        vm.updateFilters(AlertsRequestFilters(countries = listOf("FR"), scenarios = emptyList(), ipOwners = emptyList(), targets = emptyList()))
        vm.applyFilters()
        advanceUntilIdle()

        assertEquals(listOf("FR"), vm.filters.countries)
        coVerify(exactly = 2) { alertsClient.fetchAlerts(any()) }
    }

    @Test
    fun `refreshAlerts resets to first page`() = runTest {
        val (sm, _, alertsClient) = mockEnv()
        coEvery { alertsClient.fetchAlerts(any()) } returns HttpResponse(
            successful = true, statusCode = 200,
            body = TestFixtures.alertsListResponse()
        )
        val vm = AlertsListViewModel(sm)
        advanceUntilIdle()

        vm.refreshAlerts()
        advanceUntilIdle()

        assertFalse(vm.isRefreshing)
        assertEquals(0, vm.requestParams.pagination.offset)
    }

    @Test
    fun `updateFilters sets filters`() {
        val (sm) = mockEnv()
        val vm = AlertsListViewModel(sm)

        val filters = AlertsRequestFilters(countries = listOf("US"), scenarios = emptyList(), ipOwners = emptyList(), targets = emptyList())
        vm.updateFilters(filters)
        assertEquals(listOf("US"), vm.filters.countries)
    }

    @Test
    fun `resetFiltersPanelToAppliedOnes resets panel`() = runTest {
        val (sm, _, alertsClient) = mockEnv()
        coEvery { alertsClient.fetchAlerts(any()) } returns HttpResponse(
            successful = true, statusCode = 200,
            body = TestFixtures.alertsListResponse()
        )
        val vm = AlertsListViewModel(sm)
        advanceUntilIdle()

        vm.updateFilters(AlertsRequestFilters(countries = listOf("FR"), scenarios = emptyList(), ipOwners = emptyList(), targets = emptyList()))
        vm.applyFilters()
        advanceUntilIdle()

        vm.updateFilters(AlertsRequestFilters(countries = listOf("DE"), scenarios = emptyList(), ipOwners = emptyList(), targets = emptyList()))
        vm.resetFiltersPanelToAppliedOnes()

        assertEquals(listOf("FR"), vm.filters.countries)
    }

    @Test
    fun `applyFilters triggers fetch with filters`() = runTest {
        val (sm, _, alertsClient) = mockEnv()
        coEvery { alertsClient.fetchAlerts(any()) } returns HttpResponse(
            successful = true, statusCode = 200,
            body = TestFixtures.alertsListResponse()
        )
        val vm = AlertsListViewModel(sm)
        advanceUntilIdle()

        val filters = AlertsRequestFilters(countries = listOf("FR"), scenarios = listOf("test"), ipOwners = emptyList(), targets = emptyList())
        vm.updateFilters(filters)
        vm.applyFilters()
        advanceUntilIdle()

        assertEquals(listOf("FR"), vm.filters.countries)
        coVerify(exactly = 2) { alertsClient.fetchAlerts(any()) }
    }
}
