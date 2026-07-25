package com.jgeek00.crowdsecmonitor.viewmodel

import com.jgeek00.crowdsecmonitor.MainDispatcherRule
import com.jgeek00.crowdsecmonitor.data.repository.ServerRepository
import com.jgeek00.crowdsecmonitor.fixtures.TestFixtures
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ConnectionFormViewModelConnectTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var mockWebServer: MockWebServer
    private lateinit var repo: ServerRepository
    private lateinit var vm: ConnectionFormViewModel

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
        repo = mockk(relaxed = true)
        vm = ConnectionFormViewModel(mockk(relaxed = true), repo)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `connect returns false when validation fails`() = runTest {
        val result = vm.connect()
        assertFalse(result)
    }

    @Test
    fun `connect succeeds when API responds ok`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(TestFixtures.apiStatusJson))

        vm.validateName("Test Server")
        vm.validateIpDomain("127.0.0.1")
        vm.validatePort(mockWebServer.port.toString())

        val result = vm.connect()
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        assertTrue(result)
        assertFalse(vm.connectionErrorAlert)
    }

    @Test
    fun `connect handles 401 unauthorized`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(401))

        vm.validateName("Test Server")
        vm.validateIpDomain("127.0.0.1")
        vm.validatePort(mockWebServer.port.toString())

        val result = vm.connect()
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        assertFalse(result)
        assertTrue(vm.connectionErrorAlert)
    }

    @Test
    fun `connect handles 500 server error`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(500))

        vm.validateName("Test Server")
        vm.validateIpDomain("127.0.0.1")
        vm.validatePort(mockWebServer.port.toString())

        val result = vm.connect()
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        assertFalse(result)
        assertTrue(vm.connectionErrorAlert)
    }

    @Test
    fun `connect handles network error`() = runTest {
        val badVm = ConnectionFormViewModel(mockk(relaxed = true), repo)
        badVm.validateName("Test Server")
        badVm.validateIpDomain("127.0.0.1")
        badVm.validatePort("1")

        val result = badVm.connect()
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        assertFalse(result)
        assertTrue(badVm.connectionErrorAlert)
    }

    @Test
    fun `connect with custom headers succeeds`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(TestFixtures.apiStatusJson))

        vm.validateName("Test Server")
        vm.validateIpDomain("127.0.0.1")
        vm.validatePort(mockWebServer.port.toString())
        vm.addCustomHeader()
        vm.updateCustomHeaderKey(0, "X-Test")
        vm.updateCustomHeaderValue(0, "value")

        val result = vm.connect()
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        assertTrue(result)
        assertFalse(vm.connectionErrorAlert)
    }

    @Test
    fun `connect with BASIC auth succeeds`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(TestFixtures.apiStatusJson))

        vm.validateName("Test Server")
        vm.validateIpDomain("127.0.0.1")
        vm.validatePort(mockWebServer.port.toString())
        vm.authMethod = com.jgeek00.crowdsecmonitor.constants.Enums.AuthMethod.BASIC
        vm.validateBasicUser("user")
        vm.validateBasicPassword("pass")

        val result = vm.connect()
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        assertTrue(result)
        assertFalse(vm.connectionErrorAlert)
    }
}
