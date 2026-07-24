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
    fun `validateName sets error for blank`() {
        vm.validateName("")
        assertEquals("Name field is required", vm.name.error)
    }

    @Test
    fun `validateName clears error for non-blank`() {
        vm.validateName("My Server")
        assertNull(vm.name.error)
        assertEquals("My Server", vm.name.value)
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
    fun `validatePort accepts blank as valid`() {
        vm.validatePort("")
        assertNull(vm.port.error)
    }

    @Test
    fun `validatePort accepts valid port`() {
        vm.validatePort("8080")
        assertNull(vm.port.error)
    }

    @Test
    fun `validatePort rejects non-numeric`() {
        vm.validatePort("abc")
        assertEquals("Port must be a valid number", vm.port.error)
    }

    @Test
    fun `validatePort rejects out of range`() {
        vm.validatePort("99999")
        assertEquals("Port must be between 1 and 65535", vm.port.error)
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
    fun `addCustomHeader adds a header`() {
        assertEquals(0, vm.customHeaders.size)
        vm.addCustomHeader()
        assertEquals(1, vm.customHeaders.size)
    }

    @Test
    fun `removeCustomHeader removes at index`() {
        vm.addCustomHeader()
        vm.addCustomHeader()
        assertEquals(2, vm.customHeaders.size)
        vm.removeCustomHeader(0)
        assertEquals(1, vm.customHeaders.size)
    }

    @Test
    fun `removeCustomHeader with invalid index does nothing`() {
        vm.removeCustomHeader(0)
        assertEquals(0, vm.customHeaders.size)
    }

    @Test
    fun `updateCustomHeaderKey updates key and validates`() {
        vm.addCustomHeader()
        vm.updateCustomHeaderKey(0, "X-Custom")
        assertEquals("X-Custom", vm.customHeaders[0].key)
        assertNull(vm.customHeaders[0].keyError)
    }

    @Test
    fun `updateCustomHeaderKey sets error for blank`() {
        vm.addCustomHeader()
        vm.updateCustomHeaderKey(0, "")
        assertEquals("Header name is required", vm.customHeaders[0].keyError)
    }

    @Test
    fun `updateCustomHeaderValue updates value`() {
        vm.addCustomHeader()
        vm.updateCustomHeaderValue(0, "my-value")
        assertEquals("my-value", vm.customHeaders[0].value)
        assertNull(vm.customHeaders[0].valueError)
    }

    @Test
    fun `updateCustomHeaderValue sets error for blank`() {
        vm.addCustomHeader()
        vm.updateCustomHeaderValue(0, "")
        assertEquals("Header value is required", vm.customHeaders[0].valueError)
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
