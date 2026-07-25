package com.jgeek00.crowdsecmonitor.viewmodel

import com.jgeek00.crowdsecmonitor.MainDispatcherRule
import com.jgeek00.crowdsecmonitor.data.api.BlocklistsApiClient
import com.jgeek00.crowdsecmonitor.data.api.CrowdSecApiClient
import com.jgeek00.crowdsecmonitor.data.models.AddBlocklistRequest
import com.jgeek00.crowdsecmonitor.data.models.EmptyResponse
import com.jgeek00.crowdsecmonitor.data.models.HttpResponse
import com.jgeek00.crowdsecmonitor.session.SessionManager
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AddBlocklistFormViewModelBranchTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val sessionManager = mockk<SessionManager>(relaxed = true)
    private val apiClient = mockk<CrowdSecApiClient>(relaxed = true)
    private val blocklistsClient = mockk<BlocklistsApiClient>(relaxed = true)
    private lateinit var vm: AddBlocklistFormViewModel

    @Before
    fun setUp() {
        every { sessionManager.apiClient } returns apiClient
        every { apiClient.blocklists } returns blocklistsClient
        coEvery { blocklistsClient.addBlocklist(any()) } returns HttpResponse(successful = true, statusCode = 200, body = EmptyResponse())
        vm = AddBlocklistFormViewModel(sessionManager)
    }

    @Test
    fun `initial state`() {
        assertEquals("", vm.name)
        assertEquals("", vm.url)
        assertFalse(vm.requiredFieldsError)
        assertFalse(vm.invalidUrlError)
        assertFalse(vm.savingError)
    }

    @Test
    fun `save sets requiredFieldsError when name is blank`() {
        vm.name = ""
        vm.url = "https://example.com"
        vm.save {}
        assertTrue(vm.requiredFieldsError)
    }

    @Test
    fun `save sets requiredFieldsError when url is blank`() {
        vm.name = "test"
        vm.url = ""
        vm.save {}
        assertTrue(vm.requiredFieldsError)
    }

    @Test
    fun `save sets requiredFieldsError when both are blank`() {
        vm.save {}
        assertTrue(vm.requiredFieldsError)
    }

    @Test
    fun `save sets invalidUrlError for invalid URL`() {
        vm.name = "test"
        vm.url = "not-a-url"
        vm.save {}
        assertTrue(vm.invalidUrlError)
    }

    @Test
    fun `save is no-op when apiClient is null`() {
        every { sessionManager.apiClient } returns null
        vm.name = "test"
        vm.url = "https://example.com"
        val onSuccess = mockk<() -> Unit>(relaxed = true)
        vm.save(onSuccess)
        verify(exactly = 0) { onSuccess.invoke() }
    }

    @Test
    fun `save calls addBlocklist on success`() = runTest {
        vm.name = "test"
        vm.url = "https://example.com"
        var called = false
        vm.save { called = true }
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        assertTrue(called)
    }

    @Test
    fun `save sets savingError on exception`() = runTest {
        coEvery { blocklistsClient.addBlocklist(any()) } throws Exception("Network error")
        vm.name = "test"
        vm.url = "https://example.com"
        vm.save {}
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        assertTrue(vm.savingError)
    }

    @Test
    fun `save trims name and url`() = runTest {
        vm.name = "  test  "
        vm.url = "  https://example.com  "
        var captured: AddBlocklistRequest? = null
        coEvery { blocklistsClient.addBlocklist(any()) } answers {
            captured = firstArg()
            HttpResponse(successful = true, statusCode = 200, body = EmptyResponse())
        }
        vm.save {}
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        assertEquals("test", captured?.name)
        assertEquals("https://example.com", captured?.url)
    }

    @Test
    fun `reset clears all state`() {
        vm.name = "test"
        vm.url = "https://example.com"
        vm.requiredFieldsError = true
        vm.invalidUrlError = true
        vm.savingError = true
        vm.reset()
        assertEquals("", vm.name)
        assertEquals("", vm.url)
        assertFalse(vm.requiredFieldsError)
        assertFalse(vm.invalidUrlError)
        assertFalse(vm.savingError)
    }
}
