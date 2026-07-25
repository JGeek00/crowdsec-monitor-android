package com.jgeek00.crowdsecmonitor.viewmodel

import com.jgeek00.crowdsecmonitor.constants.Enums
import com.jgeek00.crowdsecmonitor.data.repository.ServerRepository
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ConnectionFormViewModelBranchCoverTest {

    private lateinit var vm: ConnectionFormViewModel

    @Before
    fun setUp() {
        val repo = mockk<ServerRepository>(relaxed = true)
        vm = ConnectionFormViewModel(mockk(relaxed = true), repo)
    }

    // ── Custom headers ────────────────────────────────────────

    @Test
    fun `addCustomHeader at max does not add`() {
        // Fill to max first
        repeat(MAX_CUSTOM_HEADERS) { vm.addCustomHeader() }
        assertEquals(MAX_CUSTOM_HEADERS, vm.customHeaders.size)

        // Try adding one more
        vm.addCustomHeader()
        assertEquals(MAX_CUSTOM_HEADERS, vm.customHeaders.size)
    }

    @Test
    fun `removeCustomHeader with valid index removes it`() {
        vm.addCustomHeader()
        vm.addCustomHeader()
        assertEquals(2, vm.customHeaders.size)

        vm.removeCustomHeader(0)
        assertEquals(1, vm.customHeaders.size)
    }

    @Test
    fun `updateCustomHeaderKey with valid index updates`() {
        vm.addCustomHeader()
        vm.updateCustomHeaderKey(0, "X-Custom")
        assertEquals("X-Custom", vm.customHeaders[0].key)
        assertNull(vm.customHeaders[0].keyError)
    }

    @Test
    fun `updateCustomHeaderValue with valid index and blank sets error`() {
        vm.addCustomHeader()
        vm.updateCustomHeaderValue(0, "")
        assertEquals("Header value is required", vm.customHeaders[0].valueError)
    }

    @Test
    fun `updateCustomHeaderValue with valid index and non-blank clears error`() {
        vm.addCustomHeader()
        vm.updateCustomHeaderValue(0, "my-value")
        assertEquals("my-value", vm.customHeaders[0].value)
        assertNull(vm.customHeaders[0].valueError)
    }

    // ── validatePath ──────────────────────────────────────────

    @Test
    fun `validatePath sets value and clears error`() {
        vm.validatePath("/api/v1")
        assertEquals("/api/v1", vm.path.value)
        assertNull(vm.path.error)
    }

    @Test
    fun `validatePath with blank sets value and clears error`() {
        vm.validatePath("")
        assertEquals("", vm.path.value)
        assertNull(vm.path.error)
    }

    // ── validateBasicUser / Password / Bearer with non-matching auth ──

    @Test
    fun `validateBasicPassword with BASIC auth and value clears error`() {
        vm.authMethod = Enums.AuthMethod.BASIC
        vm.validateBasicPassword("mypass")
        assertNull(vm.basicPassword.error)
    }

    @Test
    fun `validateBearerToken with BEARER auth and value clears error`() {
        vm.authMethod = Enums.AuthMethod.BEARER
        vm.validateBearerToken("mytoken")
        assertNull(vm.bearerToken.error)
    }

    // ── validateAll - edge cases ──────────────────────────────

    @Test
    fun `validateAll returns true when all fields valid`() {
        vm.validateName("Server")
        vm.validateIpDomain("192.168.1.1")
        vm.validatePort("8080")
        vm.validatePath("/api")
        assertTrue(vm.validateAll())
    }

    @Test
    fun `validateAll accepts valid domain name`() {
        vm.validateName("Server")
        vm.validateIpDomain("example.com")
        vm.validatePort("8080")
        assertTrue(vm.validateAll())
    }

    @Test
    fun `validateAll accepts subdomain`() {
        vm.validateName("Server")
        vm.validateIpDomain("sub.example.co.uk")
        vm.validatePort("8080")
        assertTrue(vm.validateAll())
    }

    @Test
    fun `validateAll validates custom header with blank value`() {
        vm.validateName("Server")
        vm.validateIpDomain("192.168.1.1")
        vm.addCustomHeader()
        vm.updateCustomHeaderKey(0, "X-Valid")
        // Leave value blank - should fail validation
        assertFalse(vm.validateAll())
    }

    @Test
    fun `validateAll with BASIC auth validates correctly`() {
        vm.validateName("Server")
        vm.validateIpDomain("192.168.1.1")
        vm.validatePort("8080")
        vm.authMethod = Enums.AuthMethod.BASIC
        vm.validateBasicUser("u")
        vm.validateBasicPassword("p")
        assertTrue(vm.validateAll())
    }

    @Test
    fun `validateAll with BEARER auth validates correctly`() {
        vm.validateName("Server")
        vm.validateIpDomain("192.168.1.1")
        vm.validatePort("8080")
        vm.authMethod = Enums.AuthMethod.BEARER
        vm.validateBearerToken("t")
        assertTrue(vm.validateAll())
    }

    @Test
    fun `validateAll returns false when ip domain is blank`() {
        vm.validateName("Server")
        vm.validateIpDomain("")
        vm.validatePort("8080")
        assertFalse(vm.validateAll())
    }

    @Test
    fun `reset resets connectionErrorAlert`() {
        vm.connectionErrorAlert = true
        vm.reset()
        assertFalse(vm.connectionErrorAlert)
    }
}
