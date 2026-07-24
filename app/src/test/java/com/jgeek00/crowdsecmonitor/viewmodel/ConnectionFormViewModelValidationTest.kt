package com.jgeek00.crowdsecmonitor.viewmodel

import com.jgeek00.crowdsecmonitor.constants.Enums
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class ConnectionFormViewModelValidationTest {

    private lateinit var vm: ConnectionFormViewModel

    @Before
    fun setUp() {
        val repo = mockk<com.jgeek00.crowdsecmonitor.data.repository.ServerRepository>(relaxed = true)
        vm = ConnectionFormViewModel(RuntimeEnvironment.getApplication(), repo)
    }

    @Test
    fun `initial state is empty`() {
        assertEquals("", vm.name.value)
        assertEquals("", vm.ipDomain.value)
        assertEquals(Enums.ConnectionMethod.HTTP, vm.connectionMethod)
        assertEquals(Enums.AuthMethod.NONE, vm.authMethod)
        assertFalse(vm.connecting)
        assertFalse(vm.connectionErrorAlert)
    }

    @Test
    fun `validateIpDomain sets error for blank`() {
        vm.validateIpDomain("")
        assertEquals("IP/Domain field is required", vm.ipDomain.error)
    }

    @Test
    fun `validateIpDomain accepts valid IPv4`() {
        vm.validateIpDomain("192.168.1.1")
        assertNull(vm.ipDomain.error)
    }

    @Test
    fun `validateIpDomain accepts valid domain`() {
        vm.validateIpDomain("example.com")
        assertNull(vm.ipDomain.error)
    }

    @Test
    fun `validateIpDomain rejects invalid input`() {
        vm.validateIpDomain("invalid!")
        assertEquals("IP/Domain value is not valid", vm.ipDomain.error)
    }

    @Test
    fun `validateAll returns false when name is blank`() {
        vm.validateName("")
        vm.validateIpDomain("192.168.1.1")
        assertFalse(vm.validateAll())
    }

    @Test
    fun `validateAll returns false when ip is blank`() {
        vm.validateName("Server")
        vm.validateIpDomain("")
        assertFalse(vm.validateAll())
    }

    @Test
    fun `validateAll returns true for valid inputs`() {
        vm.validateName("Server")
        vm.validateIpDomain("192.168.1.1")
        vm.validatePort("8080")
        assertTrue(vm.validateAll())
    }

    @Test
    fun `reset clears all state`() {
        vm.validateName("Server")
        vm.validateIpDomain("192.168.1.1")
        vm.authMethod = Enums.AuthMethod.BASIC
        vm.addCustomHeader()
        vm.reset()
        assertEquals("", vm.name.value)
        assertNull(vm.name.error)
        assertEquals(0, vm.customHeaders.size)
        assertEquals(Enums.AuthMethod.NONE, vm.authMethod)
    }
}
