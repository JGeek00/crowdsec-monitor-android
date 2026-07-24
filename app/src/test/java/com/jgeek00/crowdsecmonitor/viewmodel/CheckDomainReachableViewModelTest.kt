package com.jgeek00.crowdsecmonitor.viewmodel

import com.jgeek00.crowdsecmonitor.MainDispatcherRule
import com.jgeek00.crowdsecmonitor.data.api.BlocklistsApiClient
import com.jgeek00.crowdsecmonitor.data.api.CrowdSecApiClient
import com.jgeek00.crowdsecmonitor.data.models.BlocklistsCheckDomainResponse
import com.jgeek00.crowdsecmonitor.data.models.HttpClientException
import com.jgeek00.crowdsecmonitor.fixtures.TestFixtures
import com.jgeek00.crowdsecmonitor.session.SessionManager
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class CheckDomainReachableViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val sessionManager = mockk<SessionManager>(relaxed = true)
    private val apiClient = mockk<CrowdSecApiClient>(relaxed = true)
    private val blocklistsClient = mockk<BlocklistsApiClient>(relaxed = true)
    private lateinit var vm: CheckDomainReachableViewModel

    @Before
    fun setUp() {
        every { sessionManager.apiClient } returns apiClient
        every { apiClient.blocklists } returns blocklistsClient
        vm = CheckDomainReachableViewModel(sessionManager)
    }

    @Test
    fun `checkDomain invalid domain sets invalidDomainAlert`() {
        vm.domain = "not a valid domain!!!"

        vm.checkDomain()

        assertTrue(vm.invalidDomainAlert)
        assertNull(vm.data)
        assertFalse(vm.error)
        assertFalse(vm.domainNotResolvable)
    }

    @Test
    fun `checkDomain valid domain calls API and sets data`() = runTest {
        val response = mockk<BlocklistsCheckDomainResponse>(relaxed = true)
        coEvery { blocklistsClient.checkDomain(any()) } returns TestFixtures.successResponse(response)

        vm.domain = "example.com"
        vm.checkDomain()
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        assertEquals(response, vm.data)
        assertFalse(vm.error)
        assertFalse(vm.domainNotResolvable)
        assertFalse(vm.invalidDomainAlert)
    }

    @Test
    fun `checkDomain 422 error sets domainNotResolvable`() = runTest {
        coEvery { blocklistsClient.checkDomain(any()) } throws HttpClientException.HttpError(statusCode = 422)

        vm.domain = "example.com"
        vm.checkDomain()
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        assertTrue(vm.domainNotResolvable)
        assertNull(vm.data)
        assertFalse(vm.error)
    }

    @Test
    fun `checkDomain 422 error with message sets domainNotResolvable`() = runTest {
        coEvery { blocklistsClient.checkDomain(any()) } throws HttpClientException.HttpErrorWithMessage(statusCode = 422, message = "Domain not resolvable")

        vm.domain = "example.com"
        vm.checkDomain()
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        assertTrue(vm.domainNotResolvable)
        assertNull(vm.data)
        assertFalse(vm.error)
    }

    @Test
    fun `checkDomain other HttpError sets error`() = runTest {
        coEvery { blocklistsClient.checkDomain(any()) } throws HttpClientException.HttpError(statusCode = 500)

        vm.domain = "example.com"
        vm.checkDomain()
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        assertTrue(vm.error)
        assertNull(vm.data)
        assertFalse(vm.domainNotResolvable)
    }

    @Test
    fun `checkDomain other HttpErrorWithMessage sets error`() = runTest {
        coEvery { blocklistsClient.checkDomain(any()) } throws HttpClientException.HttpErrorWithMessage(statusCode = 500, message = "Server error")

        vm.domain = "example.com"
        vm.checkDomain()
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        assertTrue(vm.error)
        assertNull(vm.data)
        assertFalse(vm.domainNotResolvable)
    }

    @Test
    fun `checkDomain generic exception sets error`() = runTest {
        coEvery { blocklistsClient.checkDomain(any()) } throws Exception("network error")

        vm.domain = "example.com"
        vm.checkDomain()
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        assertTrue(vm.error)
        assertNull(vm.data)
        assertFalse(vm.domainNotResolvable)
    }

    @Test
    fun `reset clears all state`() {
        vm.domain = "example.com"
        vm.invalidDomainAlert = true

        vm.reset()

        assertEquals("", vm.domain)
        assertFalse(vm.invalidDomainAlert)
        assertNull(vm.data)
        assertFalse(vm.error)
        assertFalse(vm.domainNotResolvable)
    }

    @Test
    fun `resetAfterClose calls reset after delay`() = runTest {
        vm.domain = "example.com"
        vm.invalidDomainAlert = true

        vm.resetAfterClose(delayMs = 0)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        assertEquals("", vm.domain)
        assertFalse(vm.invalidDomainAlert)
    }
}
