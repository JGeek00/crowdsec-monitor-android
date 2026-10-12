package com.jgeek00.crowdsecmonitor.viewmodel

import com.jgeek00.crowdsecmonitor.MainDispatcherRule
import com.jgeek00.crowdsecmonitor.data.api.CrowdSecApiClient
import com.jgeek00.crowdsecmonitor.data.api.NotificationChannelsApiClient
import com.jgeek00.crowdsecmonitor.data.models.DeleteNotificationChannelResponse
import com.jgeek00.crowdsecmonitor.data.models.HttpClientException
import com.jgeek00.crowdsecmonitor.data.models.NotificationChannelsListResponse
import com.jgeek00.crowdsecmonitor.data.models.LoadingResult
import com.jgeek00.crowdsecmonitor.data.models.NotificationChannelType
import com.jgeek00.crowdsecmonitor.data.models.ProvidersListResponse
import com.jgeek00.crowdsecmonitor.data.models.UserNotificationChannel
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

class NotificationChannelsListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val sessionManager = mockk<SessionManager>(relaxed = true)
    private val apiClient = mockk<CrowdSecApiClient>(relaxed = true)
    private val channelsClient = mockk<NotificationChannelsApiClient>(relaxed = true)

    @Before
    fun setUp() {
        every { sessionManager.apiClient } returns null
        every { apiClient.notificationChannels } returns channelsClient
    }

    private fun channel(id: Int) = UserNotificationChannel(
        id = id,
        name = "channel $id",
        type = NotificationChannelType.NTFY,
        supportsAlertInfo = false,
        config = emptyMap()
    )

    private fun stubChannels(vararg items: UserNotificationChannel) {
        coEvery { channelsClient.fetchChannels() } returns
            TestFixtures.successResponse(NotificationChannelsListResponse(data = listOf(*items)))
    }

    @Test
    fun `initial state is Loading`() {
        every { sessionManager.apiClient } returns apiClient
        val vm = NotificationChannelsListViewModel(sessionManager)
        assertEquals(LoadingResult.Loading, vm.state)
        assertFalse(vm.errorDelete)
        assertFalse(vm.channelInUse)
        assertFalse(vm.didDelete)
    }

    @Test
    fun `initialFetch sets state to Success and loads providers`() = runTest {
        val c1 = channel(1)
        val c2 = channel(2)
        stubChannels(c1, c2)
        val providers = listOf(
            com.jgeek00.crowdsecmonitor.data.models.NotificationProvider(
                type = "ntfy",
                descriptionKey = null,
                icon = "ntfy",
                labelKey = "provider.ntfy.label",
                supportsTest = true,
                supportsAlertInfo = true,
                fields = emptyList()
            )
        )
        coEvery { channelsClient.fetchProviders() } returns
            TestFixtures.successResponse(ProvidersListResponse(version = 2, providers = providers))

        every { sessionManager.apiClient } returns apiClient
        val vm = NotificationChannelsListViewModel(sessionManager)
        vm.initialFetch()
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        val result = vm.state
        assertTrue(result is LoadingResult.Success)
        assertEquals(listOf(c1, c2), (result as LoadingResult.Success).value)
        assertEquals(providers, vm.providers)
    }

    @Test
    fun `initialFetch sets state to Failure on exception`() = runTest {
        coEvery { channelsClient.fetchChannels() } throws Exception("test error")

        every { sessionManager.apiClient } returns apiClient
        val vm = NotificationChannelsListViewModel(sessionManager)
        vm.initialFetch()
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        assertTrue(vm.state is LoadingResult.Failure)
    }

    @Test
    fun `providers failure is non-fatal and list still shows`() = runTest {
        stubChannels(channel(1))
        coEvery { channelsClient.fetchProviders() } throws Exception("providers error")

        every { sessionManager.apiClient } returns apiClient
        val vm = NotificationChannelsListViewModel(sessionManager)
        vm.initialFetch()
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        val result = vm.state
        assertTrue(result is LoadingResult.Success)
        assertEquals(1, (result as LoadingResult.Success).value.size)
        assertTrue(vm.providers.isEmpty())
    }

    @Test
    fun `initialFetch is no-op when data already loaded`() = runTest {
        stubChannels(channel(1))

        every { sessionManager.apiClient } returns apiClient
        val vm = NotificationChannelsListViewModel(sessionManager)
        vm.initialFetch()
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        vm.initialFetch()
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { channelsClient.fetchChannels() }
    }

    @Test
    fun `delete removes channel locally and sets didDelete`() = runTest {
        val c1 = channel(1)
        val c2 = channel(2)
        stubChannels(c1, c2)
        coEvery { channelsClient.deleteChannel(1) } returns
            TestFixtures.successResponse(DeleteNotificationChannelResponse(message = "deleted"))

        every { sessionManager.apiClient } returns apiClient
        val vm = NotificationChannelsListViewModel(sessionManager)
        vm.initialFetch()
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        vm.delete(1)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        val result = vm.state as LoadingResult.Success
        assertEquals(listOf(c2), result.value)
        assertTrue(vm.didDelete)
        assertFalse(vm.errorDelete)
        assertFalse(vm.channelInUse)
    }

    @Test
    fun `delete with 409 sets channelInUse and keeps state`() = runTest {
        stubChannels(channel(1))
        coEvery { channelsClient.deleteChannel(1) } throws HttpClientException.HttpError(409)

        every { sessionManager.apiClient } returns apiClient
        val vm = NotificationChannelsListViewModel(sessionManager)
        vm.initialFetch()
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        vm.delete(1)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        assertTrue(vm.channelInUse)
        assertFalse(vm.errorDelete)
        assertFalse(vm.didDelete)
        val result = vm.state as LoadingResult.Success
        assertEquals(1, result.value.size)
    }

    @Test
    fun `delete with 409 message variant sets channelInUse`() = runTest {
        stubChannels(channel(1))
        coEvery {
            channelsClient.deleteChannel(1)
        } throws HttpClientException.HttpErrorWithMessage(409, "channel in use")

        every { sessionManager.apiClient } returns apiClient
        val vm = NotificationChannelsListViewModel(sessionManager)
        vm.initialFetch()
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        vm.delete(1)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        assertTrue(vm.channelInUse)
        assertFalse(vm.errorDelete)
    }

    @Test
    fun `delete with other error sets errorDelete`() = runTest {
        stubChannels(channel(1))
        coEvery { channelsClient.deleteChannel(1) } throws HttpClientException.HttpError(500)

        every { sessionManager.apiClient } returns apiClient
        val vm = NotificationChannelsListViewModel(sessionManager)
        vm.initialFetch()
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        vm.delete(1)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        assertTrue(vm.errorDelete)
        assertFalse(vm.channelInUse)
        assertFalse(vm.didDelete)
    }

    @Test
    fun `reset clears state, providers and action flags`() = runTest {
        stubChannels(channel(1))

        every { sessionManager.apiClient } returns apiClient
        val vm = NotificationChannelsListViewModel(sessionManager)
        vm.initialFetch()
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        vm.reset()
        assertEquals(LoadingResult.Loading, vm.state)
        assertTrue(vm.providers.isEmpty())
        assertFalse(vm.errorDelete)
        assertFalse(vm.channelInUse)
        assertFalse(vm.didDelete)
    }
}
