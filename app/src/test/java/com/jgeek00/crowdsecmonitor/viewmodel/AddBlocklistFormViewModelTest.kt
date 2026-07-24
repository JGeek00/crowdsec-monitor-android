package com.jgeek00.crowdsecmonitor.viewmodel

import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AddBlocklistFormViewModelTest {

    private lateinit var vm: AddBlocklistFormViewModel

    @Before
    fun setUp() {
        vm = AddBlocklistFormViewModel(mockk(relaxed = true))
    }

    @Test
    fun `initial state is empty`() {
        assertEquals("", vm.name)
        assertEquals("", vm.url)
        assertFalse(vm.isSaving)
        assertFalse(vm.requiredFieldsError)
        assertFalse(vm.invalidUrlError)
        assertFalse(vm.savingError)
    }

    @Test
    fun `reset clears all state`() {
        vm.name = "test"
        vm.url = "https://example.com"
        vm.reset()
        assertEquals("", vm.name)
        assertEquals("", vm.url)
        assertFalse(vm.requiredFieldsError)
    }
}
