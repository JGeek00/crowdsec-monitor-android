package com.jgeek00.crowdsecmonitor.viewmodel

import com.jgeek00.crowdsecmonitor.MainDispatcherRule
import com.jgeek00.crowdsecmonitor.data.api.AlertsApiClient
import com.jgeek00.crowdsecmonitor.data.api.CrowdSecApiClient
import com.jgeek00.crowdsecmonitor.data.models.*
import com.jgeek00.crowdsecmonitor.session.SessionManager
import io.mockk.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class AlertsListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun mockEnvironment(): Triple<SessionManager, CrowdSecApiClient, AlertsApiClient> {
        val alertsClient = mockk<AlertsApiClient>(relaxed = true)
        val apiClient = mockk<CrowdSecApiClient>(relaxed = true).also { every { it.alerts } returns alertsClient }
        val sm = mockk<SessionManager>(relaxed = true).also {
            every { it.apiClient } returns apiClient
            every { it.alertsRefreshEvent } returns MutableSharedFlow<Unit>()
        }
        return Triple(sm, apiClient, alertsClient)
    }

    private val defaultFilters = AlertsRequestFilters(
        countries = emptyList(), scenarios = emptyList(),
        ipOwners = emptyList(), targets = emptyList()
    )

    @Test
    fun `initial state is Loading`() {
        val (sm) = mockEnvironment()
        val vm = AlertsListViewModel(sm)
        assertTrue(vm.state is LoadingResult.Loading)
    }

    @Test
    fun `initialFetchAlerts calls fetchAlerts and state becomes Success`() = runTest {
        val (sm, _, alertsClient) = mockEnvironment()
        val response = AlertsListResponse(
            filtering = AlertsListResponseFiltering(emptyList(), emptyList(), emptyList(), emptyList()),
            items = listOf(AlertsListResponseAlert(
                id = 1, uuid = "u1", scenario = "s1", scenarioVersion = "1.0",
                scenarioHash = "h1", message = "m1", capacity = 10, leakspeed = "1s",
                simulated = false, remediation = true, eventsCount = 1, machineId = "m1",
                source = AlertSource(scope = "ip", value = "1.2.3.4"),
                meta = emptyList(), events = emptyList(),
                crowdsecCreatedAt = "2024-01-01T00:00:00Z", startAt = "2024-01-01T00:00:00Z",
                stopAt = "2024-01-01T01:00:00Z"
            )),
            pagination = AlertsListResponsePagination(page = 0, amount = 1, total = 50)
        )
        coEvery { alertsClient.fetchAlerts(any()) } returns HttpResponse(successful = true, statusCode = 200, body = response)

        val vm = AlertsListViewModel(sm)
        advanceUntilIdle()

        assertTrue(vm.state is LoadingResult.Success)
        assertEquals(response, (vm.state as LoadingResult.Success).value)
    }

    @Test
    fun `initialFetchAlerts when data already exists does nothing`() = runTest {
        val (sm, _, alertsClient) = mockEnvironment()
        val response = AlertsListResponse(
            filtering = AlertsListResponseFiltering(emptyList(), emptyList(), emptyList(), emptyList()),
            items = listOf(AlertsListResponseAlert(
                id = 1, uuid = "u1", scenario = "s1", scenarioVersion = "1.0",
                scenarioHash = "h1", message = "m1", capacity = 10, leakspeed = "1s",
                simulated = false, remediation = true, eventsCount = 1, machineId = "m1",
                source = AlertSource(scope = "ip", value = "1.2.3.4"),
                meta = emptyList(), events = emptyList(),
                crowdsecCreatedAt = "2024-01-01T00:00:00Z", startAt = "2024-01-01T00:00:00Z",
                stopAt = "2024-01-01T01:00:00Z"
            )),
            pagination = AlertsListResponsePagination(page = 0, amount = 1, total = 50)
        )
        coEvery { alertsClient.fetchAlerts(any()) } returns HttpResponse(successful = true, statusCode = 200, body = response)

        val vm = AlertsListViewModel(sm)
        advanceUntilIdle()

        vm.initialFetchAlerts()
        advanceUntilIdle()

        coVerify(exactly = 1) { alertsClient.fetchAlerts(any()) }
    }

    @Test
    fun `fetch failure sets state to Failure`() = runTest {
        val (sm, _, alertsClient) = mockEnvironment()
        coEvery { alertsClient.fetchAlerts(any()) } throws RuntimeException("Network error")

        val vm = AlertsListViewModel(sm)
        advanceUntilIdle()

        assertTrue(vm.state is LoadingResult.Failure)
    }

    @Test
    fun `refreshAlerts toggles isRefreshing`() = runTest {
        val (sm, _, alertsClient) = mockEnvironment()
        coEvery { alertsClient.fetchAlerts(any()) } returns HttpResponse(
            successful = true, statusCode = 200,
            body = AlertsListResponse(
                filtering = AlertsListResponseFiltering(emptyList(), emptyList(), emptyList(), emptyList()),
                items = emptyList(),
                pagination = AlertsListResponsePagination(page = 0, amount = 0, total = 0)
            )
        )

        val vm = AlertsListViewModel(sm)
        advanceUntilIdle()

        vm.refreshAlerts()
        advanceUntilIdle()

        assertFalse(vm.isRefreshing)
        coVerify(exactly = 2) { alertsClient.fetchAlerts(any()) }
    }

    @Test
    fun `fetchMore merges pages and deduplicates by id`() = runTest {
        val (sm, _, alertsClient) = mockEnvironment()
        val page0 = AlertsListResponse(
            filtering = AlertsListResponseFiltering(emptyList(), emptyList(), emptyList(), emptyList()),
            items = (0 until 50).map { i ->
                AlertsListResponseAlert(id = i, uuid = "u$i", scenario = "s$i", scenarioVersion = "1.0",
                    scenarioHash = "h$i", message = "m$i", capacity = 10, leakspeed = "1s",
                    simulated = false, remediation = true, eventsCount = 1, machineId = "m$i",
                    source = AlertSource(scope = "ip", value = "1.2.3.4"),
                    meta = emptyList(), events = emptyList(),
                    crowdsecCreatedAt = "2024-01-01T00:00:00Z", startAt = "2024-01-01T00:00:00Z",
                    stopAt = "2024-01-01T01:00:00Z")
            },
            pagination = AlertsListResponsePagination(page = 1, amount = 50, total = 100)
        )
        val page1 = page0.copy(
            items = (40 until 90).map { i ->
                AlertsListResponseAlert(id = i, uuid = "u$i", scenario = "s$i", scenarioVersion = "1.0",
                    scenarioHash = "h$i", message = "m$i", capacity = 10, leakspeed = "1s",
                    simulated = false, remediation = true, eventsCount = 1, machineId = "m$i",
                    source = AlertSource(scope = "ip", value = "1.2.3.4"),
                    meta = emptyList(), events = emptyList(),
                    crowdsecCreatedAt = "2024-01-01T00:00:00Z", startAt = "2024-01-01T00:00:00Z",
                    stopAt = "2024-01-01T01:00:00Z")
            },
            pagination = AlertsListResponsePagination(page = 2, amount = 50, total = 100)
        )
        coEvery { alertsClient.fetchAlerts(any()) } returnsMany listOf(
            HttpResponse(successful = true, statusCode = 200, body = page0),
            HttpResponse(successful = true, statusCode = 200, body = page1)
        )

        val vm = AlertsListViewModel(sm)
        advanceUntilIdle()

        vm.fetchMore()
        advanceUntilIdle()

        val data = (vm.state as LoadingResult.Success).value as AlertsListResponse
        assertEquals(90, data.items.size)
        assertEquals(0, data.items.first().id)
        assertEquals(89, data.items.last().id)
    }

    @Test
    fun `fetchMore at end does nothing`() = runTest {
        val (sm, _, alertsClient) = mockEnvironment()
        val response = AlertsListResponse(
            filtering = AlertsListResponseFiltering(emptyList(), emptyList(), emptyList(), emptyList()),
            items = (0 until 50).map { i ->
                AlertsListResponseAlert(id = i, uuid = "u$i", scenario = "s$i", scenarioVersion = "1.0",
                    scenarioHash = "h$i", message = "m$i", capacity = 10, leakspeed = "1s",
                    simulated = false, remediation = true, eventsCount = 1, machineId = "m$i",
                    source = AlertSource(scope = "ip", value = "1.2.3.4"),
                    meta = emptyList(), events = emptyList(),
                    crowdsecCreatedAt = "2024-01-01T00:00:00Z", startAt = "2024-01-01T00:00:00Z",
                    stopAt = "2024-01-01T01:00:00Z")
            },
            pagination = AlertsListResponsePagination(page = 2, amount = 50, total = 100)
        )
        coEvery { alertsClient.fetchAlerts(any()) } returns HttpResponse(successful = true, statusCode = 200, body = response)

        val vm = AlertsListViewModel(sm)
        advanceUntilIdle()

        vm.fetchMore()
        advanceUntilIdle()

        coVerify(exactly = 1) { alertsClient.fetchAlerts(any()) }
    }

    @Test
    fun `deleteAlert success calls deleteAlert refreshes and returns true`() = runTest {
        val (sm, _, alertsClient) = mockEnvironment()
        val response = AlertsListResponse(
            filtering = AlertsListResponseFiltering(emptyList(), emptyList(), emptyList(), emptyList()),
            items = emptyList(), pagination = AlertsListResponsePagination(page = 0, amount = 0, total = 0)
        )
        coEvery { alertsClient.fetchAlerts(any()) } returns HttpResponse(successful = true, statusCode = 200, body = response)
        coEvery { alertsClient.deleteAlert(any()) } returns HttpResponse(successful = true, statusCode = 200, body = EmptyResponse())

        val vm = AlertsListViewModel(sm)
        vm.selectAlert(1)
        advanceUntilIdle()

        var result: Boolean? = null
        vm.deleteAlert(1) { result = it }
        advanceUntilIdle()

        coVerify { alertsClient.deleteAlert(1) }
        assertNull(vm.selectedAlert)
        assertTrue(result!!)
    }

    @Test
    fun `deleteAlert failure returns false`() = runTest {
        val (sm, _, alertsClient) = mockEnvironment()
        val response = AlertsListResponse(
            filtering = AlertsListResponseFiltering(emptyList(), emptyList(), emptyList(), emptyList()),
            items = emptyList(), pagination = AlertsListResponsePagination(page = 0, amount = 0, total = 0)
        )
        coEvery { alertsClient.fetchAlerts(any()) } returns HttpResponse(successful = true, statusCode = 200, body = response)
        coEvery { alertsClient.deleteAlert(any()) } throws RuntimeException("Deletion failed")

        val vm = AlertsListViewModel(sm)
        advanceUntilIdle()

        var result: Boolean? = null
        vm.deleteAlert(1) { result = it }
        advanceUntilIdle()

        assertFalse(result!!)
    }

    @Test
    fun `selectAlert updates selectedAlert`() {
        val (sm) = mockEnvironment()
        val vm = AlertsListViewModel(sm)
        vm.selectAlert(42)
        assertEquals(42, vm.selectedAlert)
        vm.selectAlert(null)
        assertNull(vm.selectedAlert)
    }

    @Test
    fun `reset restores defaults`() = runTest {
        val (sm, _, alertsClient) = mockEnvironment()
        coEvery { alertsClient.fetchAlerts(any()) } returns HttpResponse(
            successful = true, statusCode = 200,
            body = AlertsListResponse(AlertsListResponseFiltering(emptyList(), emptyList(), emptyList(), emptyList()), emptyList(), AlertsListResponsePagination(0, 0, 0))
        )
        val vm = AlertsListViewModel(sm)
        advanceUntilIdle()
        vm.selectAlert(42)

        vm.reset()

        assertTrue(vm.state is LoadingResult.Loading)
        assertNull(vm.selectedAlert)
        assertFalse(vm.deletingAlertProcess)
        assertEquals(defaultFilters, vm.filters)
    }

    @Test
    fun `resetFilters resets to defaults and triggers fetch`() = runTest {
        val (sm, _, alertsClient) = mockEnvironment()
        coEvery { alertsClient.fetchAlerts(any()) } returns HttpResponse(
            successful = true, statusCode = 200,
            body = AlertsListResponse(AlertsListResponseFiltering(emptyList(), emptyList(), emptyList(), emptyList()), emptyList(), AlertsListResponsePagination(0, 0, 0))
        )
        val vm = AlertsListViewModel(sm)
        advanceUntilIdle()

        vm.updateFilters(AlertsRequestFilters(countries = listOf("FR"), scenarios = emptyList(), ipOwners = emptyList(), targets = emptyList()))
        vm.resetFilters()
        advanceUntilIdle()

        assertEquals(defaultFilters, vm.filters)
        coVerify(exactly = 2) { alertsClient.fetchAlerts(any()) }
    }

    @Test
    fun `applyFilters applies current filters and triggers fetch`() = runTest {
        val (sm, _, alertsClient) = mockEnvironment()
        coEvery { alertsClient.fetchAlerts(any()) } returns HttpResponse(
            successful = true, statusCode = 200,
            body = AlertsListResponse(AlertsListResponseFiltering(emptyList(), emptyList(), emptyList(), emptyList()), emptyList(), AlertsListResponsePagination(0, 0, 0))
        )
        val vm = AlertsListViewModel(sm)
        advanceUntilIdle()

        val newFilters = AlertsRequestFilters(countries = listOf("FR"), scenarios = listOf("test-scenario"), ipOwners = emptyList(), targets = emptyList())
        vm.updateFilters(newFilters)
        vm.applyFilters()
        advanceUntilIdle()

        assertEquals(listOf("FR"), vm.filters.countries)
        coVerify(exactly = 2) { alertsClient.fetchAlerts(any()) }
    }
}
