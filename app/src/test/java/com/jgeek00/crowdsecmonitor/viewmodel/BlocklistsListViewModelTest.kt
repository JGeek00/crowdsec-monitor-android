package com.jgeek00.crowdsecmonitor.viewmodel

import com.jgeek00.crowdsecmonitor.MainDispatcherRule
import com.jgeek00.crowdsecmonitor.constants.Defaults
import com.jgeek00.crowdsecmonitor.data.api.BlocklistsApiClient
import com.jgeek00.crowdsecmonitor.data.api.CrowdSecApiClient
import com.jgeek00.crowdsecmonitor.data.models.BlocklistsRequest
import com.jgeek00.crowdsecmonitor.data.models.EmptyResponse
import com.jgeek00.crowdsecmonitor.data.models.HttpClientException
import com.jgeek00.crowdsecmonitor.data.models.LoadingResult
import com.jgeek00.crowdsecmonitor.data.models.RefreshBlocklistsResponse
import com.jgeek00.crowdsecmonitor.data.models.ToggleBlocklistRequest
import com.jgeek00.crowdsecmonitor.fixtures.TestFixtures
import com.jgeek00.crowdsecmonitor.session.SessionManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class BlocklistsListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun mockEnvironment(): Triple<SessionManager, CrowdSecApiClient, BlocklistsApiClient> {
        val blocklistsClient = mockk<BlocklistsApiClient>(relaxed = true)
        val apiClient = mockk<CrowdSecApiClient>(relaxed = true).also { every { it.blocklists } returns blocklistsClient }
        val sm = mockk<SessionManager>(relaxed = true).also {
            every { it.apiClient } returns null
        }
        return Triple(sm, apiClient, blocklistsClient)
    }

    @Test
    fun `initial state is Loading`() {
        val (sm) = mockEnvironment()
        val vm = BlocklistsListViewModel(sm)
        assertEquals(LoadingResult.Loading, vm.state)
        assertEquals(BlocklistsRequest(offset = 0, limit = Defaults.BLOCKLISTS_AMOUNT_BATCH), vm.requestParams)
        assertNull(vm.selectedListName)
        assertFalse(vm.isRefreshing)
        assertFalse(vm.isLoadingMore)
        assertFalse(vm.processingModal)
        assertFalse(vm.errorDisableBlocklist)
        assertFalse(vm.errorEnableBlocklist)
        assertFalse(vm.errorDeleteBlocklist)
        assertFalse(vm.blocklistDeletedSuccessfully)
        assertFalse(vm.errorRefreshBlocklist)
    }

    @Test
    fun `initialFetch calls fetchBlocklists and state becomes Success`() = runTest {
        val (sm, apiClient, blocklistsClient) = mockEnvironment()
        every { sm.apiClient } returns apiClient
        val response = TestFixtures.blocklistsListResponse(total = 10)
        coEvery { blocklistsClient.fetchBlocklists(any()) } returns TestFixtures.successResponse(response)

        val vm = BlocklistsListViewModel(sm)
        vm.initialFetch()
        advanceUntilIdle()

        val result = vm.state
        assertTrue(result is LoadingResult.Success)
        assertEquals(response, (result as LoadingResult.Success).value)
    }

    @Test
    fun `initialFetch when data already exists is no-op`() = runTest {
        val (sm, apiClient, blocklistsClient) = mockEnvironment()
        every { sm.apiClient } returns apiClient
        coEvery { blocklistsClient.fetchBlocklists(any()) } returns TestFixtures.successResponse(TestFixtures.blocklistsListResponse(total = 10))

        val vm = BlocklistsListViewModel(sm)
        vm.initialFetch()
        advanceUntilIdle()
        assertTrue(vm.state is LoadingResult.Success)

        vm.initialFetch()
        advanceUntilIdle()
        assertTrue(vm.state is LoadingResult.Success)
    }

    @Test
    fun `fetch failure sets Failure state`() = runTest {
        val (sm, apiClient, blocklistsClient) = mockEnvironment()
        every { sm.apiClient } returns apiClient
        coEvery { blocklistsClient.fetchBlocklists(any()) } throws HttpClientException.NetworkError(RuntimeException("network error"))

        val vm = BlocklistsListViewModel(sm)
        vm.initialFetch()
        advanceUntilIdle()

        val result = vm.state
        assertTrue(result is LoadingResult.Failure)
        assertNotNull((result as LoadingResult.Failure).throwable)
    }

    @Test
    fun `refresh toggles isRefreshing and calls fetchBlocklists`() = runTest {
        val (sm, apiClient, blocklistsClient) = mockEnvironment()
        every { sm.apiClient } returns apiClient
        coEvery { blocklistsClient.fetchBlocklists(any()) } returns TestFixtures.successResponse(TestFixtures.blocklistsListResponse(total = 10))

        val vm = BlocklistsListViewModel(sm)
        vm.refresh()
        advanceUntilIdle()

        assertFalse(vm.isRefreshing)
        assertTrue(vm.state is LoadingResult.Success)
        coVerify { blocklistsClient.fetchBlocklists(any()) }
    }

    @Test
    fun `fetchMore appends paginated data`() = runTest {
        val (sm, apiClient, blocklistsClient) = mockEnvironment()
        every { sm.apiClient } returns apiClient
        val page1 = TestFixtures.blocklistsListResponse(
            items = (1..Defaults.BLOCKLISTS_AMOUNT_BATCH).map { TestFixtures.blocklistsListResponseItem(id = it.toString()) },
            page = 1,
            total = 2500
        )
        coEvery { blocklistsClient.fetchBlocklists(any()) } returns TestFixtures.successResponse(page1)

        val vm = BlocklistsListViewModel(sm)
        vm.initialFetch()
        advanceUntilIdle()

        val page2Items = (1001..2000).map { TestFixtures.blocklistsListResponseItem(id = it.toString()) }
        val page2 = TestFixtures.blocklistsListResponse(items = page2Items, page = 2, total = 2500)
        coEvery { blocklistsClient.fetchBlocklists(any()) } returns TestFixtures.successResponse(page2)

        vm.fetchMore()
        advanceUntilIdle()

        val data = (vm.state as LoadingResult.Success).value
        assertEquals(Defaults.BLOCKLISTS_AMOUNT_BATCH + page2Items.size, data.items.size)
        coVerify(exactly = 2) { blocklistsClient.fetchBlocklists(any()) }
    }

    @Test
    fun `fetchMore is no-op when all data loaded`() = runTest {
        val (sm, apiClient, blocklistsClient) = mockEnvironment()
        every { sm.apiClient } returns apiClient
        val total = 10
        coEvery { blocklistsClient.fetchBlocklists(any()) } returns TestFixtures.successResponse(
            TestFixtures.blocklistsListResponse(total = total, page = 1)
        )

        val vm = BlocklistsListViewModel(sm)
        vm.initialFetch()
        advanceUntilIdle()

        vm.fetchMore()
        advanceUntilIdle()
    }

    @Test
    fun `enableDisableBlocklist success toggles processingModal and calls refreshInternal`() = runTest {
        val (sm, apiClient, blocklistsClient) = mockEnvironment()
        every { sm.apiClient } returns apiClient
        coEvery { blocklistsClient.toggleBlocklist(any(), any()) } returns TestFixtures.successResponse(EmptyResponse())
        coEvery { blocklistsClient.fetchBlocklists(any()) } returns TestFixtures.successResponse(TestFixtures.blocklistsListResponse(total = 10))

        val vm = BlocklistsListViewModel(sm)
        vm.enableDisableBlocklist("blocklist-1", true)
        advanceUntilIdle()

        assertFalse(vm.processingModal)
        assertFalse(vm.errorEnableBlocklist)
        coVerify { blocklistsClient.toggleBlocklist("blocklist-1", ToggleBlocklistRequest(enabled = true)) }
    }

    @Test
    fun `enableDisableBlocklist failure sets errorEnableBlocklist`() = runTest {
        val (sm, apiClient, blocklistsClient) = mockEnvironment()
        every { sm.apiClient } returns apiClient
        coEvery { blocklistsClient.toggleBlocklist(any(), any()) } throws HttpClientException.NetworkError(RuntimeException("error"))

        val vm = BlocklistsListViewModel(sm)
        vm.enableDisableBlocklist("blocklist-1", true)
        advanceUntilIdle()

        assertTrue(vm.errorEnableBlocklist)
        assertFalse(vm.processingModal)
    }

    @Test
    fun `enableDisableBlocklist disable failure sets errorDisableBlocklist`() = runTest {
        val (sm, apiClient, blocklistsClient) = mockEnvironment()
        every { sm.apiClient } returns apiClient
        coEvery { blocklistsClient.toggleBlocklist(any(), any()) } throws HttpClientException.NetworkError(RuntimeException("error"))

        val vm = BlocklistsListViewModel(sm)
        vm.enableDisableBlocklist("blocklist-1", false)
        advanceUntilIdle()

        assertTrue(vm.errorDisableBlocklist)
        assertFalse(vm.processingModal)
    }

    @Test
    fun `deleteBlocklist success sets blocklistDeletedSuccessfully`() = runTest {
        val (sm, apiClient, blocklistsClient) = mockEnvironment()
        every { sm.apiClient } returns apiClient
        coEvery { blocklistsClient.deleteBlocklist(any()) } returns TestFixtures.successResponse(EmptyResponse())
        coEvery { blocklistsClient.fetchBlocklists(any()) } returns TestFixtures.successResponse(TestFixtures.blocklistsListResponse(total = 10))

        val vm = BlocklistsListViewModel(sm)
        vm.deleteBlocklist("blocklist-1")
        advanceUntilIdle()

        assertFalse(vm.processingModal)
        assertTrue(vm.blocklistDeletedSuccessfully)
        coVerify { blocklistsClient.deleteBlocklist("blocklist-1") }
    }

    @Test
    fun `deleteBlocklist failure sets errorDeleteBlocklist`() = runTest {
        val (sm, apiClient, blocklistsClient) = mockEnvironment()
        every { sm.apiClient } returns apiClient
        coEvery { blocklistsClient.deleteBlocklist(any()) } throws HttpClientException.NetworkError(RuntimeException("error"))

        val vm = BlocklistsListViewModel(sm)
        vm.deleteBlocklist("blocklist-1")
        advanceUntilIdle()

        assertTrue(vm.errorDeleteBlocklist)
        assertFalse(vm.processingModal)
    }

    @Test
    fun `refreshBlocklists with id calls refreshBlocklist`() = runTest {
        val (sm, apiClient, blocklistsClient) = mockEnvironment()
        every { sm.apiClient } returns apiClient
        coEvery { blocklistsClient.refreshBlocklist(any()) } returns TestFixtures.successResponse(RefreshBlocklistsResponse(message = "ok"))

        val vm = BlocklistsListViewModel(sm)
        vm.refreshBlocklists("blocklist-1")
        advanceUntilIdle()

        assertFalse(vm.processingModal)
        assertFalse(vm.errorRefreshBlocklist)
        coVerify { blocklistsClient.refreshBlocklist("blocklist-1") }
        coVerify(exactly = 0) { blocklistsClient.refreshAllBlocklists() }
    }

    @Test
    fun `refreshBlocklists without id calls refreshAllBlocklists`() = runTest {
        val (sm, apiClient, blocklistsClient) = mockEnvironment()
        every { sm.apiClient } returns apiClient
        coEvery { blocklistsClient.refreshAllBlocklists() } returns TestFixtures.successResponse(RefreshBlocklistsResponse(message = "ok"))

        val vm = BlocklistsListViewModel(sm)
        vm.refreshBlocklists()
        advanceUntilIdle()

        assertFalse(vm.processingModal)
        assertFalse(vm.errorRefreshBlocklist)
        coVerify { blocklistsClient.refreshAllBlocklists() }
        coVerify(exactly = 0) { blocklistsClient.refreshBlocklist(any()) }
    }

    @Test
    fun `refreshBlocklists failure sets errorRefreshBlocklist`() = runTest {
        val (sm, apiClient, blocklistsClient) = mockEnvironment()
        every { sm.apiClient } returns apiClient
        coEvery { blocklistsClient.refreshBlocklist(any()) } throws HttpClientException.NetworkError(RuntimeException("error"))

        val vm = BlocklistsListViewModel(sm)
        vm.refreshBlocklists("blocklist-1")
        advanceUntilIdle()

        assertTrue(vm.errorRefreshBlocklist)
        assertFalse(vm.processingModal)
    }

    @Test
    fun `reset restores all state`() = runTest {
        val (sm, apiClient, blocklistsClient) = mockEnvironment()
        every { sm.apiClient } returns apiClient
        coEvery { blocklistsClient.fetchBlocklists(any()) } returns TestFixtures.successResponse(TestFixtures.blocklistsListResponse(total = 10))
        coEvery { blocklistsClient.toggleBlocklist(any(), any()) } throws HttpClientException.NetworkError(RuntimeException("error"))

        val vm = BlocklistsListViewModel(sm)
        vm.initialFetch()
        advanceUntilIdle()

        vm.enableDisableBlocklist("1", true)
        advanceUntilIdle()
        assertTrue(vm.errorEnableBlocklist)

        vm.selectListName("test-list")
        vm.reset()

        assertEquals(LoadingResult.Loading, vm.state)
        assertNull(vm.selectedListName)
        assertFalse(vm.isRefreshing)
        assertFalse(vm.isLoadingMore)
        assertFalse(vm.processingModal)
        assertFalse(vm.errorDisableBlocklist)
        assertFalse(vm.errorEnableBlocklist)
        assertFalse(vm.errorDeleteBlocklist)
        assertFalse(vm.blocklistDeletedSuccessfully)
        assertFalse(vm.errorRefreshBlocklist)
    }

    @Test
    fun `selectListName sets selectedListName`() {
        val (sm) = mockEnvironment()
        val vm = BlocklistsListViewModel(sm)
        vm.selectListName("test")
        assertEquals("test", vm.selectedListName)
        vm.selectListName(null)
        assertNull(vm.selectedListName)
    }

    @Test
    fun `clearErrorDisableBlocklist resets flag`() {
        val (sm) = mockEnvironment()
        val vm = BlocklistsListViewModel(sm)
        vm.clearErrorDisableBlocklist()
        assertFalse(vm.errorDisableBlocklist)
    }

    @Test
    fun `clearErrorEnableBlocklist resets flag`() {
        val (sm) = mockEnvironment()
        val vm = BlocklistsListViewModel(sm)
        vm.clearErrorEnableBlocklist()
        assertFalse(vm.errorEnableBlocklist)
    }

    @Test
    fun `clearErrorDeleteBlocklist resets flag`() {
        val (sm) = mockEnvironment()
        val vm = BlocklistsListViewModel(sm)
        vm.clearErrorDeleteBlocklist()
        assertFalse(vm.errorDeleteBlocklist)
    }

    @Test
    fun `clearBlocklistDeletedSuccessfully resets flag`() {
        val (sm) = mockEnvironment()
        val vm = BlocklistsListViewModel(sm)
        vm.clearBlocklistDeletedSuccessfully()
        assertFalse(vm.blocklistDeletedSuccessfully)
    }

    @Test
    fun `clearErrorRefreshBlocklist resets flag`() {
        val (sm) = mockEnvironment()
        val vm = BlocklistsListViewModel(sm)
        vm.clearErrorRefreshBlocklist()
        assertFalse(vm.errorRefreshBlocklist)
    }
}
