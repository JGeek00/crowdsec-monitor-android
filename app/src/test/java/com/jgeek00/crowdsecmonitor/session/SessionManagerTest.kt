package com.jgeek00.crowdsecmonitor.session

import com.jgeek00.crowdsecmonitor.fixtures.TestFixtures
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SessionManagerTest {

    @Test
    fun `initial state has no server configured`() {
        val sm = SessionManager()
        assertNull(sm.currentServer)
        assertNull(sm.apiClient)
        assertFalse(sm.hasServerConfigured)
    }

    @Test
    fun `activate with authMethod none sets apiClient and currentServer`() = runTest {
        val sm = SessionManager()
        val server = TestFixtures.csserverModel(authMethod = "none")
        sm.activate(server)
        assertNotNull(sm.apiClient)
        assertEquals(server, sm.currentServer)
    }

    @Test
    fun `activate with null basicUser and basicPassword`() = runTest {
        val sm = SessionManager()
        val server = TestFixtures.csserverModel(
            authMethod = "basic",
            basicUser = null,
            basicPassword = null
        )
        sm.activate(server)
        assertNotNull(sm.apiClient)
        assertEquals(server, sm.currentServer)
    }

    @Test
    fun `hasServerConfigured returns true after activate`() = runTest {
        val sm = SessionManager()
        val server = TestFixtures.csserverModel()
        sm.activate(server)
        assertTrue(sm.hasServerConfigured)
    }

    @Test
    fun `hasServerConfigured returns false after deactivate`() = runTest {
        val sm = SessionManager()
        sm.activate(TestFixtures.csserverModel())
        sm.deactivate()
        assertFalse(sm.hasServerConfigured)
        assertNull(sm.currentServer)
        assertNull(sm.apiClient)
    }

    @Test
    fun `triggerDecisionsRefresh emits event`() = runTest {
        val sm = SessionManager()
        sm.activate(TestFixtures.csserverModel())

        val received = mutableListOf<Unit>()
        val job = launch {
            sm.decisionsRefreshEvent.collect { received.add(it) }
        }
        advanceUntilIdle()

        sm.triggerDecisionsRefresh()
        advanceUntilIdle()

        assertEquals(1, received.size)
        job.cancel()
    }

    @Test
    fun `triggerAlertsRefresh emits event`() = runTest {
        val sm = SessionManager()
        sm.activate(TestFixtures.csserverModel())

        val received = mutableListOf<Unit>()
        val job = launch {
            sm.alertsRefreshEvent.collect { received.add(it) }
        }
        advanceUntilIdle()

        sm.triggerAlertsRefresh()
        advanceUntilIdle()

        assertEquals(1, received.size)
        job.cancel()
    }
}
