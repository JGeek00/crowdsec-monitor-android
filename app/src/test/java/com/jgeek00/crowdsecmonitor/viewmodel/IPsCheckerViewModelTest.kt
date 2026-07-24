package com.jgeek00.crowdsecmonitor.viewmodel

import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)

class IPsCheckerViewModelTest {

    private lateinit var vm: IPsCheckerViewModel

    @Before
    fun setUp() {
        vm = IPsCheckerViewModel(mockk(relaxed = true))
    }

    @Test
    fun `initial state is empty`() {
        assertTrue(vm.ipsToCheck.isEmpty())
        assertFalse(vm.blocklistsLoading)
        assertFalse(vm.blocklistsError)
    }

    @Test
    fun `addEntry adds an IPField`() {
        vm.addEntry()
        assertEquals(1, vm.ipsToCheck.size)
        assertEquals("", vm.ipsToCheck[0].value)
        assertFalse(vm.ipsToCheck[0].invalid)
    }

    @Test
    fun `addEntry adds multiple entries`() {
        vm.addEntry()
        vm.addEntry()
        assertEquals(2, vm.ipsToCheck.size)
    }

    @Test
    fun `removeEntry removes at index`() {
        vm.addEntry()
        vm.addEntry()
        vm.removeEntry(0)
        assertEquals(1, vm.ipsToCheck.size)
    }

    @Test
    fun `updateEntry updates value`() {
        vm.addEntry()
        vm.updateEntry(0, "1.2.3.4")
        assertEquals("1.2.3.4", vm.ipsToCheck[0].value)
    }

    @Test
    fun `reset clears all state`() {
        vm.addEntry()
        vm.addEntry()
        vm.selectedListType = com.jgeek00.crowdsecmonitor.constants.Enums.ListType.ALLOWLIST
        vm.reset()
        assertTrue(vm.ipsToCheck.isEmpty())
        assertFalse(vm.blocklistsLoading)
        assertFalse(vm.blocklistsError)
        assertFalse(vm.allowlistsLoading)
        assertFalse(vm.allowlistsError)
    }
}
