package com.jgeek00.crowdsecmonitor.viewmodel

import com.jgeek00.crowdsecmonitor.MainDispatcherRule
import com.jgeek00.crowdsecmonitor.data.api.CrowdSecApiClient
import com.jgeek00.crowdsecmonitor.data.api.NotificationsApiClient
import com.jgeek00.crowdsecmonitor.data.models.LoadingResult
import com.jgeek00.crowdsecmonitor.data.models.NotificationConditionNode
import com.jgeek00.crowdsecmonitor.data.models.NotificationConditionValue
import com.jgeek00.crowdsecmonitor.data.models.NotificationDetailResponse
import com.jgeek00.crowdsecmonitor.data.models.NotificationsListResponse
import com.jgeek00.crowdsecmonitor.data.models.UserNotification
import com.jgeek00.crowdsecmonitor.fixtures.TestFixtures
import com.jgeek00.crowdsecmonitor.session.SessionManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class NotificationsListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val sessionManager = mockk<SessionManager>(relaxed = true)
    private val apiClient = mockk<CrowdSecApiClient>(relaxed = true)
    private val notificationsClient = mockk<NotificationsApiClient>(relaxed = true)

    @Before
    fun setUp() {
        every { sessionManager.apiClient } returns null
        every { apiClient.notifications } returns notificationsClient
    }

    private fun notification(id: Int, enabled: Boolean = true) = UserNotification(
        id = id,
        name = "notification $id",
        description = null,
        enabled = enabled,
        condition = NotificationConditionNode.Leaf(
            field = "scenario",
            operator = "equals",
            value = NotificationConditionValue.Single("crowdsec/ssh-bf")
        ),
        threshold = null,
        message = "Alert",
        includeAlertInfo = false,
        channelIds = listOf(1)
    )

    private fun stubFetch(vararg items: UserNotification) {
        coEvery { notificationsClient.fetchNotifications() } returns
            TestFixtures.successResponse(NotificationsListResponse(data = listOf(*items)))
    }

    @Test
    fun `initial state is Loading`() {
        every { sessionManager.apiClient } returns apiClient
        val vm = NotificationsListViewModel(sessionManager)
        assertEquals(LoadingResult.Loading, vm.state)
        assertFalse(vm.errorToggle)
        assertFalse(vm.errorDelete)
        assertFalse(vm.didDelete)
    }

    @Test
    fun `initialFetch sets state to Success with list data`() = runTest {
        val n1 = notification(1)
        val n2 = notification(2)
        stubFetch(n1, n2)

        every { sessionManager.apiClient } returns apiClient
        val vm = NotificationsListViewModel(sessionManager)
        vm.initialFetch()
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        val result = vm.state
        assertTrue(result is LoadingResult.Success)
        assertEquals(listOf(n1, n2), (result as LoadingResult.Success).value)
    }

    @Test
    fun `initialFetch sets state to Failure on exception`() = runTest {
        coEvery { notificationsClient.fetchNotifications() } throws Exception("test error")

        every { sessionManager.apiClient } returns apiClient
        val vm = NotificationsListViewModel(sessionManager)
        vm.initialFetch()
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        assertTrue(vm.state is LoadingResult.Failure)
    }

    @Test
    fun `initialFetch is no-op when data already loaded`() = runTest {
        stubFetch(notification(1))

        every { sessionManager.apiClient } returns apiClient
        val vm = NotificationsListViewModel(sessionManager)
        vm.initialFetch()
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        vm.initialFetch()
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { notificationsClient.fetchNotifications() }
    }

    @Test
    fun `refresh fetches data again`() = runTest {
        stubFetch(notification(1))

        every { sessionManager.apiClient } returns apiClient
        val vm = NotificationsListViewModel(sessionManager)
        vm.refresh()
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        assertTrue(vm.state is LoadingResult.Success)
    }

    @Test
    fun `refresh sets state to Failure on exception`() = runTest {
        coEvery { notificationsClient.fetchNotifications() } throws Exception("test error")

        every { sessionManager.apiClient } returns apiClient
        val vm = NotificationsListViewModel(sessionManager)
        vm.refresh()
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        assertTrue(vm.state is LoadingResult.Failure)
    }

    @Test
    fun `toggle replaces item in place with response body`() = runTest {
        val original = notification(1, enabled = true)
        val other = notification(2, enabled = true)
        stubFetch(original, other)
        val toggled = notification(1, enabled = false)
        coEvery { notificationsClient.toggleNotification(1, any()) } returns
            TestFixtures.successResponse(NotificationDetailResponse(data = toggled))

        every { sessionManager.apiClient } returns apiClient
        val vm = NotificationsListViewModel(sessionManager)
        vm.initialFetch()
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        vm.toggle(original)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        val result = vm.state as LoadingResult.Success
        assertEquals(listOf(toggled, other), result.value)
        assertFalse(vm.errorToggle)
    }

    @Test
    fun `toggle sets errorToggle on failure and keeps state`() = runTest {
        val original = notification(1, enabled = true)
        stubFetch(original)
        coEvery {
            notificationsClient.toggleNotification(1, any())
        } throws Exception("test error")

        every { sessionManager.apiClient } returns apiClient
        val vm = NotificationsListViewModel(sessionManager)
        vm.initialFetch()
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        vm.toggle(original)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        assertTrue(vm.errorToggle)
        val result = vm.state as LoadingResult.Success
        assertEquals(listOf(original), result.value)
    }

    @Test
    fun `toggle does not surface cancellation`() = runTest {
        val original = notification(1, enabled = true)
        stubFetch(original)
        coEvery {
            notificationsClient.toggleNotification(1, any())
        } throws CancellationException("cancelled")

        every { sessionManager.apiClient } returns apiClient
        val vm = NotificationsListViewModel(sessionManager)
        vm.initialFetch()
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        vm.toggle(original)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        assertFalse(vm.errorToggle)
        val result = vm.state as LoadingResult.Success
        assertEquals(listOf(original), result.value)
    }

    @Test
    fun `delete removes item locally and sets didDelete`() = runTest {
        val n1 = notification(1)
        val n2 = notification(2)
        stubFetch(n1, n2)
        coEvery { notificationsClient.deleteNotification(1) } returns
            TestFixtures.successResponse(
                com.jgeek00.crowdsecmonitor.data.models.DeleteNotificationResponse(message = "deleted")
            )

        every { sessionManager.apiClient } returns apiClient
        val vm = NotificationsListViewModel(sessionManager)
        vm.initialFetch()
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        vm.delete(1)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        val result = vm.state as LoadingResult.Success
        assertEquals(listOf(n2), result.value)
        assertTrue(vm.didDelete)
        assertFalse(vm.errorDelete)
    }

    @Test
    fun `delete sets errorDelete on failure and keeps state`() = runTest {
        stubFetch(notification(1))
        coEvery { notificationsClient.deleteNotification(1) } throws Exception("test error")

        every { sessionManager.apiClient } returns apiClient
        val vm = NotificationsListViewModel(sessionManager)
        vm.initialFetch()
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        vm.delete(1)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        assertTrue(vm.errorDelete)
        assertFalse(vm.didDelete)
        val result = vm.state as LoadingResult.Success
        assertEquals(1, result.value.size)
    }

    @Test
    fun `delete does not surface cancellation`() = runTest {
        stubFetch(notification(1))
        coEvery { notificationsClient.deleteNotification(1) } throws CancellationException("cancelled")

        every { sessionManager.apiClient } returns apiClient
        val vm = NotificationsListViewModel(sessionManager)
        vm.initialFetch()
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        vm.delete(1)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        assertFalse(vm.errorDelete)
        assertFalse(vm.didDelete)
        val result = vm.state as LoadingResult.Success
        assertEquals(1, result.value.size)
    }

    @Test
    fun `reset clears state and action flags`() = runTest {
        stubFetch(notification(1))

        every { sessionManager.apiClient } returns apiClient
        val vm = NotificationsListViewModel(sessionManager)
        vm.initialFetch()
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        vm.reset()
        assertEquals(LoadingResult.Loading, vm.state)
        assertFalse(vm.errorToggle)
        assertFalse(vm.errorDelete)
        assertFalse(vm.didDelete)
    }
}
