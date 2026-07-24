package com.jgeek00.crowdsecmonitor.viewmodel

import com.jgeek00.crowdsecmonitor.constants.Enums
import com.jgeek00.crowdsecmonitor.data.repository.ServerRepository
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ConnectionFormViewModelTest {

    private lateinit var vm: ConnectionFormViewModel

    @Before
    fun setUp() {
        val repo = mockk<ServerRepository>(relaxed = true)
        vm = ConnectionFormViewModel(mockk(relaxed = true), repo)
    }

    @Test
    fun `initial state is empty`() {
        assertEquals("", vm.name.value)
        assertEquals("", vm.port.value)
        assertEquals("", vm.path.value)
        assertEquals("", vm.basicUser.value)
        assertEquals("", vm.basicPassword.value)
        assertEquals("", vm.bearerToken.value)
        assertEquals(Enums.ConnectionMethod.HTTP, vm.connectionMethod)
        assertEquals(Enums.AuthMethod.NONE, vm.authMethod)
        assertFalse(vm.connecting)
        assertFalse(vm.connectionErrorAlert)
        assertTrue(vm.customHeaders.isEmpty())
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
    fun `validateBasicUser sets error when blank with BASIC auth`() {
        vm.authMethod = Enums.AuthMethod.BASIC
        vm.validateBasicUser("")
        assertEquals("Username is required", vm.basicUser.error)
    }

    @Test
    fun `validateBasicUser clears error when auth is not BASIC`() {
        vm.validateBasicUser("")
        assertNull(vm.basicUser.error)
    }

    @Test
    fun `validateBasicPassword sets error when blank with BASIC auth`() {
        vm.authMethod = Enums.AuthMethod.BASIC
        vm.validateBasicPassword("")
        assertEquals("Password is required", vm.basicPassword.error)
    }

    @Test
    fun `validateBasicPassword clears error when auth is not BASIC`() {
        vm.validateBasicPassword("")
        assertNull(vm.basicPassword.error)
    }

    @Test
    fun `validateBearerToken sets error when blank with BEARER auth`() {
        vm.authMethod = Enums.AuthMethod.BEARER
        vm.validateBearerToken("")
        assertEquals("Token is required", vm.bearerToken.error)
    }

    @Test
    fun `validateBearerToken clears error when auth is not BEARER`() {
        vm.validateBearerToken("")
        assertNull(vm.bearerToken.error)
    }

    @Test
    fun `addCustomHeader adds a header`() {
        assertEquals(0, vm.customHeaders.size)
        vm.addCustomHeader()
        assertEquals(1, vm.customHeaders.size)
    }

    @Test
    fun `addCustomHeader respects max`() {
        repeat(11) { vm.addCustomHeader() }
        assertEquals(MAX_CUSTOM_HEADERS, vm.customHeaders.size)
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
    fun `updateCustomHeaderKey sets error for invalid characters`() {
        vm.addCustomHeader()
        vm.updateCustomHeaderKey(0, "bad header!")
        assertEquals("Header name contains invalid characters", vm.customHeaders[0].keyError)
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
        vm.authMethod = Enums.AuthMethod.BASIC
        vm.addCustomHeader()
        vm.reset()
        assertEquals("", vm.name.value)
        assertNull(vm.name.error)
        assertEquals(0, vm.customHeaders.size)
        assertEquals(Enums.AuthMethod.NONE, vm.authMethod)
    }
}
