package com.jgeek00.crowdsecmonitor.viewmodel

import com.jgeek00.crowdsecmonitor.MainDispatcherRule
import com.jgeek00.crowdsecmonitor.data.api.BlocklistsApiClient
import com.jgeek00.crowdsecmonitor.data.api.CrowdSecApiClient
import com.jgeek00.crowdsecmonitor.data.models.BlocklistDataResponse
import com.jgeek00.crowdsecmonitor.data.models.BlocklistDataResponseData
import com.jgeek00.crowdsecmonitor.data.models.BlocklistType
import com.jgeek00.crowdsecmonitor.data.models.LoadingResult
import com.jgeek00.crowdsecmonitor.data.models.ToggleBlocklistRequest
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

class BlocklistDetailsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val sessionManager = mockk<SessionManager>(relaxed = true)
    private val apiClient = mockk<CrowdSecApiClient>(relaxed = true)
    private val blocklistsClient = mockk<BlocklistsApiClient>(relaxed = true)
    private lateinit var vm: BlocklistDetailsViewModel

    @Before
    fun setUp() {
        every { sessionManager.apiClient } returns null
        vm = BlocklistDetailsViewModel(sessionManager)
    }

    @Test
    fun `initialize fetches data and sets state to Success`() = runTest {
        every { sessionManager.apiClient } returns apiClient
        every { apiClient.blocklists } returns blocklistsClient
        val data = BlocklistDataResponse(
            BlocklistDataResponseData(
                id = "1", name = "Test", enabled = true, countIps = 100,
                type = BlocklistType.API,
                blocklistIps = listOf("1.2.3.4")
            )
        )
        coEvery { blocklistsClient.fetchBlocklistData("1") } returns TestFixtures.successResponse(data)

        vm.initialize("1")
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        val result = vm.state
        assertTrue(result is LoadingResult.Success)
        assertEquals(data, (result as LoadingResult.Success).value)
        assertEquals(1, vm.ipsRound)
        assertFalse(vm.searchPresented)
        assertEquals("", vm.searchText)
    }

    @Test
    fun `initialize duplicate call is no-op`() = runTest {
        every { sessionManager.apiClient } returns apiClient
        every { apiClient.blocklists } returns blocklistsClient
        val data = BlocklistDataResponse(
            BlocklistDataResponseData(
                id = "1", name = "Test", enabled = true, countIps = 100,
                type = BlocklistType.API,
                blocklistIps = listOf("1.2.3.4")
            )
        )
        coEvery { blocklistsClient.fetchBlocklistData("1") } returns TestFixtures.successResponse(data)

        vm.initialize("1")
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        vm.initialize("1")
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { blocklistsClient.fetchBlocklistData("1") }
    }

    @Test
    fun `initialize sets state to Failure on exception`() = runTest {
        every { sessionManager.apiClient } returns apiClient
        every { apiClient.blocklists } returns blocklistsClient
        coEvery { blocklistsClient.fetchBlocklistData("1") } throws Exception("test error")

        vm.initialize("1")
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        assertTrue(vm.state is LoadingResult.Failure)
    }

    @Test
    fun `refresh fetches data and toggles isRefreshing`() = runTest {
        every { sessionManager.apiClient } returns apiClient
        every { apiClient.blocklists } returns blocklistsClient
        val data = BlocklistDataResponse(
            BlocklistDataResponseData(
                id = "1", name = "Test", enabled = true, countIps = 100,
                type = BlocklistType.API,
                blocklistIps = listOf("1.2.3.4")
            )
        )
        coEvery { blocklistsClient.fetchBlocklistData("1") } returns TestFixtures.successResponse(data)

        vm.initialize("1")
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        vm.refresh("1")
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        assertFalse(vm.isRefreshing)
        assertTrue(vm.state is LoadingResult.Success)
    }

    @Test
    fun `updateBlocklistId changes ID and fetches with showLoading`() = runTest {
        every { sessionManager.apiClient } returns apiClient
        every { apiClient.blocklists } returns blocklistsClient
        val data1 = BlocklistDataResponse(
            BlocklistDataResponseData(
                id = "1", name = "Old", enabled = true, countIps = 100,
                type = BlocklistType.API,
                blocklistIps = listOf("1.2.3.4")
            )
        )
        val data2 = BlocklistDataResponse(
            BlocklistDataResponseData(
                id = "2", name = "New", enabled = true, countIps = 50,
                type = BlocklistType.API,
                blocklistIps = listOf("5.6.7.8")
            )
        )
        coEvery { blocklistsClient.fetchBlocklistData("1") } returns TestFixtures.successResponse(data1)
        coEvery { blocklistsClient.fetchBlocklistData("2") } returns TestFixtures.successResponse(data2)

        vm.initialize("1")
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        vm.updateBlocklistId("2")
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        assertEquals(data2, (vm.state as LoadingResult.Success).value)
        assertEquals(1, vm.ipsRound)
        assertEquals("", vm.searchText)
    }

    @Test
    fun `incrementIpsRound increments the counter`() {
        assertEquals(1, vm.ipsRound)
        vm.incrementIpsRound()
        assertEquals(2, vm.ipsRound)
        vm.incrementIpsRound()
        assertEquals(3, vm.ipsRound)
    }

    @Test
    fun `updateSearchPresented sets value and clears searchText when false`() {
        vm.updateSearchText("test")
        vm.updateSearchPresented(true)
        assertTrue(vm.searchPresented)
        assertEquals("test", vm.searchText)

        vm.updateSearchPresented(false)
        assertFalse(vm.searchPresented)
        assertEquals("", vm.searchText)
    }

    @Test
    fun `updateSearchText updates search text`() {
        vm.updateSearchText("hello")
        assertEquals("hello", vm.searchText)
    }

    @Test
    fun `toggleBlocklist success calls API and refreshes data`() = runTest {
        every { sessionManager.apiClient } returns apiClient
        every { apiClient.blocklists } returns blocklistsClient
        val data = BlocklistDataResponse(
            BlocklistDataResponseData(
                id = "1", name = "Test", enabled = true, countIps = 100,
                type = BlocklistType.API,
                blocklistIps = listOf("1.2.3.4")
            )
        )
        coEvery { blocklistsClient.fetchBlocklistData("1") } returns TestFixtures.successResponse(data)
        coEvery { blocklistsClient.toggleBlocklist("1", any()) } returns TestFixtures.successResponse(mockk(relaxed = true))

        vm.initialize("1")
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        vm.toggleBlocklist("1", false)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        assertFalse(vm.processingModal)
        assertFalse(vm.errorToggleBlocklist)
        coVerify { blocklistsClient.toggleBlocklist("1", ToggleBlocklistRequest(enabled = false)) }
    }

    @Test
    fun `toggleBlocklist failure sets error flag`() = runTest {
        every { sessionManager.apiClient } returns apiClient
        every { apiClient.blocklists } returns blocklistsClient
        val data = BlocklistDataResponse(
            BlocklistDataResponseData(
                id = "1", name = "Test", enabled = true, countIps = 100,
                type = BlocklistType.API,
                blocklistIps = listOf("1.2.3.4")
            )
        )
        coEvery { blocklistsClient.fetchBlocklistData("1") } returns TestFixtures.successResponse(data)
        coEvery { blocklistsClient.toggleBlocklist("1", any()) } throws Exception("test error")

        vm.initialize("1")
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        vm.toggleBlocklist("1", true)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        assertFalse(vm.processingModal)
        assertTrue(vm.errorToggleBlocklist)
    }

    @Test
    fun `deleteBlocklist success sets deleted flag`() = runTest {
        every { sessionManager.apiClient } returns apiClient
        every { apiClient.blocklists } returns blocklistsClient
        coEvery { blocklistsClient.deleteBlocklist("1") } returns TestFixtures.successResponse(mockk(relaxed = true))

        vm.deleteBlocklist("1")
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        assertFalse(vm.processingModal)
        assertTrue(vm.blocklistDeletedSuccessfully)
        assertFalse(vm.errorDeleteBlocklist)
        coVerify { blocklistsClient.deleteBlocklist("1") }
    }

    @Test
    fun `deleteBlocklist failure sets error flag`() = runTest {
        every { sessionManager.apiClient } returns apiClient
        every { apiClient.blocklists } returns blocklistsClient
        coEvery { blocklistsClient.deleteBlocklist("1") } throws Exception("test error")

        vm.deleteBlocklist("1")
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        assertFalse(vm.processingModal)
        assertTrue(vm.errorDeleteBlocklist)
        assertFalse(vm.blocklistDeletedSuccessfully)
    }

    @Test
    fun `refreshBlocklist success calls API`() = runTest {
        every { sessionManager.apiClient } returns apiClient
        every { apiClient.blocklists } returns blocklistsClient
        coEvery { blocklistsClient.refreshBlocklist("1") } returns TestFixtures.successResponse(mockk(relaxed = true))

        vm.refreshBlocklist("1")
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        assertFalse(vm.processingModal)
        assertFalse(vm.errorRefreshBlocklist)
        coVerify { blocklistsClient.refreshBlocklist("1") }
    }

    @Test
    fun `refreshBlocklist failure sets error flag`() = runTest {
        every { sessionManager.apiClient } returns apiClient
        every { apiClient.blocklists } returns blocklistsClient
        coEvery { blocklistsClient.refreshBlocklist("1") } throws Exception("test error")

        vm.refreshBlocklist("1")
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        assertFalse(vm.processingModal)
        assertTrue(vm.errorRefreshBlocklist)
    }

    @Test
    fun `clearErrorRefreshBlocklist resets error`() {
        vm.clearErrorRefreshBlocklist()
        assertFalse(vm.errorRefreshBlocklist)
    }

    @Test
    fun `clearErrorToggleBlocklist resets error`() {
        vm.clearErrorToggleBlocklist()
        assertFalse(vm.errorToggleBlocklist)
    }

    @Test
    fun `clearErrorDeleteBlocklist resets error`() {
        vm.clearErrorDeleteBlocklist()
        assertFalse(vm.errorDeleteBlocklist)
    }

    @Test
    fun `clearBlocklistDeletedSuccessfully resets flag`() {
        vm.clearBlocklistDeletedSuccessfully()
        assertFalse(vm.blocklistDeletedSuccessfully)
    }
}
