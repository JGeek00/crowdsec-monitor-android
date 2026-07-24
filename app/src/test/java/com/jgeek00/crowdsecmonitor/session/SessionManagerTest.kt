package com.jgeek00.crowdsecmonitor.session

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class SessionManagerTest {

    @Test
    fun `initial state has no server configured`() {
        val sm = SessionManager()
        assertNull(sm.currentServer)
        assertNull(sm.apiClient)
        assertFalse(sm.hasServerConfigured)
    }
}
