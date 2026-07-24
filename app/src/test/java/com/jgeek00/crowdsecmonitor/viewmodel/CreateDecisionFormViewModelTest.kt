package com.jgeek00.crowdsecmonitor.viewmodel

import com.jgeek00.crowdsecmonitor.constants.Enums
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CreateDecisionFormViewModelTest {

    private lateinit var vm: CreateDecisionFormViewModel

    @Before
    fun setUp() {
        vm = CreateDecisionFormViewModel(mockk(relaxed = true))
    }

    @Test
    fun `initial state is default values`() {
        assertEquals("", vm.ipAddress)
        assertEquals(0, vm.durationDays)
        assertEquals(4, vm.durationHours)
        assertEquals(0, vm.durationMinutes)
        assertEquals(Enums.DecisionType.BAN, vm.type)
        assertEquals("", vm.reason)
        assertFalse(vm.creatingDecision)
    }

    @Test
    fun `durationString returns combined components`() {
        vm.durationDays = 1
        vm.durationHours = 2
        vm.durationMinutes = 30
        assertEquals("1d2h30m", vm.durationString)
    }

    @Test
    fun `durationString skips zero components`() {
        vm.durationDays = 0
        vm.durationHours = 4
        vm.durationMinutes = 0
        assertEquals("4h", vm.durationString)
    }

    @Test
    fun `durationString returns empty when all zero`() {
        vm.durationDays = 0
        vm.durationHours = 0
        vm.durationMinutes = 0
        assertEquals("", vm.durationString)
    }

    @Test
    fun `durationString includes only non-zero components`() {
        vm.durationDays = 0
        vm.durationHours = 0
        vm.durationMinutes = 15
        assertEquals("15m", vm.durationString)
    }

    @Test
    fun `validateValues returns false when ip is blank`() {
        assertFalse(vm.validateValues())
        assertTrue(vm.invalidFieldsAlert)
    }

    @Test
    fun `validateValues returns false when ip is invalid`() {
        vm.ipAddress = "not-an-ip"
        assertFalse(vm.validateValues())
        assertTrue(vm.invalidFieldsAlert)
    }

    @Test
    fun `validateValues returns false when reason is blank`() {
        vm.ipAddress = "192.168.1.1"
        assertFalse(vm.validateValues())
        assertEquals("Reason is required", vm.invalidFieldsAlertMessage)
    }

    @Test
    fun `reset clears all state`() {
        vm.ipAddress = "1.2.3.4"
        vm.durationDays = 2
        vm.reason = "test"
        vm.reset()
        assertEquals("", vm.ipAddress)
        assertEquals(0, vm.durationDays)
        assertEquals(4, vm.durationHours)
        assertEquals("", vm.reason)
        assertFalse(vm.invalidFieldsAlert)
        assertFalse(vm.errorCreatingDecisionAlert)
    }

    @Test
    fun `validateValues rejects invalid IPv4`() {
        vm.ipAddress = "999.999.999.999"
        assertFalse(vm.validateValues())
    }

    @Test
    fun `validateValues rejects range outside 0-255`() {
        vm.ipAddress = "192.168.256.1"
        assertFalse(vm.validateValues())
    }

    @Test
    fun `validateValues accepts valid IPv4 with reason`() {
        vm.ipAddress = "192.168.1.1"
        vm.reason = "test"
        assertTrue(vm.validateValues())
    }
}
