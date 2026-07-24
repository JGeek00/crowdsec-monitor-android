package com.jgeek00.crowdsecmonitor.viewmodel

import com.jgeek00.crowdsecmonitor.MainDispatcherRule
import com.jgeek00.crowdsecmonitor.data.api.CrowdSecApiClient
import com.jgeek00.crowdsecmonitor.data.models.ApiStatusResponse
import com.jgeek00.crowdsecmonitor.data.models.LoadingResult
import com.jgeek00.crowdsecmonitor.repository.ServiceStatusRepository
import com.jgeek00.crowdsecmonitor.session.SessionManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ServiceStatusViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val sessionManager = mockk<SessionManager>(relaxed = true)
    private val repository = mockk<ServiceStatusRepository>(relaxed = true)
    private val apiClient = mockk<CrowdSecApiClient>(relaxed = true)
    private val statusFlow = MutableStateFlow<LoadingResult<ApiStatusResponse>>(LoadingResult.Loading)
    private lateinit var vm: ServiceStatusViewModel

    @Before
    fun setUp() {
        every { sessionManager.apiClient } returns null
        every { repository.status } returns statusFlow
        vm = ServiceStatusViewModel(sessionManager, repository)
    }

    @Test
    fun `viewModel can be created`() {
        assertNotNull(vm)
    }

    @Test
    fun `status exposes repository status`() {
        assertEquals(LoadingResult.Loading, vm.status.value)
    }

    @Test
    fun `status reflects Success from repository`() {
        val response = mockk<ApiStatusResponse>(relaxed = true)
        statusFlow.value = LoadingResult.Success(response)

        assertTrue(vm.status.value is LoadingResult.Success)
        assertEquals(response, (vm.status.value as LoadingResult.Success).value)
    }

    @Test
    fun `status reflects Failure from repository`() {
        statusFlow.value = LoadingResult.Failure(Exception("test error"))

        assertTrue(vm.status.value is LoadingResult.Failure)
    }

    @Test
    fun `closeWebSocket calls repository stop with current apiClient`() {
        every { sessionManager.apiClient } returns apiClient

        vm.closeWebSocket()

        verify { repository.stop(apiClient) }
    }

    @Test
    fun `closeWebSocket passes null when apiClient is null`() {
        vm.closeWebSocket()

        verify { repository.stop(null) }
    }

    @Test
    fun `openWebSocket calls repository openWebSocketManual`() {
        every { sessionManager.apiClient } returns apiClient

        vm.openWebSocket()

        verify { repository.openWebSocketManual(apiClient) }
    }

    @Test
    fun `openWebSocket does nothing when apiClient is null`() {
        vm.openWebSocket()

        verify(exactly = 0) { repository.openWebSocketManual(any()) }
    }
}
