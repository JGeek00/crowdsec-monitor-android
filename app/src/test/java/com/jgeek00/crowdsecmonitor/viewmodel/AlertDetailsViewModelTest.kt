package com.jgeek00.crowdsecmonitor.viewmodel

import com.jgeek00.crowdsecmonitor.MainDispatcherRule
import com.jgeek00.crowdsecmonitor.data.api.AlertsApiClient
import com.jgeek00.crowdsecmonitor.data.api.CrowdSecApiClient
import com.jgeek00.crowdsecmonitor.data.models.AlertDetailsResponse
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

class AlertDetailsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val sessionManager = mockk<SessionManager>(relaxed = true)
    private val apiClient = mockk<CrowdSecApiClient>(relaxed = true)
    private val alertsClient = mockk<AlertsApiClient>(relaxed = true)
    private lateinit var vm: AlertDetailsViewModel

    @Before
    fun setUp() {
        every { sessionManager.apiClient } returns apiClient
        every { apiClient.alerts } returns alertsClient
        vm = AlertDetailsViewModel(sessionManager)
    }

    @Test
    fun `initialize fetches data and sets state to Success`() = runTest {
        val alertDetails = mockk<AlertDetailsResponse>(relaxed = true)
        coEvery { alertsClient.fetchAlertDetails(1) } returns TestFixtures.successResponse(alertDetails)

        vm.initialize(1)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        val result = vm.state
        assertTrue(result is LoadingResult.Success)
        assertEquals(alertDetails, (result as LoadingResult.Success).value)
    }

    @Test
    fun `initialize duplicate call is no-op`() = runTest {
        val alertDetails = mockk<AlertDetailsResponse>(relaxed = true)
        coEvery { alertsClient.fetchAlertDetails(1) } returns TestFixtures.successResponse(alertDetails)

        vm.initialize(1)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        vm.initialize(1)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { alertsClient.fetchAlertDetails(1) }
    }

    @Test
    fun `initialize sets state to Failure on exception`() = runTest {
        coEvery { alertsClient.fetchAlertDetails(1) } throws Exception("test error")

        vm.initialize(1)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        assertTrue(vm.state is LoadingResult.Failure)
    }

    @Test
    fun `refresh calls fetchAlertDetails and toggles isRefreshing`() = runTest {
        val alertDetails = mockk<AlertDetailsResponse>(relaxed = true)
        coEvery { alertsClient.fetchAlertDetails(any()) } returns TestFixtures.successResponse(alertDetails)

        vm.initialize(1)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        vm.refresh(1)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        assertFalse(vm.isRefreshing)
        val result = vm.state
        assertTrue(result is LoadingResult.Success)
    }
}
