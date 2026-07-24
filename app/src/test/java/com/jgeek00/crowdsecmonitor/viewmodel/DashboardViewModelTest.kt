package com.jgeek00.crowdsecmonitor.viewmodel

import androidx.compose.ui.graphics.Color
import com.jgeek00.crowdsecmonitor.MainDispatcherRule
import com.jgeek00.crowdsecmonitor.constants.chartColors
import com.jgeek00.crowdsecmonitor.data.api.CrowdSecApiClient
import com.jgeek00.crowdsecmonitor.data.api.statistics.StatisticsApiClient
import com.jgeek00.crowdsecmonitor.data.models.LoadingResult
import com.jgeek00.crowdsecmonitor.data.models.StatisticsResponse
import com.jgeek00.crowdsecmonitor.data.repository.PreferencesRepository
import com.jgeek00.crowdsecmonitor.fixtures.TestFixtures
import com.jgeek00.crowdsecmonitor.session.SessionManager
import com.jgeek00.crowdsecmonitor.utils.DashboardItemData
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class DashboardViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val preferencesRepository = mockk<PreferencesRepository>(relaxed = true)
    private val sessionManager = mockk<SessionManager>(relaxed = true)
    private val apiClient = mockk<CrowdSecApiClient>(relaxed = true)
    private val statisticsClient = mockk<StatisticsApiClient>(relaxed = true)
    private lateinit var vm: DashboardViewModel

    @Before
    fun setUp() {
        every { sessionManager.apiClient } returns null
        every { preferencesRepository.topItemsDashboard } returns flowOf(5)
        vm = DashboardViewModel(preferencesRepository, sessionManager)
    }

    @Test
    fun `initial state is Loading`() {
        assertEquals(LoadingResult.Loading, vm.state)
        assertFalse(vm.isRefreshing)
    }

    @Test
    fun `fetchDashboardData sets state to Success`() = runTest {
        every { sessionManager.apiClient } returns apiClient
        every { apiClient.statistics } returns statisticsClient
        val stats = TestFixtures.statisticsResponse()
        coEvery { statisticsClient.fetchStatistics(amount = any()) } returns TestFixtures.successResponse(stats)

        vm.fetchDashboardData()
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        val result = vm.state
        assertTrue(result is LoadingResult.Success)
        assertEquals(stats, (result as LoadingResult.Success).value)
    }

    @Test
    fun `fetchDashboardData sets state to Failure on exception`() = runTest {
        every { sessionManager.apiClient } returns apiClient
        every { apiClient.statistics } returns statisticsClient
        coEvery { statisticsClient.fetchStatistics(amount = any()) } throws Exception("test error")

        vm.fetchDashboardData()
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        val result = vm.state
        assertTrue(result is LoadingResult.Failure)
    }

    @Test
    fun `refresh sets isRefreshing and sets state to Success`() = runTest {
        every { sessionManager.apiClient } returns apiClient
        every { apiClient.statistics } returns statisticsClient
        val stats = TestFixtures.statisticsResponse()
        coEvery { statisticsClient.fetchStatistics(amount = any()) } returns TestFixtures.successResponse(stats)

        vm.refresh()
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        val result = vm.state
        assertTrue(result is LoadingResult.Success)
        assertEquals(stats, (result as LoadingResult.Success).value)
        assertFalse(vm.isRefreshing)
    }

    @Test
    fun `refresh sets state to Failure on exception`() = runTest {
        every { sessionManager.apiClient } returns apiClient
        every { apiClient.statistics } returns statisticsClient
        coEvery { statisticsClient.fetchStatistics(amount = any()) } throws Exception("test error")

        vm.refresh()
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        assertTrue(vm.state is LoadingResult.Failure)
        assertFalse(vm.isRefreshing)
    }

    @Test
    fun `reset sets state to Loading`() {
        vm.reset()
        assertEquals(LoadingResult.Loading, vm.state)
    }

    @Test
    fun `generateViewData calculates percentages correctly`() {
        val items = listOf(
            DashboardItemData(item = "US", value = 200),
            DashboardItemData(item = "CN", value = 100),
            DashboardItemData(item = "RU", value = 100)
        )
        val result = vm.generateViewData(items)

        assertEquals(3, result.size)
        assertEquals(0.5, result[0].percentage, 0.001)
        assertEquals(0.25, result[1].percentage, 0.001)
        assertEquals(0.25, result[2].percentage, 0.001)
    }

    @Test
    fun `generateViewData uses chartColors for indices within range and Gray for out of range`() {
        val items = (0 until chartColors.size + 2).map { DashboardItemData(item = "Item$it", value = 1) }
        val result = vm.generateViewData(items)

        assertEquals(items.size, result.size)
        for (i in 0 until chartColors.size) {
            assertEquals(chartColors[i], result[i].color)
        }
        assertEquals(Color.Gray, result[chartColors.size].color)
        assertEquals(Color.Gray, result[chartColors.size + 1].color)
    }

    @Test
    fun `generateViewData returns zero percentage when total is zero`() {
        val items = listOf(
            DashboardItemData(item = "A", value = 0),
            DashboardItemData(item = "B", value = 0)
        )
        val result = vm.generateViewData(items)

        assertEquals(0.0, result[0].percentage, 0.001)
        assertEquals(0.0, result[1].percentage, 0.001)
    }

    @Test
    fun `fetchDashboardData does nothing when apiClient is null`() = runTest {
        vm.fetchDashboardData()
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        assertEquals(LoadingResult.Loading, vm.state)
    }
}
