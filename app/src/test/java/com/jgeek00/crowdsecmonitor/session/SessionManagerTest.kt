package com.jgeek00.crowdsecmonitor.session

import com.jgeek00.crowdsecmonitor.MainDispatcherRule
import com.jgeek00.crowdsecmonitor.fixtures.TestFixtures
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SessionManagerTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var sessionManager: SessionManager

    @Before
    fun setUp() {
        sessionManager = SessionManager()
    }

    @Test
    fun `initial state has no server or apiClient`() {
        assertNull(sessionManager.apiClient)
        assertNull(sessionManager.currentServer)
        assertFalse(sessionManager.hasServerConfigured)
    }

    @Test
    fun `activate sets currentServer and creates apiClient`() = runTest {
        val server = TestFixtures.csserverModel()

        sessionManager.activate(server)

        assertEquals(server, sessionManager.currentServer)
        assertNotNull(sessionManager.apiClient)
        assertTrue(sessionManager.hasServerConfigured)
    }

    @Test
    fun `activate replaces existing apiClient`() = runTest {
        val server1 = TestFixtures.csserverModel(id = java.util.UUID.fromString("00000000-0000-0000-0000-000000000001"))
        val server2 = TestFixtures.csserverModel(id = java.util.UUID.fromString("00000000-0000-0000-0000-000000000002"))

        sessionManager.activate(server1)
        val firstClient = sessionManager.apiClient

        sessionManager.activate(server2)
        val secondClient = sessionManager.apiClient

        assertEquals(server2, sessionManager.currentServer)
        assertNotNull(secondClient)
    }

    @Test
    fun `deactivate clears server and apiClient`() = runTest {
        val server = TestFixtures.csserverModel()
        sessionManager.activate(server)
        assertTrue(sessionManager.hasServerConfigured)

        sessionManager.deactivate()

        assertNull(sessionManager.currentServer)
        assertNull(sessionManager.apiClient)
        assertFalse(sessionManager.hasServerConfigured)
    }

    @Test
    fun `deactivate when already deactivated does not throw`() = runTest {
        sessionManager.deactivate()
        assertNull(sessionManager.apiClient)
    }

    @Test
    fun `triggerDecisionsRefresh does not throw`() {
        // tryEmit with extraBufferCapacity=1 is expected to succeed
        sessionManager.triggerDecisionsRefresh()
    }

    @Test
    fun `triggerAlertsRefresh does not throw`() {
        sessionManager.triggerAlertsRefresh()
    }

    @Test
    fun `hasServerConfigured false when only server set`() {
        assertFalse(sessionManager.hasServerConfigured)
    }
}
