package com.jgeek00.crowdsecmonitor.viewmodel

import android.net.InetAddresses
import com.jgeek00.crowdsecmonitor.MainDispatcherRule
import com.jgeek00.crowdsecmonitor.constants.Enums
import com.jgeek00.crowdsecmonitor.data.api.AllowlistsApiClient
import com.jgeek00.crowdsecmonitor.data.api.BlocklistsApiClient
import com.jgeek00.crowdsecmonitor.data.api.CrowdSecApiClient
import com.jgeek00.crowdsecmonitor.data.models.AllowlistsCheckIPsRequest
import com.jgeek00.crowdsecmonitor.data.models.AllowlistsCheckIPsResponse
import com.jgeek00.crowdsecmonitor.data.models.AllowlistsCheckIPsResponseResult
import com.jgeek00.crowdsecmonitor.data.models.BlocklistsCheckIPsRequest
import com.jgeek00.crowdsecmonitor.data.models.BlocklistsCheckIPsResponse
import com.jgeek00.crowdsecmonitor.data.models.BlocklistsCheckIPsResponseResult
import com.jgeek00.crowdsecmonitor.data.models.HttpResponse
import com.jgeek00.crowdsecmonitor.session.SessionManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class IPsCheckerViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val sessionManager = mockk<SessionManager>(relaxed = true)
    private val apiClient = mockk<CrowdSecApiClient>(relaxed = true)
    private val blocklistsClient = mockk<BlocklistsApiClient>(relaxed = true)
    private val allowlistsClient = mockk<AllowlistsApiClient>(relaxed = true)
    private lateinit var vm: IPsCheckerViewModel

    @Before
    fun setUp() {
        mockkStatic(InetAddresses::class)
        every { InetAddresses.isNumericAddress(any()) } returns true
        every { sessionManager.apiClient } returns null
        every { apiClient.blocklists } returns blocklistsClient
        every { apiClient.allowlists } returns allowlistsClient
        vm = IPsCheckerViewModel(sessionManager)
    }

    @Test
    fun `initial state is empty`() {
        assertTrue(vm.ipsToCheck.isEmpty())
        assertFalse(vm.blocklistsLoading)
        assertFalse(vm.blocklistsError)
    }

    @Test
    fun `addEntry adds an IPField`() {
        vm.addEntry()
        assertEquals(1, vm.ipsToCheck.size)
        assertEquals("", vm.ipsToCheck[0].value)
        assertFalse(vm.ipsToCheck[0].invalid)
    }

    @Test
    fun `addEntry adds multiple entries`() {
        vm.addEntry()
        vm.addEntry()
        assertEquals(2, vm.ipsToCheck.size)
    }

    @Test
    fun `removeEntry removes at index`() {
        vm.addEntry()
        vm.addEntry()
        vm.removeEntry(0)
        assertEquals(1, vm.ipsToCheck.size)
    }

    @Test
    fun `updateEntry updates value`() {
        vm.addEntry()
        vm.updateEntry(0, "1.2.3.4")
        assertEquals("1.2.3.4", vm.ipsToCheck[0].value)
    }

    @Test
    fun `updateEntry marks invalid when isNumericAddress returns false`() {
        every { InetAddresses.isNumericAddress(any()) } returns false
        vm.addEntry()
        vm.updateEntry(0, "not-an-ip")
        assertTrue(vm.ipsToCheck[0].invalid)
    }

    @Test
    fun `reset clears all state`() {
        vm.addEntry()
        vm.addEntry()
        vm.selectedListType = Enums.ListType.ALLOWLIST
        vm.reset()
        assertTrue(vm.ipsToCheck.isEmpty())
        assertFalse(vm.blocklistsLoading)
        assertFalse(vm.blocklistsError)
        assertFalse(vm.allowlistsLoading)
        assertFalse(vm.allowlistsError)
    }

    @Test
    fun `checkIps does nothing when apiClient is null`() = runTest {
        vm.addEntry()
        vm.updateEntry(0, "1.2.3.4")
        vm.checkIps()
        advanceUntilIdle()
        assertFalse(vm.blocklistsLoading)
    }

    @Test
    fun `checkIps blocklists success`() = runTest {
        every { sessionManager.apiClient } returns apiClient
        vm.addEntry()
        vm.updateEntry(0, "1.2.3.4")
        val response = BlocklistsCheckIPsResponse(
            results = listOf(BlocklistsCheckIPsResponseResult(ip = "1.2.3.4", blocklists = listOf("1")))
        )
        coEvery { blocklistsClient.checkIps(any<BlocklistsCheckIPsRequest>()) } returns HttpResponse(successful = true, statusCode = 200, body = response)

        vm.checkIps()
        advanceUntilIdle()

        assertFalse(vm.blocklistsLoading)
        assertFalse(vm.blocklistsError)
        assertEquals(response, vm.blocklistsResult)
        coVerify { blocklistsClient.checkIps(BlocklistsCheckIPsRequest(ips = listOf("1.2.3.4"))) }
    }

    @Test
    fun `checkIps blocklists failure`() = runTest {
        every { sessionManager.apiClient } returns apiClient
        vm.addEntry()
        vm.updateEntry(0, "1.2.3.4")
        coEvery { blocklistsClient.checkIps(any<BlocklistsCheckIPsRequest>()) } throws Exception("network error")

        vm.checkIps()
        advanceUntilIdle()

        assertFalse(vm.blocklistsLoading)
        assertTrue(vm.blocklistsError)
    }

    @Test
    fun `checkIps allowlists success`() = runTest {
        every { sessionManager.apiClient } returns apiClient
        vm.addEntry()
        vm.updateEntry(0, "1.2.3.4")
        vm.selectedListType = Enums.ListType.ALLOWLIST
        val response = AllowlistsCheckIPsResponse(
            results = listOf(AllowlistsCheckIPsResponseResult(ip = "1.2.3.4", allowlist = "test"))
        )
        coEvery { allowlistsClient.checkIps(any<AllowlistsCheckIPsRequest>()) } returns HttpResponse(successful = true, statusCode = 200, body = response)

        vm.checkIps()
        advanceUntilIdle()

        assertFalse(vm.allowlistsLoading)
        assertFalse(vm.allowlistsError)
        assertEquals(response, vm.allowlistsResult)
        coVerify { allowlistsClient.checkIps(AllowlistsCheckIPsRequest(ips = listOf("1.2.3.4"))) }
    }

    @Test
    fun `checkIps allowlists failure`() = runTest {
        every { sessionManager.apiClient } returns apiClient
        vm.addEntry()
        vm.updateEntry(0, "1.2.3.4")
        vm.selectedListType = Enums.ListType.ALLOWLIST
        coEvery { allowlistsClient.checkIps(any<AllowlistsCheckIPsRequest>()) } throws Exception("network error")

        vm.checkIps()
        advanceUntilIdle()

        assertFalse(vm.allowlistsLoading)
        assertTrue(vm.allowlistsError)
    }
}
