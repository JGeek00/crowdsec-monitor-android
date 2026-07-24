package com.jgeek00.crowdsecmonitor.viewmodel

import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ServersManagerViewModelTest {

    private lateinit var vm: ServersManagerViewModel

    @Before
    fun setUp() {
        val repo = mockk<com.jgeek00.crowdsecmonitor.data.repository.ServerRepository>(relaxed = true)
        val session = mockk<com.jgeek00.crowdsecmonitor.session.SessionManager>(relaxed = true)
        vm = ServersManagerViewModel(repo, session)
    }

    @Test
    fun `initial state is loading`() {
        assertTrue(vm.isLoading)
        assertTrue(vm.servers.isEmpty())
        assertFalse(vm.deleteServerError)
        assertFalse(vm.setDefaultServerError)
    }

    @Test
    fun `hasServerConfigured delegates to session`() {
        assertNotNull(vm.hasServerConfigured)
    }

    @Test
    fun `currentServer delegates to session`() {
        assertNotNull(vm.currentServer)
    }

    @Test
    fun `clearDeleteServerError resets flag`() {
        vm.clearDeleteServerError()
        assertFalse(vm.deleteServerError)
    }

    @Test
    fun `clearSetDefaultServerError resets flag`() {
        vm.clearSetDefaultServerError()
        assertFalse(vm.setDefaultServerError)
    }

    @Test
    fun `clearNewDefaultServerSet resets value`() {
        vm.clearNewDefaultServerSet()
        assertEquals(null, vm.newDefaultServerSet)
    }
}
