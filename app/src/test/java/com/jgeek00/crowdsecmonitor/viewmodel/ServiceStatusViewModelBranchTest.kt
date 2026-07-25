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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ServiceStatusViewModelBranchTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val sessionManager = mockk<SessionManager>(relaxed = true)
    private val repository = mockk<ServiceStatusRepository>(relaxed = true)
    private val apiClient = mockk<CrowdSecApiClient>(relaxed = true)
    private val statusFlow = MutableStateFlow<LoadingResult<ApiStatusResponse>>(LoadingResult.Loading)

    @Before
    fun setUp() {
        every { repository.status } returns statusFlow
    }

    @Test
    fun `init with non-null apiClient calls repository start`() = runTest {
        every { sessionManager.apiClient } returns apiClient

        ServiceStatusViewModel(sessionManager, repository)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        verify { repository.start(apiClient) }
    }

    @Test
    fun `init with null apiClient calls repository stop`() = runTest {
        every { sessionManager.apiClient } returns null

        ServiceStatusViewModel(sessionManager, repository)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        verify { repository.stop(null) }
    }

    @Test
    fun `status returns repository status flow`() {
        every { sessionManager.apiClient } returns apiClient
        statusFlow.value = LoadingResult.Success(mockk(relaxed = true))

        val vm = ServiceStatusViewModel(sessionManager, repository)

        assertTrue(vm.status.value is LoadingResult.Success)
    }

    @Test
    fun `closeWebSocket stops repository`() {
        every { sessionManager.apiClient } returns apiClient
        val vm = ServiceStatusViewModel(sessionManager, repository)

        vm.closeWebSocket()

        verify { repository.stop(apiClient) }
    }
}
