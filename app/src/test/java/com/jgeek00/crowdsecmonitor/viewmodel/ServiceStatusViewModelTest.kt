package com.jgeek00.crowdsecmonitor.viewmodel

import io.mockk.mockk
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class ServiceStatusViewModelTest {

    private lateinit var vm: ServiceStatusViewModel

    @Before
    fun setUp() {
        val session = mockk<com.jgeek00.crowdsecmonitor.session.SessionManager>(relaxed = true)
        val repo = mockk<com.jgeek00.crowdsecmonitor.repository.ServiceStatusRepository>(relaxed = true)
        vm = ServiceStatusViewModel(session, repo)
    }

    @Test
    fun `viewModel can be created`() {
        assertNotNull(vm)
    }
}
