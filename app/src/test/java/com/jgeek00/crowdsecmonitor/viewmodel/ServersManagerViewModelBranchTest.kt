package com.jgeek00.crowdsecmonitor.viewmodel

import com.jgeek00.crowdsecmonitor.MainDispatcherRule
import com.jgeek00.crowdsecmonitor.data.api.CrowdSecApiClient
import com.jgeek00.crowdsecmonitor.data.repository.ServerRepository
import com.jgeek00.crowdsecmonitor.fixtures.TestFixtures
import com.jgeek00.crowdsecmonitor.session.SessionManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ServersManagerViewModelBranchTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `activateAppropriateServer picks first when no default`() = runTest {
        val serverRepository = mockk<ServerRepository>(relaxed = true)
        val sessionManager = mockk<SessionManager>(relaxed = true)
        every { sessionManager.apiClient } returns null
        every { sessionManager.currentServer } returns null

        val server = TestFixtures.csserverModel(name = "First Server", defaultServer = false)
        val flow = MutableStateFlow(listOf(server))
        every { serverRepository.getAllServers() } returns flow

        val vm = ServersManagerViewModel(serverRepository, sessionManager)
        advanceUntilIdle()

        coVerify { sessionManager.activate(server) }
    }

    @Test
    fun `activateAppropriateServer same server does not re-activate`() = runTest {
        val serverRepository = mockk<ServerRepository>(relaxed = true)
        val sessionManager = mockk<SessionManager>(relaxed = true)
        val apiClient = mockk<CrowdSecApiClient>(relaxed = true)
        val server = TestFixtures.csserverModel(name = "Same", defaultServer = true)
        every { sessionManager.apiClient } returns apiClient
        every { sessionManager.currentServer } returns server

        val flow = MutableStateFlow(listOf(server))
        every { serverRepository.getAllServers() } returns flow

        val vm = ServersManagerViewModel(serverRepository, sessionManager)
        advanceUntilIdle()

        assertFalse(vm.isLoading)
    }

    @Test
    fun `changeCurrentServer with different server switches`() = runTest {
        val serverRepository = mockk<ServerRepository>(relaxed = true)
        val sessionManager = mockk<SessionManager>(relaxed = true)
        val current = TestFixtures.csserverModel(id = java.util.UUID.fromString("00000000-0000-0000-0000-000000000001"))
        val newServer = TestFixtures.csserverModel(id = java.util.UUID.fromString("00000000-0000-0000-0000-000000000002"))
        every { sessionManager.currentServer } returns current
        every { sessionManager.apiClient } returns null

        val flow = MutableStateFlow(listOf(current))
        every { serverRepository.getAllServers() } returns flow

        val vm = ServersManagerViewModel(serverRepository, sessionManager)
        advanceUntilIdle()

        vm.changeCurrentServer(newServer)
        advanceUntilIdle()

        coVerify { sessionManager.activate(newServer) }
    }

    @Test
    fun `changeCurrentServer with same id does not crash`() = runTest {
        val serverRepository = mockk<ServerRepository>(relaxed = true)
        val sessionManager = mockk<SessionManager>(relaxed = true)
        val server = TestFixtures.csserverModel(name = "Same")
        every { sessionManager.currentServer } returns server
        every { sessionManager.apiClient } returns mockk(relaxed = true)

        val flow = MutableStateFlow(listOf(server))
        every { serverRepository.getAllServers() } returns flow

        val vm = ServersManagerViewModel(serverRepository, sessionManager)
        advanceUntilIdle()

        vm.changeCurrentServer(server)
        advanceUntilIdle()
    }

    @Test
    fun `changeCurrentServer with different id activates`() = runTest {
        val serverRepository = mockk<ServerRepository>(relaxed = true)
        val sessionManager = mockk<SessionManager>(relaxed = true)
        val current = TestFixtures.csserverModel(
            id = java.util.UUID.fromString("00000000-0000-0000-0000-000000000001"),
            name = "Current"
        )
        val newServer = TestFixtures.csserverModel(
            id = java.util.UUID.fromString("00000000-0000-0000-0000-000000000002"),
            name = "New"
        )
        every { sessionManager.currentServer } returns current
        every { sessionManager.apiClient } returns null

        val flow = MutableStateFlow(listOf(current, newServer))
        every { serverRepository.getAllServers() } returns flow

        val vm = ServersManagerViewModel(serverRepository, sessionManager)
        advanceUntilIdle()

        vm.changeCurrentServer(newServer)
        advanceUntilIdle()

        coVerify { sessionManager.activate(newServer) }
    }

    @Test
    fun `servers list is updated when flow emits new value`() = runTest {
        val serverRepository = mockk<ServerRepository>(relaxed = true)
        val sessionManager = mockk<SessionManager>(relaxed = true)
        every { sessionManager.apiClient } returns null
        every { sessionManager.currentServer } returns null

        val server1 = TestFixtures.csserverModel(name = "Server 1")
        val flow = MutableStateFlow(listOf(server1))
        every { serverRepository.getAllServers() } returns flow

        val vm = ServersManagerViewModel(serverRepository, sessionManager)
        advanceUntilIdle()
        assertEquals(1, vm.servers.size)

        val server2 = TestFixtures.csserverModel(name = "Server 2")
        flow.value = listOf(server1, server2)
        advanceUntilIdle()

        assertEquals(2, vm.servers.size)
    }
}
