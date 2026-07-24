package com.jgeek00.crowdsecmonitor.viewmodel

import com.jgeek00.crowdsecmonitor.MainDispatcherRule
import com.jgeek00.crowdsecmonitor.data.db.CSServerModel
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
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ServersManagerViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val serverRepository = mockk<ServerRepository>(relaxed = true)
    private val sessionManager = mockk<SessionManager>(relaxed = true)
    private lateinit var vm: ServersManagerViewModel

    @Before
    fun setUp() {
        every { sessionManager.apiClient } returns null
        every { sessionManager.currentServer } returns null
        every { sessionManager.hasServerConfigured } returns false
    }

    @Test
    fun `initial state is loading`() = runTest {
        val flow = MutableStateFlow(listOf<CSServerModel>())
        every { serverRepository.getAllServers() } returns flow

        vm = ServersManagerViewModel(serverRepository, sessionManager)

        assertTrue(vm.isLoading)
        assertTrue(vm.servers.isEmpty())
        assertFalse(vm.deleteServerError)
        assertFalse(vm.setDefaultServerError)
    }

    @Test
    fun `loadServers success updates state`() = runTest {
        val server1 = TestFixtures.csserverModel(name = "Server 1")
        val server2 = TestFixtures.csserverModel(name = "Server 2", defaultServer = true)
        val flow = MutableStateFlow(listOf(server1, server2))
        every { serverRepository.getAllServers() } returns flow

        vm = ServersManagerViewModel(serverRepository, sessionManager)
        advanceUntilIdle()

        assertFalse(vm.isLoading)
        assertEquals(2, vm.servers.size)
        assertEquals("Server 1", vm.servers[0].name)
        assertEquals("Server 2", vm.servers[1].name)
    }

    @Test
    fun `loadServers empty list`() = runTest {
        val flow = MutableStateFlow(emptyList<CSServerModel>())
        every { serverRepository.getAllServers() } returns flow

        vm = ServersManagerViewModel(serverRepository, sessionManager)
        advanceUntilIdle()

        assertFalse(vm.isLoading)
        assertTrue(vm.servers.isEmpty())
        coVerify { sessionManager.deactivate() }
    }

    @Test
    fun `loadServers activates appropriate server`() = runTest {
        val server = TestFixtures.csserverModel(name = "Active", defaultServer = true)
        val flow = MutableStateFlow(listOf(server))
        every { serverRepository.getAllServers() } returns flow

        vm = ServersManagerViewModel(serverRepository, sessionManager)
        advanceUntilIdle()

        coVerify { sessionManager.activate(server) }
    }

    @Test
    fun `deleteServer success`() = runTest {
        val server = TestFixtures.csserverModel()
        val flow = MutableStateFlow(listOf(server))
        every { serverRepository.getAllServers() } returns flow
        vm = ServersManagerViewModel(serverRepository, sessionManager)
        advanceUntilIdle()

        vm.deleteServer(server)
        advanceUntilIdle()

        coVerify { serverRepository.deleteServer(server) }
        assertFalse(vm.deleteServerError)
    }

    @Test
    fun `deleteServer failure sets error`() = runTest {
        val server = TestFixtures.csserverModel()
        val flow = MutableStateFlow(listOf(server))
        every { serverRepository.getAllServers() } returns flow
        coEvery { serverRepository.deleteServer(any()) } throws Exception("db error")
        vm = ServersManagerViewModel(serverRepository, sessionManager)
        advanceUntilIdle()

        vm.deleteServer(server)
        advanceUntilIdle()

        assertTrue(vm.deleteServerError)
    }

    @Test
    fun `setDefaultServer success`() = runTest {
        val server = TestFixtures.csserverModel(name = "Default Server")
        val flow = MutableStateFlow(listOf(server))
        every { serverRepository.getAllServers() } returns flow
        vm = ServersManagerViewModel(serverRepository, sessionManager)
        advanceUntilIdle()

        vm.setDefaultServer(server)
        advanceUntilIdle()

        coVerify { serverRepository.setDefaultServer(server.id) }
        assertEquals("Default Server", vm.newDefaultServerSet)
        assertFalse(vm.setDefaultServerError)
    }

    @Test
    fun `setDefaultServer failure sets error`() = runTest {
        val server = TestFixtures.csserverModel()
        val flow = MutableStateFlow(listOf(server))
        every { serverRepository.getAllServers() } returns flow
        coEvery { serverRepository.setDefaultServer(any()) } throws Exception("db error")
        vm = ServersManagerViewModel(serverRepository, sessionManager)
        advanceUntilIdle()

        vm.setDefaultServer(server)
        advanceUntilIdle()

        assertTrue(vm.setDefaultServerError)
        assertNull(vm.newDefaultServerSet)
    }

    @Test
    fun `clearDeleteServerError resets flag`() {
        val flow = MutableStateFlow(emptyList<CSServerModel>())
        every { serverRepository.getAllServers() } returns flow
        vm = ServersManagerViewModel(serverRepository, sessionManager)

        vm.clearDeleteServerError()
        assertFalse(vm.deleteServerError)
    }

    @Test
    fun `clearSetDefaultServerError resets flag`() {
        val flow = MutableStateFlow(emptyList<CSServerModel>())
        every { serverRepository.getAllServers() } returns flow
        vm = ServersManagerViewModel(serverRepository, sessionManager)

        vm.clearSetDefaultServerError()
        assertFalse(vm.setDefaultServerError)
    }

    @Test
    fun `clearNewDefaultServerSet resets value`() {
        val flow = MutableStateFlow(emptyList<CSServerModel>())
        every { serverRepository.getAllServers() } returns flow
        vm = ServersManagerViewModel(serverRepository, sessionManager)

        vm.clearNewDefaultServerSet()
        assertNull(vm.newDefaultServerSet)
    }

    @Test
    fun `currentServer delegates to session`() {
        val flow = MutableStateFlow(emptyList<CSServerModel>())
        every { serverRepository.getAllServers() } returns flow
        vm = ServersManagerViewModel(serverRepository, sessionManager)

        assertEquals(vm.currentServer, sessionManager.currentServer)
    }

    @Test
    fun `hasServerConfigured delegates to session`() {
        val flow = MutableStateFlow(emptyList<CSServerModel>())
        every { serverRepository.getAllServers() } returns flow
        vm = ServersManagerViewModel(serverRepository, sessionManager)

        assertEquals(vm.hasServerConfigured, sessionManager.hasServerConfigured)
    }
}
