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
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class ConnectionFormViewModelBranchTest {

    private lateinit var vm: ConnectionFormViewModel

    @Before
    fun setUp() {
        val repo = mockk<ServerRepository>(relaxed = true)
        vm = ConnectionFormViewModel(RuntimeEnvironment.getApplication(), repo)
    }

    @Test
    fun `validatePath sets value and clears error`() {
        vm.validatePath("/api/v1")
        assertEquals("/api/v1", vm.path.value)
        assertNull(vm.path.error)
    }

    @Test
    fun `validateIpDomain accepts valid domain`() {
        vm.validateIpDomain("example.com")
        assertNull(vm.ipDomain.error)
    }

    @Test
    fun `validateIpDomain accepts valid IPv4`() {
        vm.validateIpDomain("192.168.1.1")
        assertNull(vm.ipDomain.error)
    }

    @Test
    fun `validateIpDomain rejects invalid string`() {
        vm.validateIpDomain("not valid!")
        assertEquals("IP/Domain value is not valid", vm.ipDomain.error)
    }

    @Test
    fun `validateIpDomain sets error for blank`() {
        vm.validateIpDomain("")
        assertEquals("IP/Domain field is required", vm.ipDomain.error)
    }

    @Test
    fun `validateAll returns true for valid inputs no auth`() {
        vm.validateName("Server")
        vm.validateIpDomain("192.168.1.1")
        vm.validatePort("8080")
        assertTrue(vm.validateAll())
    }

    @Test
    fun `validateAll validates basic auth when BASIC`() {
        vm.validateName("Server")
        vm.validateIpDomain("192.168.1.1")
        vm.authMethod = Enums.AuthMethod.BASIC
        assertFalse(vm.validateAll())
        assertEquals("Username is required", vm.basicUser.error)
        assertEquals("Password is required", vm.basicPassword.error)
    }

    @Test
    fun `validateAll validates bearer token when BEARER`() {
        vm.validateName("Server")
        vm.validateIpDomain("192.168.1.1")
        vm.authMethod = Enums.AuthMethod.BEARER
        assertFalse(vm.validateAll())
        assertEquals("Token is required", vm.bearerToken.error)
    }

    @Test
    fun `validateAll passes with BASIC auth fields filled`() {
        vm.validateName("Server")
        vm.validateIpDomain("192.168.1.1")
        vm.validatePort("8080")
        vm.authMethod = Enums.AuthMethod.BASIC
        vm.validateBasicUser("user")
        vm.validateBasicPassword("pass")
        assertTrue(vm.validateAll())
    }

    @Test
    fun `validateAll passes with BEARER auth fields filled`() {
        vm.validateName("Server")
        vm.validateIpDomain("192.168.1.1")
        vm.validatePort("8080")
        vm.authMethod = Enums.AuthMethod.BEARER
        vm.validateBearerToken("token")
        assertTrue(vm.validateAll())
    }

    @Test
    fun `validateAll returns false when custom headers invalid`() {
        vm.validateName("Server")
        vm.validateIpDomain("192.168.1.1")
        vm.addCustomHeader()
        assertFalse(vm.validateAll())
    }

    @Test
    fun `updateCustomHeaderKey with invalid index does nothing`() {
        vm.updateCustomHeaderKey(99, "test")
    }

    @Test
    fun `updateCustomHeaderValue with invalid index does nothing`() {
        vm.updateCustomHeaderValue(99, "test")
    }

    @Test
    fun `removeCustomHeader with invalid index does nothing`() {
        vm.removeCustomHeader(99)
        assertTrue(vm.customHeaders.isEmpty())
    }

    @Test
    fun `validateBasicUser no error when auth is BASIC and value provided`() {
        vm.authMethod = Enums.AuthMethod.BASIC
        vm.validateBasicUser("myuser")
        assertEquals("myuser", vm.basicUser.value)
        assertNull(vm.basicUser.error)
    }

    @Test
    fun `validateBasicPassword no error when auth is BASIC and value provided`() {
        vm.authMethod = Enums.AuthMethod.BASIC
        vm.validateBasicPassword("mypass")
        assertEquals("mypass", vm.basicPassword.value)
        assertNull(vm.basicPassword.error)
    }

    @Test
    fun `validateBearerToken no error when auth is BEARER and value provided`() {
        vm.authMethod = Enums.AuthMethod.BEARER
        vm.validateBearerToken("mytoken")
        assertEquals("mytoken", vm.bearerToken.value)
        assertNull(vm.bearerToken.error)
    }

    @Test
    fun `validatePort edge cases`() {
        vm.validatePort("0")
        assertEquals("Port must be between 1 and 65535", vm.port.error)

        vm.validatePort("1")
        assertNull(vm.port.error)

        vm.validatePort("65535")
        assertNull(vm.port.error)

        vm.validatePort("65536")
        assertEquals("Port must be between 1 and 65535", vm.port.error)
    }

    @Test
    fun `reset clears error state`() {
        vm.connectionErrorAlert = true
        vm.reset()
        assertFalse(vm.connectionErrorAlert)
    }
}
