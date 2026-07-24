package com.jgeek00.crowdsecmonitor.viewmodel

import com.jgeek00.crowdsecmonitor.MainDispatcherRule
import com.jgeek00.crowdsecmonitor.constants.Enums
import com.jgeek00.crowdsecmonitor.constants.chartColors
import com.jgeek00.crowdsecmonitor.data.api.CrowdSecApiClient
import com.jgeek00.crowdsecmonitor.data.api.statistics.CountriesStatisticsApiClient
import com.jgeek00.crowdsecmonitor.data.api.statistics.IpOwnersStatisticsApiClient
import com.jgeek00.crowdsecmonitor.data.api.statistics.ScenariosStatisticsApiClient
import com.jgeek00.crowdsecmonitor.data.api.statistics.TargetsStatisticsApiClient
import com.jgeek00.crowdsecmonitor.data.models.LoadingResult
import com.jgeek00.crowdsecmonitor.data.models.TopCountry
import com.jgeek00.crowdsecmonitor.data.models.TopIpOwner
import com.jgeek00.crowdsecmonitor.data.models.TopScenario
import com.jgeek00.crowdsecmonitor.data.models.TopTarget
import com.jgeek00.crowdsecmonitor.fixtures.TestFixtures
import com.jgeek00.crowdsecmonitor.session.SessionManager
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class FullListDashboardViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val sessionManager = mockk<SessionManager>(relaxed = true)
    private val apiClient = mockk<CrowdSecApiClient>(relaxed = true)
    private val countriesClient = mockk<CountriesStatisticsApiClient>(relaxed = true)
    private val ipOwnersClient = mockk<IpOwnersStatisticsApiClient>(relaxed = true)
    private val scenariosClient = mockk<ScenariosStatisticsApiClient>(relaxed = true)
    private val targetsClient = mockk<TargetsStatisticsApiClient>(relaxed = true)

    @Before
    fun setUp() {
        every { sessionManager.apiClient } returns null
    }

    @Test
    fun `initial fetch for COUNTRY type returns success`() = runTest {
        every { sessionManager.apiClient } returns apiClient
        every { apiClient.statistics.countries } returns countriesClient
        val countries = listOf(
            TopCountry(countryCode = "US", amount = 200),
            TopCountry(countryCode = "CN", amount = 100)
        )
        coEvery { countriesClient.fetchCountriesStatistics() } returns TestFixtures.successResponse(countries)

        val vm = FullListDashboardViewModel(Enums.DashboardItemType.COUNTRY, sessionManager)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        val result = vm.state
        assertTrue(result is LoadingResult.Success)
        assertEquals(2, (result as LoadingResult.Success).value.size)
        assertEquals("US", result.value[0].item)
        assertEquals("CN", result.value[1].item)
    }

    @Test
    fun `initial fetch for IP_OWNER type returns success`() = runTest {
        every { sessionManager.apiClient } returns apiClient
        every { apiClient.statistics.ipOwners } returns ipOwnersClient
        val owners = listOf(TopIpOwner(ipOwner = "AS12345", amount = 400))
        coEvery { ipOwnersClient.fetchIpOwnersStatistics() } returns TestFixtures.successResponse(owners)

        val vm = FullListDashboardViewModel(Enums.DashboardItemType.IP_OWNER, sessionManager)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        val result = vm.state
        assertTrue(result is LoadingResult.Success)
        assertEquals(1, (result as LoadingResult.Success).value.size)
        assertEquals("AS12345", result.value[0].item)
    }

    @Test
    fun `initial fetch for SCENARIO type returns success`() = runTest {
        every { sessionManager.apiClient } returns apiClient
        every { apiClient.statistics.scenarios } returns scenariosClient
        val scenarios = listOf(TopScenario(scenario = "crowdsec/ssh-bf", amount = 300))
        coEvery { scenariosClient.fetchScenariosStatistics() } returns TestFixtures.successResponse(scenarios)

        val vm = FullListDashboardViewModel(Enums.DashboardItemType.SCENARIO, sessionManager)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        val result = vm.state
        assertTrue(result is LoadingResult.Success)
        assertEquals(1, (result as LoadingResult.Success).value.size)
        assertEquals("crowdsec/ssh-bf", result.value[0].item)
    }

    @Test
    fun `initial fetch for TARGET type returns success`() = runTest {
        every { sessionManager.apiClient } returns apiClient
        every { apiClient.statistics.targets } returns targetsClient
        val targets = listOf(TopTarget(target = "1.2.3.4", amount = 500))
        coEvery { targetsClient.fetchTargetsStatistics() } returns TestFixtures.successResponse(targets)

        val vm = FullListDashboardViewModel(Enums.DashboardItemType.TARGET, sessionManager)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        val result = vm.state
        assertTrue(result is LoadingResult.Success)
        assertEquals(1, (result as LoadingResult.Success).value.size)
        assertEquals("1.2.3.4", result.value[0].item)
    }

    @Test
    fun `fetch returns Failure on exception`() = runTest {
        every { sessionManager.apiClient } returns apiClient
        every { apiClient.statistics.countries } returns countriesClient
        coEvery { countriesClient.fetchCountriesStatistics() } throws Exception("test error")

        val vm = FullListDashboardViewModel(Enums.DashboardItemType.COUNTRY, sessionManager)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        assertTrue(vm.state is LoadingResult.Failure)
    }

    @Test
    fun `chartData combines items beyond chartColors into Others`() = runTest {
        every { sessionManager.apiClient } returns apiClient
        every { apiClient.statistics.countries } returns countriesClient
        val manyCountries = (0 until chartColors.size + 3).map { i ->
            TopCountry(countryCode = "C$i", amount = 10)
        }
        coEvery { countriesClient.fetchCountriesStatistics() } returns TestFixtures.successResponse(manyCountries)

        val vm = FullListDashboardViewModel(Enums.DashboardItemType.COUNTRY, sessionManager)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        val chartData = vm.chartData
        assertEquals(chartColors.size + 1, chartData.size)
        assertEquals("Others", chartData.last().item)
        assertEquals(30, chartData.last().value)
    }

    @Test
    fun `chartData returns all items when count is within chartColors size`() = runTest {
        every { sessionManager.apiClient } returns apiClient
        every { apiClient.statistics.countries } returns countriesClient
        val fewCountries = listOf(
            TopCountry(countryCode = "US", amount = 200),
            TopCountry(countryCode = "CN", amount = 100)
        )
        coEvery { countriesClient.fetchCountriesStatistics() } returns TestFixtures.successResponse(fewCountries)

        val vm = FullListDashboardViewModel(Enums.DashboardItemType.COUNTRY, sessionManager)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        val chartData = vm.chartData
        assertEquals(2, chartData.size)
        assertEquals("US", chartData[0].item)
        assertEquals("CN", chartData[1].item)
    }

    @Test
    fun `chartData returns empty list when state is Loading`() {
        every { sessionManager.apiClient } returns apiClient

        val vm = FullListDashboardViewModel(Enums.DashboardItemType.COUNTRY, sessionManager)

        assertTrue(vm.chartData.isEmpty())
    }
}
