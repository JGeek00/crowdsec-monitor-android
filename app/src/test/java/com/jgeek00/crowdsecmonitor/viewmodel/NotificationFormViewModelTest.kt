package com.jgeek00.crowdsecmonitor.viewmodel

import com.jgeek00.crowdsecmonitor.MainDispatcherRule
import com.jgeek00.crowdsecmonitor.constants.NotificationDefaults
import com.jgeek00.crowdsecmonitor.data.api.AlertsApiClient
import com.jgeek00.crowdsecmonitor.data.api.CrowdSecApiClient
import com.jgeek00.crowdsecmonitor.data.api.NotificationChannelsApiClient
import com.jgeek00.crowdsecmonitor.data.api.NotificationsApiClient
import com.jgeek00.crowdsecmonitor.data.models.AlertsListResponse
import com.jgeek00.crowdsecmonitor.data.models.AlertsListResponseFiltering
import com.jgeek00.crowdsecmonitor.data.models.AlertsListResponsePagination
import com.jgeek00.crowdsecmonitor.data.models.CreateNotificationRequest
import com.jgeek00.crowdsecmonitor.data.models.NotificationChannelsListResponse
import com.jgeek00.crowdsecmonitor.data.models.NotificationConditionNode
import com.jgeek00.crowdsecmonitor.data.models.NotificationConditionValue
import com.jgeek00.crowdsecmonitor.data.models.NotificationDetailResponse
import com.jgeek00.crowdsecmonitor.data.models.NotificationThreshold
import com.jgeek00.crowdsecmonitor.data.models.UpdateNotificationRequest
import com.jgeek00.crowdsecmonitor.data.models.UserNotification
import com.jgeek00.crowdsecmonitor.data.models.UserNotificationChannel
import com.jgeek00.crowdsecmonitor.data.models.NotificationChannelType
import com.jgeek00.crowdsecmonitor.fixtures.TestFixtures
import com.jgeek00.crowdsecmonitor.session.SessionManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class NotificationFormViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val sessionManager = mockk<SessionManager>(relaxed = true)
    private val apiClient = mockk<CrowdSecApiClient>(relaxed = true)
    private val alertsClient = mockk<AlertsApiClient>(relaxed = true)
    private val notificationsClient = mockk<NotificationsApiClient>(relaxed = true)
    private val channelsClient = mockk<NotificationChannelsApiClient>(relaxed = true)

    @Before
    fun setUp() {
        every { sessionManager.apiClient } returns null
        every { apiClient.alerts } returns alertsClient
        every { apiClient.notifications } returns notificationsClient
        every { apiClient.notificationChannels } returns channelsClient
    }

    private fun makeVm(withApiClient: Boolean = false): NotificationFormViewModel {
        every { sessionManager.apiClient } returns if (withApiClient) apiClient else null
        return NotificationFormViewModel(sessionManager)
    }

    private fun notification(
        id: Int,
        condition: NotificationConditionNode,
        threshold: NotificationThreshold? = null,
        description: String? = null,
        channelIds: List<Int> = emptyList()
    ) = UserNotification(
        id = id,
        name = "n",
        description = description,
        enabled = true,
        condition = condition,
        threshold = threshold,
        message = "m",
        includeAlertInfo = false,
        channelIds = channelIds
    )

    private fun leafNode(field: String, op: String, value: NotificationConditionValue) =
        NotificationConditionNode.Leaf(field = field, operator = op, value = value)

    // MARK: - Step gates

    @Test
    fun `step zero requires name`() {
        val vm = makeVm()
        assertFalse(vm.canProceed(0))
        vm.name = "alerts"
        assertTrue(vm.canProceed(0))
    }

    @Test
    fun `step one requires valid condition`() {
        val vm = makeVm()
        assertFalse(vm.canProceed(1))
        assertEquals(RulesValidationIssue.EMPTY, vm.validationIssue(1))
        vm.rules = listOf(EditableLeaf.Empty)
        assertEquals(RulesValidationIssue.INCOMPLETE, vm.validationIssue(1))
        vm.rules = listOf(EditableLeaf(field = ConditionField.COUNTRY, op = ConditionOperator.EQUALS, values = listOf("ES")))
        assertTrue(vm.canProceed(1))
        assertNull(vm.validationIssue(1))
    }

    @Test
    fun `empty rules issue is empty`() {
        val vm = makeVm()
        vm.rules = emptyList()
        assertEquals(RulesValidationIssue.EMPTY, vm.validationIssue(1))
    }

    @Test
    fun `steps two and three gates`() {
        val vm = makeVm()
        assertFalse(vm.canProceed(2))
        vm.message = "hello"
        assertTrue(vm.canProceed(2))
        assertFalse(vm.canProceed(3))
        vm.selectedChannelIds = setOf(4)
        assertTrue(vm.canProceed(3))
    }

    // MARK: - OR merging / advanced detection (A4 deferred behavior)

    @Test
    fun `or same field merges into one rule`() {
        val vm = makeVm()
        vm.load(
            notification(
                1,
                condition = NotificationConditionNode.Or(
                    listOf(
                        leafNode("scenario", "equals", NotificationConditionValue.Single("a")),
                        leafNode("scenario", "equals", NotificationConditionValue.Single("b"))
                    )
                )
            )
        )
        assertNull(vm.advancedCondition)
        assertEquals(1, vm.rules.size)
        assertEquals(ConditionOperator.IN, vm.rules.first().op)
        assertEquals(listOf("a", "b"), vm.rules.first().values)
        assertTrue(vm.canProceed(1))
    }

    @Test
    fun `or mixed operators merges with in`() {
        val vm = makeVm()
        vm.load(
            notification(
                1,
                condition = NotificationConditionNode.Or(
                    listOf(
                        leafNode("scenario", "equals", NotificationConditionValue.Single("a")),
                        leafNode("scenario", "in", NotificationConditionValue.Multiple(listOf("b", "c")))
                    )
                )
            )
        )
        assertNull(vm.advancedCondition)
        assertEquals(1, vm.rules.size)
        assertEquals(ConditionOperator.IN, vm.rules.first().op)
        assertEquals(listOf("a", "b", "c"), vm.rules.first().values)
    }

    @Test
    fun `or dedups values preserving first occurrence order`() {
        val vm = makeVm()
        vm.load(
            notification(
                1,
                condition = NotificationConditionNode.Or(
                    listOf(
                        leafNode("scenario", "equals", NotificationConditionValue.Single("a")),
                        leafNode("scenario", "equals", NotificationConditionValue.Single("b")),
                        leafNode("scenario", "equals", NotificationConditionValue.Single("a"))
                    )
                )
            )
        )
        assertEquals(listOf("a", "b"), vm.rules.first().values)
    }

    @Test
    fun `cross field or is advanced`() {
        val vm = makeVm()
        vm.load(
            notification(
                1,
                condition = NotificationConditionNode.Or(
                    listOf(
                        leafNode("scenario", "equals", NotificationConditionValue.Single("a")),
                        leafNode("country", "equals", NotificationConditionValue.Single("ES"))
                    )
                )
            )
        )
        assertNotNull(vm.advancedCondition)
        assertFalse(vm.canProceed(1))
        assertEquals(RulesValidationIssue.ADVANCED, vm.validationIssue(1))
        vm.replaceWithSimpleRules()
        assertNull(vm.advancedCondition)
        assertTrue(vm.rules.isEmpty())
    }

    @Test
    fun `or with non merging operator is advanced`() {
        val vm = makeVm()
        vm.load(
            notification(
                1,
                condition = NotificationConditionNode.Or(
                    listOf(
                        leafNode("scenario", "equals", NotificationConditionValue.Single("a")),
                        leafNode("scenario", "contains", NotificationConditionValue.Single("b"))
                    )
                )
            )
        )
        assertNotNull(vm.advancedCondition)
        assertEquals(RulesValidationIssue.ADVANCED, vm.validationIssue(1))
    }

    @Test
    fun `or with unknown field is advanced`() {
        val vm = makeVm()
        vm.load(
            notification(
                1,
                condition = NotificationConditionNode.Or(
                    listOf(
                        leafNode("origin", "equals", NotificationConditionValue.Single("a")),
                        leafNode("scenario", "equals", NotificationConditionValue.Single("b"))
                    )
                )
            )
        )
        assertNotNull(vm.advancedCondition)
    }

    @Test
    fun `not condition is advanced`() {
        val vm = makeVm()
        vm.load(
            notification(
                1,
                condition = NotificationConditionNode.Not(
                    leafNode("scenario", "equals", NotificationConditionValue.Single("a"))
                )
            )
        )
        assertNotNull(vm.advancedCondition)
        assertFalse(vm.canProceed(1))
    }

    @Test
    fun `and of leaves loads as flat rules`() {
        val vm = makeVm()
        vm.load(
            notification(
                1,
                condition = NotificationConditionNode.And(
                    listOf(
                        leafNode("scenario", "equals", NotificationConditionValue.Single("a")),
                        leafNode("country", "equals", NotificationConditionValue.Single("ES"))
                    )
                )
            )
        )
        assertNull(vm.advancedCondition)
        assertEquals(2, vm.rules.size)
    }

    // MARK: - Defaults

    @Test
    fun `defaults are three in ten seconds`() {
        val vm = makeVm()
        assertEquals(3, vm.count)
        assertEquals("10", vm.windowSecondsText)
        assertEquals(NotificationDefaults.DEFAULT_COOLDOWN_SECONDS, vm.cooldownSeconds)
        assertTrue(vm.windowValid)
        assertFalse(vm.noCondition)
        assertTrue(vm.rules.isEmpty())
        assertEquals(RulesValidationIssue.EMPTY, vm.validationIssue(1))
        assertEquals(EditableCondition.And(emptyList()), vm.buildCondition())
    }

    // MARK: - Draft prefill

    @Test
    fun `new draft prefills first scenario`() {
        val vm = makeVm()
        vm.filterOptions = NotificationFilterOptions(scenarios = listOf("b", "a"))
        val draft = vm.newDraft()
        assertEquals(ConditionField.SCENARIO, draft.field)
        assertEquals(ConditionOperator.EQUALS, draft.op)
        assertEquals(listOf("b"), draft.values)
        assertTrue(draft.isValid)
    }

    @Test
    fun `new draft empty without options`() {
        val vm = makeVm()
        val draft = vm.newDraft()
        assertTrue(draft.values.isEmpty())
        assertFalse(draft.isValid)
    }

    // MARK: - Window boundaries

    @Test
    fun `window boundaries`() {
        val vm = makeVm()
        vm.rules = listOf(EditableLeaf(field = ConditionField.SCENARIO, op = ConditionOperator.EQUALS, values = listOf("x")))
        vm.windowSecondsText = "9"
        assertFalse(vm.windowValid)
        assertEquals(RulesValidationIssue.INVALID_WINDOW, vm.validationIssue(1))
        assertFalse(vm.canProceed(1))
        vm.windowSecondsText = "10"
        assertTrue(vm.canProceed(1))
        vm.windowSecondsText = "86400"
        assertTrue(vm.canProceed(1))
        vm.windowSecondsText = "86401"
        assertFalse(vm.canProceed(1))
        vm.windowSecondsText = "abc"
        assertFalse(vm.canProceed(1))
    }

    // MARK: - No condition

    @Test
    fun `no condition prefill and build`() {
        val vm = makeVm()
        vm.load(
            notification(
                2,
                condition = NotificationConditionNode.And(emptyList()),
                threshold = NotificationThreshold(count = 3, windowSeconds = 10)
            )
        )
        assertTrue(vm.noCondition)
        assertNull(vm.advancedCondition)
        assertTrue(vm.canProceed(1))
        assertEquals(EditableCondition.And(emptyList()), vm.buildCondition())
    }

    @Test
    fun `no condition skips rules validation`() {
        val vm = makeVm()
        vm.load(notification(2, condition = NotificationConditionNode.And(emptyList())))
        assertTrue(vm.canProceed(1))
        assertNull(vm.validationIssue(1))
    }

    // MARK: - Condition building

    @Test
    fun `build condition always and`() {
        val vm = makeVm()
        vm.rules = listOf(
            EditableLeaf(field = ConditionField.SCENARIO, op = ConditionOperator.EQUALS, values = listOf("a")),
            EditableLeaf(field = ConditionField.COUNTRY, op = ConditionOperator.EQUALS, values = listOf("ES"))
        )
        val built = vm.buildCondition()
        assertTrue(built is EditableCondition.And)
        assertEquals(2, (built as EditableCondition.And).children.size)
    }

    // MARK: - Edit prefill

    @Test
    fun `edit prefills from notification`() {
        val vm = makeVm()
        vm.load(
            notification(
                9,
                condition = NotificationConditionNode.Or(
                    listOf(leafNode("country", "equals", NotificationConditionValue.Single("ES")))
                ),
                threshold = NotificationThreshold(count = 4, windowSeconds = 60, cooldownSeconds = 300),
                description = "d",
                channelIds = listOf(3)
            )
        )
        assertTrue(vm.isEditing)
        assertEquals("n", vm.name)
        assertEquals("d", vm.notificationDescription)
        assertEquals(1, vm.rules.size)
        assertEquals(ConditionField.COUNTRY, vm.rules.first().field)
        assertEquals(4, vm.count)
        assertEquals("60", vm.windowSecondsText)
        assertEquals(300, vm.cooldownSeconds)
        assertTrue(vm.windowValid)
        assertEquals(setOf(3), vm.selectedChannelIds)
        assertTrue(vm.canProceed(0))
        assertTrue(vm.canProceed(1))
    }

    @Test
    fun `edit without cooldown falls back to default`() {
        val vm = makeVm()
        vm.load(
            notification(
                10,
                condition = NotificationConditionNode.And(emptyList()),
                threshold = NotificationThreshold(count = 2, windowSeconds = 30)
            )
        )
        assertEquals(NotificationDefaults.DEFAULT_COOLDOWN_SECONDS, vm.cooldownSeconds)
        assertFalse(vm.isEditing && vm.name.isEmpty())
        assertEquals("n", vm.name)
    }

    // MARK: - loadOptions

    @Test
    fun `load options populates filter options and channels`() = runTest {
        val vm = makeVm(withApiClient = true)
        coEvery { alertsClient.fetchAlerts(any()) } returns TestFixtures.successResponse(
            AlertsListResponse(
                filtering = AlertsListResponseFiltering(
                    countries = listOf("ES", "AR"),
                    scenarios = listOf("ssh-bf", "http-cve"),
                    ipOwners = listOf("acme"),
                    targets = listOf("1.2.3.4")
                ),
                items = emptyList(),
                pagination = AlertsListResponsePagination(page = 1, amount = 1, total = 0)
            )
        )
        coEvery { channelsClient.fetchChannels() } returns TestFixtures.successResponse(
            NotificationChannelsListResponse(
                data = listOf(
                    UserNotificationChannel(
                        id = 1,
                        name = "ch",
                        type = NotificationChannelType.NTFY,
                        supportsAlertInfo = true
                    )
                )
            )
        )

        vm.loadOptions()
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        assertEquals(listOf("http-cve", "ssh-bf"), vm.filterOptions.scenarios)
        assertEquals(listOf("AR", "ES"), vm.filterOptions.countries)
        assertEquals(listOf("1.2.3.4"), vm.filterOptions.targets)
        assertEquals(listOf("acme"), vm.filterOptions.ipOwners)
        assertEquals(1, vm.channels.size)
        assertFalse(vm.loadError)
        coVerify { alertsClient.fetchAlerts(any()) }
    }

    @Test
    fun `load options tolerates failure`() = runTest {
        val vm = makeVm(withApiClient = true)
        coEvery { alertsClient.fetchAlerts(any()) } throws Exception("test error")

        vm.loadOptions()
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        assertTrue(vm.loadError)
        assertEquals(NotificationFilterOptions(), vm.filterOptions)
    }

    @Test
    fun `load options without client is a no-op`() = runTest {
        val vm = makeVm()
        vm.loadOptions()
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        assertFalse(vm.loadError)
        assertEquals(NotificationFilterOptions(), vm.filterOptions)
    }

    // MARK: - Rule sheet staging

    @Test
    fun `open rule sheet presents immediately without keyboard`() {
        val vm = makeVm()
        val draft = EditableLeaf(field = ConditionField.SCENARIO, op = ConditionOperator.EQUALS, values = listOf("a"))
        vm.openRuleSheet(draft, null)
        assertNotNull(vm.ruleSheet)
        assertEquals(draft, vm.ruleSheet?.draft)
        assertNull(vm.ruleSheet?.index)
    }

    @Test
    fun `open rule sheet stages while keyboard visible then presents after hide`() = runTest {
        val vm = makeVm()
        val draft = EditableLeaf(field = ConditionField.SCENARIO, op = ConditionOperator.EQUALS, values = listOf("a"))
        vm.keyboardWillShow()
        vm.openRuleSheet(draft, 0)
        assertNull(vm.ruleSheet)

        vm.keyboardDidHide()
        // Still staged during the settle delay.
        assertNull(vm.ruleSheet)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        assertNotNull(vm.ruleSheet)
        assertEquals(draft, vm.ruleSheet?.draft)
        assertEquals(0, vm.ruleSheet?.index)
        assertFalse(vm.isKeyboardVisible)
    }

    @Test
    fun `commit rule sheet appends new rule`() {
        val vm = makeVm()
        val draft = EditableLeaf(field = ConditionField.SCENARIO, op = ConditionOperator.EQUALS, values = listOf("a"))
        vm.openRuleSheet(draft, null)
        vm.commitRuleSheet()
        assertNull(vm.ruleSheet)
        assertEquals(listOf(draft), vm.rules)
    }

    @Test
    fun `commit rule sheet replaces existing rule`() {
        val vm = makeVm()
        val original = EditableLeaf(field = ConditionField.SCENARIO, op = ConditionOperator.EQUALS, values = listOf("a"))
        val updated = EditableLeaf(field = ConditionField.COUNTRY, op = ConditionOperator.EQUALS, values = listOf("ES"))
        vm.rules = listOf(original)
        vm.openRuleSheet(updated, 0)
        vm.commitRuleSheet()
        assertEquals(listOf(updated), vm.rules)
    }

    @Test
    fun `commit rule sheet is no-op when dismissed`() {
        val vm = makeVm()
        vm.commitRuleSheet()
        assertTrue(vm.rules.isEmpty())
    }

    @Test
    fun `dismiss rule sheet discards draft`() {
        val vm = makeVm()
        vm.openRuleSheet(EditableLeaf.Empty, null)
        vm.dismissRuleSheet()
        assertNull(vm.ruleSheet)
        assertTrue(vm.rules.isEmpty())
    }

    // MARK: - Save

    @Test
    fun `save create sends enabled true and threshold`() = runTest {
        val vm = makeVm(withApiClient = true)
        vm.name = "n"
        vm.rules = listOf(EditableLeaf(field = ConditionField.SCENARIO, op = ConditionOperator.EQUALS, values = listOf("x")))
        vm.message = "m"
        vm.selectedChannelIds = setOf(1)
        val saved = notification(1, condition = leafNode("scenario", "equals", NotificationConditionValue.Single("x")), channelIds = listOf(1))
        val slot = slot<CreateNotificationRequest>()
        coEvery { notificationsClient.createNotification(capture(slot)) } returns
            TestFixtures.successResponse(NotificationDetailResponse(data = saved))

        val result = vm.save()

        assertNotNull(result)
        assertFalse(vm.saveError)
        assertFalse(vm.isSaving)
        val body = slot.captured
        assertTrue(body.enabled == true)
        assertEquals(3, body.threshold?.count)
        assertEquals(10, body.threshold?.windowSeconds)
        assertEquals(NotificationDefaults.DEFAULT_COOLDOWN_SECONDS, body.threshold?.cooldownSeconds)
        assertEquals("n", body.name)
        assertNull(body.description)
        val condition = body.condition as NotificationConditionNode.And
        val leaf = condition.children.first() as NotificationConditionNode.Leaf
        assertEquals("scenario", leaf.field)
        assertEquals("equals", leaf.operator)
        assertEquals(NotificationConditionValue.Single("x"), leaf.value)
    }

    @Test
    fun `save sends picked cooldown`() = runTest {
        val vm = makeVm(withApiClient = true)
        vm.name = "n"
        vm.rules = listOf(EditableLeaf(field = ConditionField.SCENARIO, op = ConditionOperator.EQUALS, values = listOf("x")))
        vm.cooldownSeconds = 120
        vm.message = "m"
        vm.selectedChannelIds = setOf(1)
        val slot = slot<CreateNotificationRequest>()
        coEvery { notificationsClient.createNotification(capture(slot)) } returns
            TestFixtures.successResponse(
                NotificationDetailResponse(
                    data = notification(1, condition = leafNode("scenario", "equals", NotificationConditionValue.Single("x")))
                )
            )

        val result = vm.save()

        assertNotNull(result)
        assertEquals(120, slot.captured.threshold?.cooldownSeconds)
    }

    @Test
    fun `save update omits enabled and trims description`() = runTest {
        val vm = makeVm(withApiClient = true)
        vm.load(
            notification(
                9,
                condition = NotificationConditionNode.Or(
                    listOf(leafNode("country", "equals", NotificationConditionValue.Single("ES")))
                ),
                threshold = NotificationThreshold(count = 4, windowSeconds = 60, cooldownSeconds = 300),
                description = "d",
                channelIds = listOf(3)
            )
        )
        vm.notificationDescription = "  hello  "
        val slot = slot<UpdateNotificationRequest>()
        coEvery { notificationsClient.updateNotification(9, capture(slot)) } returns
            TestFixtures.successResponse(
                NotificationDetailResponse(
                    data = notification(9, condition = leafNode("scenario", "equals", NotificationConditionValue.Single("x")))
                )
            )

        val result = vm.save()

        assertNotNull(result)
        assertFalse(vm.saveError)
        val body = slot.captured
        assertNull(body.enabled)
        assertEquals("hello", body.description)
        assertEquals("n", body.name)
        assertEquals(300, body.threshold?.cooldownSeconds)
        coVerify(exactly = 1) { notificationsClient.updateNotification(9, any()) }
        coVerify(exactly = 0) { notificationsClient.createNotification(any()) }
    }

    @Test
    fun `save create parses window text`() = runTest {
        val vm = makeVm(withApiClient = true)
        vm.name = "n"
        vm.rules = listOf(EditableLeaf(field = ConditionField.SCENARIO, op = ConditionOperator.EQUALS, values = listOf("x")))
        vm.windowSecondsText = " 45 "
        vm.message = "m"
        vm.selectedChannelIds = setOf(1)
        val slot = slot<CreateNotificationRequest>()
        coEvery { notificationsClient.createNotification(capture(slot)) } returns
            TestFixtures.successResponse(
                NotificationDetailResponse(
                    data = notification(1, condition = leafNode("scenario", "equals", NotificationConditionValue.Single("x")))
                )
            )

        vm.save()

        assertEquals(45, slot.captured.threshold?.windowSeconds)
    }

    @Test
    fun `save error flag`() = runTest {
        val vm = makeVm(withApiClient = true)
        vm.name = "n"
        vm.rules = listOf(EditableLeaf(field = ConditionField.SCENARIO, op = ConditionOperator.EQUALS, values = listOf("x")))
        vm.message = "m"
        vm.selectedChannelIds = setOf(1)
        coEvery { notificationsClient.createNotification(any()) } throws Exception("test error")

        val result = vm.save()

        assertNull(result)
        assertTrue(vm.saveError)
        assertFalse(vm.isSaving)
    }

    @Test
    fun `save does not surface cancellation`() = runTest {
        val vm = makeVm(withApiClient = true)
        vm.name = "n"
        vm.rules = listOf(EditableLeaf(field = ConditionField.SCENARIO, op = ConditionOperator.EQUALS, values = listOf("x")))
        vm.message = "m"
        vm.selectedChannelIds = setOf(1)
        coEvery { notificationsClient.createNotification(any()) } throws CancellationException("cancelled")

        try {
            vm.save()
            org.junit.Assert.fail("CancellationException should propagate")
        } catch (_: CancellationException) {
        }
        assertFalse(vm.saveError)
    }

    @Test
    fun `save without client returns null`() = runTest {
        val vm = makeVm()
        vm.name = "n"
        vm.message = "m"
        assertNull(vm.save())
        assertFalse(vm.saveError)
        assertFalse(vm.isSaving)
    }
}
