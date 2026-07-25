package com.jgeek00.crowdsecmonitor.viewmodel

import com.jgeek00.crowdsecmonitor.MainDispatcherRule
import com.jgeek00.crowdsecmonitor.data.api.AlertsApiClient
import com.jgeek00.crowdsecmonitor.data.api.CrowdSecApiClient
import com.jgeek00.crowdsecmonitor.data.models.LoadingResult
import com.jgeek00.crowdsecmonitor.fixtures.TestFixtures
import com.jgeek00.crowdsecmonitor.session.SessionManager
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AlertDetailsViewModelBranchTest {

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
    fun `initialize with no apiClient does not crash`() = runTest {
        every { sessionManager.apiClient } returns null
        val vm2 = AlertDetailsViewModel(sessionManager)

        vm2.initialize(1)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        assertTrue(vm2.state is LoadingResult.Loading)
    }

    @Test
    fun `initialize sets state to Loading before fetch`() = runTest {
        coEvery { alertsClient.fetchAlertDetails(any()) } returns TestFixtures.successResponse(mockk(relaxed = true))

        vm.initialize(1)
        assertTrue(vm.state is LoadingResult.Loading)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        assertTrue(vm.state is LoadingResult.Success)
    }

    @Test
    fun `refresh on error sets Failure state`() = runTest {
        vm.initialize(1)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        coEvery { alertsClient.fetchAlertDetails(any()) } throws Exception("test error")
        vm.refresh(1)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        assertTrue(vm.state is LoadingResult.Failure)
        assertTrue(vm.isRefreshing == false)
    }
}
