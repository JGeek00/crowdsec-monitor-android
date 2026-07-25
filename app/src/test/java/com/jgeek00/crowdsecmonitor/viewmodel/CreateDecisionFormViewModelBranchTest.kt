package com.jgeek00.crowdsecmonitor.viewmodel

import com.jgeek00.crowdsecmonitor.MainDispatcherRule
import com.jgeek00.crowdsecmonitor.constants.Enums
import com.jgeek00.crowdsecmonitor.data.api.CrowdSecApiClient
import com.jgeek00.crowdsecmonitor.data.api.DecisionsApiClient
import com.jgeek00.crowdsecmonitor.data.models.CreateDecisionRequest
import com.jgeek00.crowdsecmonitor.session.SessionManager
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class CreateDecisionFormViewModelBranchTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val sessionManager = mockk<SessionManager>(relaxed = true)
    private val apiClient = mockk<CrowdSecApiClient>(relaxed = true)
    private val decisionsClient = mockk<DecisionsApiClient>(relaxed = true)
    private lateinit var vm: CreateDecisionFormViewModel

    @Before
    fun setUp() {
        every { sessionManager.apiClient } returns apiClient
        every { apiClient.decisions } returns decisionsClient
        coEvery { decisionsClient.createDecision(any()) } returns mockk(relaxed = true)
        vm = CreateDecisionFormViewModel(sessionManager)
    }

    @Test
    fun `validateValues returns false when duration is zero`() {
        vm.ipAddress = "192.168.1.1"
        vm.reason = "test"
        vm.durationDays = 0
        vm.durationHours = 0
        vm.durationMinutes = 0
        assertFalse(vm.validateValues())
        assertEquals("Duration must be greater than 0", vm.invalidFieldsAlertMessage)
    }

    @Test
    fun `save returns early when validation fails`() {
        vm.save {}
        assertFalse(vm.errorCreatingDecisionAlert)
    }

    @Test
    fun `save returns early when apiClient is null`() {
        every { sessionManager.apiClient } returns null
        vm.ipAddress = "192.168.1.1"
        vm.reason = "test"
        vm.save {}
        // No crash without apiClient
    }

    @Test
    fun `save succeeds with valid data`() = runTest {
        var called = false
        vm.ipAddress = "192.168.1.1"
        vm.reason = "test"
        vm.save { called = true }
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        assertTrue(called)
        verify { sessionManager.triggerDecisionsRefresh() }
        verify { sessionManager.triggerAlertsRefresh() }
    }

    @Test
    fun `save handles creation error`() = runTest {
        coEvery { decisionsClient.createDecision(any()) } throws Exception("API error")
        vm.ipAddress = "192.168.1.1"
        vm.reason = "test"
        vm.save {}
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        assertTrue(vm.errorCreatingDecisionAlert)
    }

    @Test
    fun `validateValues accepts valid IPv4 with reason`() {
        vm.ipAddress = "192.168.1.1"
        vm.reason = "test"
        assertTrue(vm.validateValues())
    }

    @Test
    fun `reset clears error state`() {
        vm.errorCreatingDecisionAlert = true
        vm.invalidFieldsAlert = true
        vm.reset()
        assertFalse(vm.errorCreatingDecisionAlert)
        assertFalse(vm.invalidFieldsAlert)
    }

    @Test
    fun `save creates request with trimmed IP`() = runTest {
        var captured: CreateDecisionRequest? = null
        coEvery { decisionsClient.createDecision(any()) } answers {
            captured = firstArg()
            mockk(relaxed = true)
        }
        vm.ipAddress = "  192.168.1.1  "
        vm.reason = "test"
        vm.save {}
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        assertEquals("192.168.1.1", captured?.ip)
    }
}
