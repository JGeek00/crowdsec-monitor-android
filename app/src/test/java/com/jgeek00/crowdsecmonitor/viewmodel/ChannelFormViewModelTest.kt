package com.jgeek00.crowdsecmonitor.viewmodel

import com.jgeek00.crowdsecmonitor.MainDispatcherRule
import com.jgeek00.crowdsecmonitor.data.api.CrowdSecApiClient
import com.jgeek00.crowdsecmonitor.data.api.NotificationChannelsApiClient
import com.jgeek00.crowdsecmonitor.data.models.ChannelTestResponse
import com.jgeek00.crowdsecmonitor.data.models.ChannelTestResult
import com.jgeek00.crowdsecmonitor.data.models.CreateChannelRequest
import com.jgeek00.crowdsecmonitor.data.models.NotificationChannelDetailResponse
import com.jgeek00.crowdsecmonitor.data.models.NotificationChannelType
import com.jgeek00.crowdsecmonitor.data.models.NotificationProvider
import com.jgeek00.crowdsecmonitor.data.models.ProviderField
import com.jgeek00.crowdsecmonitor.data.models.ProviderFieldCondition
import com.jgeek00.crowdsecmonitor.data.models.ProviderFieldOption
import com.jgeek00.crowdsecmonitor.data.models.ProviderSection
import com.jgeek00.crowdsecmonitor.data.models.ProvidersListResponse
import com.jgeek00.crowdsecmonitor.data.models.UserNotificationChannel
import com.jgeek00.crowdsecmonitor.fixtures.TestFixtures
import com.jgeek00.crowdsecmonitor.session.SessionManager
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ChannelFormViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val sessionManager = mockk<SessionManager>(relaxed = true)
    private val apiClient = mockk<CrowdSecApiClient>(relaxed = true)
    private val channelsClient = mockk<NotificationChannelsApiClient>(relaxed = true)

    @Before
    fun setUp() {
        every { sessionManager.apiClient } returns null
        every { apiClient.notificationChannels } returns channelsClient
    }

    private fun makeVm(
        withApiClient: Boolean = false,
        editing: UserNotificationChannel? = null
    ): ChannelFormViewModel {
        every { sessionManager.apiClient } returns if (withApiClient) apiClient else null
        val vm = ChannelFormViewModel(sessionManager)
        if (editing != null) {
            vm.load(editing)
        }
        vm.providers = listOf(ntfyProvider(), emailProvider())
        return vm
    }

    // MARK: - Fixtures (provider fixtures ALWAYS include options on selects — gotcha 15)

    private fun field(
        key: String,
        type: String,
        section: String? = null,
        required: Boolean? = null,
        secret: Boolean? = null,
        defaultValue: JsonPrimitive? = null,
        regex: String? = null,
        integer: Boolean? = null,
        min: Double? = null,
        max: Double? = null,
        options: List<String>? = null,
        requiredIf: ProviderFieldCondition? = null,
        exclusiveWith: List<String>? = null,
        visibleIf: ProviderFieldCondition? = null
    ) = ProviderField(
        key = key,
        section = section,
        labelKey = "field_$key",
        type = type,
        required = required,
        secret = secret,
        defaultValue = defaultValue,
        regex = regex,
        min = min,
        max = max,
        integer = integer,
        options = options?.map { ProviderFieldOption(value = it, labelKey = "opt_$it") },
        requiredIf = requiredIf,
        exclusiveWith = exclusiveWith,
        visibleIf = visibleIf
    )

    private fun ntfyProvider() = NotificationProvider(
        type = "ntfy",
        descriptionKey = null,
        icon = "ntfy",
        labelKey = "provider_ntfy",
        supportsTest = true,
        supportsAlertInfo = false,
        sections = emptyList(),
        fields = listOf(
            field("topic", type = "text", required = true, regex = "^[-_A-Za-z0-9]{1,64}$"),
            field("server", type = "url", defaultValue = JsonPrimitive("https://ntfy.sh")),
            field("username", type = "text"),
            field(
                "password", type = "password", secret = true,
                requiredIf = ProviderFieldCondition(field = "username", present = true),
                visibleIf = ProviderFieldCondition(field = "username", present = true)
            ),
            field(
                "accessToken", type = "password", secret = true,
                exclusiveWith = listOf("username", "password")
            ),
            field(
                "priority", type = "select", defaultValue = JsonPrimitive("default"),
                options = listOf("min", "low", "default", "high", "urgent", "max")
            )
        )
    )

    private fun emailProvider() = NotificationProvider(
        type = "email",
        descriptionKey = null,
        icon = "email",
        labelKey = "provider_email",
        supportsTest = true,
        supportsAlertInfo = false,
        sections = emptyList(),
        fields = listOf(
            field("host", type = "text", required = true),
            field("port", type = "number", defaultValue = JsonPrimitive(587), integer = true, min = 1.0, max = 65535.0),
            field("secure", type = "boolean", defaultValue = JsonPrimitive(false)),
            field("from", type = "email", required = true),
            field("to", type = "email", required = true)
        )
    )

    private fun makeProvider(
        fields: List<ProviderField>,
        sections: List<ProviderSection> = emptyList()
    ) = NotificationProvider(
        type = "t",
        descriptionKey = null,
        icon = "t",
        labelKey = "p",
        supportsTest = true,
        supportsAlertInfo = false,
        sections = sections,
        fields = fields
    )

    private fun sectionedField(key: String, section: String?) = ProviderField(
        key = key,
        section = section,
        labelKey = "l_$key",
        type = "text"
    )

    private fun makeChannel() = UserNotificationChannel(
        id = 5,
        name = "phone",
        type = NotificationChannelType.NTFY,
        supportsAlertInfo = false,
        config = mapOf("topic" to JsonPrimitive("alerts"))
    )

    private fun ChannelFormViewModel.set(key: String, value: kotlinx.serialization.json.JsonElement) {
        values = values + (key to value)
    }

    // MARK: - Grouping

    @Test
    fun `grouped fields follow definition order with trailing ungrouped group`() {
        val vm = makeVm()
        vm.providers = listOf(
            makeProvider(
                fields = listOf(
                    sectionedField("ungrouped", section = null),
                    sectionedField("b1", section = "s2"),
                    sectionedField("a1", section = "s1"),
                    sectionedField("weird", section = "nope")
                ),
                sections = listOf(
                    ProviderSection(key = "s1", labelKey = "ls1"),
                    ProviderSection(key = "s2", labelKey = "ls2")
                )
            )
        )
        vm.providerType = "t"
        val groups = vm.groupedFields()
        assertEquals(listOf("s1", "s2", null), groups.map { it.first?.key })
        assertEquals(listOf("a1"), groups[0].second.map { it.key })
        assertEquals(listOf("b1"), groups[1].second.map { it.key })
        assertEquals(listOf("ungrouped", "weird"), groups[2].second.map { it.key })
    }

    // MARK: - visibleIf

    @Test
    fun `visible fields respect visibleIf`() {
        val vm = makeVm()
        vm.providerType = "ntfy"
        assertFalse(vm.visibleFields().map { it.key }.contains("password"))
        vm.set("username", JsonPrimitive("u"))
        assertTrue(vm.visibleFields().map { it.key }.contains("password"))
    }

    // MARK: - Validation

    @Test
    fun `ntfy validation with requiredIf and exclusiveWith`() {
        val vm = makeVm()
        vm.providerType = "ntfy"
        assertFalse(vm.providerValid())
        vm.set("topic", JsonPrimitive("bad topic!"))
        assertFalse(vm.providerValid())
        vm.set("topic", JsonPrimitive("good-topic"))
        assertTrue(vm.providerValid())
        vm.set("username", JsonPrimitive("u"))
        assertFalse(vm.providerValid())
        vm.set("password", JsonPrimitive("p"))
        assertTrue(vm.providerValid())
        vm.set("accessToken", JsonPrimitive("t"))
        assertFalse(vm.providerValid())
    }

    @Test
    fun `email validation and number coercion`() {
        val vm = makeVm()
        vm.providerType = "email"
        assertFalse(vm.providerValid())
        vm.set("host", JsonPrimitive("smtp.example.com"))
        vm.set("from", JsonPrimitive("nope"))
        vm.set("to", JsonPrimitive("d@e.f"))
        assertFalse(vm.providerValid())
        vm.set("from", JsonPrimitive("a@b.c"))
        assertTrue(vm.providerValid())
        vm.set("port", JsonPrimitive("not-a-number"))
        assertFalse(vm.providerValid())
        vm.set("port", JsonPrimitive("465"))
        assertTrue(vm.providerValid())
        val payload = vm.buildPayload()
        assertEquals(JsonPrimitive(465), payload?.get("port"))
        assertNull(payload?.get("username"))
    }

    // MARK: - Defaults on provider select (gotcha 11)

    @Test
    fun `select provider applies defaults`() {
        val vm = makeVm()
        vm.providers = listOf(ntfyProvider())
        vm.selectProvider("ntfy")
        assertEquals(JsonPrimitive("default"), vm.values["priority"])
        assertFalse(vm.providerValid())
        vm.set("topic", JsonPrimitive("t"))
        assertTrue(vm.providerValid())
    }

    @Test
    fun `number accepts int values`() {
        val vm = makeVm()
        vm.providers = listOf(emailProvider())
        vm.providerType = "email"
        vm.set("host", JsonPrimitive("h"))
        vm.set("from", JsonPrimitive("a@b.c"))
        vm.set("to", JsonPrimitive("d@e.f"))
        vm.set("port", JsonPrimitive(465))
        assertTrue(vm.providerValid())
        assertEquals(JsonPrimitive(465), vm.buildPayload()?.get("port"))
    }

    // MARK: - Edit prefill

    @Test
    fun `edit prefills from channel`() {
        val vm = makeVm(editing = makeChannel())
        assertTrue(vm.isEditing)
        assertEquals("ntfy", vm.providerType)
        assertEquals("phone", vm.name)
        assertEquals(JsonPrimitive("alerts"), vm.values["topic"])
        assertTrue(vm.providerValid())
    }

    // MARK: - loadProviders

    @Test
    fun `load providers fetches and applies defaults`() = runTest {
        val vm = makeVm(withApiClient = true)
        vm.providers = emptyList()
        coEvery { channelsClient.fetchProviders() } returns TestFixtures.successResponse(
            ProvidersListResponse(version = 1, providers = listOf(ntfyProvider()))
        )
        vm.loadProviders()
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        assertEquals(1, vm.providers.size)
        assertFalse(vm.loadError)
    }

    @Test
    fun `load providers failure is non-fatal`() = runTest {
        val vm = makeVm(withApiClient = true)
        coEvery { channelsClient.fetchProviders() } throws Exception("providers error")
        vm.loadProviders()
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        assertTrue(vm.loadError)
    }

    // MARK: - Save

    @Test
    fun `save create posts full config`() = runTest {
        val vm = makeVm(withApiClient = true)
        vm.providerType = "email"
        vm.name = "ops"
        vm.set("host", JsonPrimitive("h"))
        vm.set("from", JsonPrimitive("a@b.c"))
        vm.set("to", JsonPrimitive("d@e.f"))
        val slot = slot<CreateChannelRequest>()
        coEvery { channelsClient.createChannel(capture(slot)) } returns TestFixtures.successResponse(
            NotificationChannelDetailResponse(data = makeChannel())
        )
        val saved = vm.save()
        assertNotNull(saved)
        val body = slot.captured
        assertEquals("email", body.type)
        assertEquals("ops", body.name)
        assertEquals(JsonPrimitive("h"), body.config["host"])
        assertEquals(JsonPrimitive("a@b.c"), body.config["from"])
        assertEquals(JsonPrimitive("d@e.f"), body.config["to"])
        assertFalse(body.config.containsKey("username"))
    }

    // MARK: - Inline test

    @Test
    fun `inline test success`() = runTest {
        val vm = makeVm(withApiClient = true)
        vm.providerType = "ntfy"
        vm.name = "phone"
        vm.set("topic", JsonPrimitive("t"))
        coEvery { channelsClient.testInlineChannel(any()) } returns TestFixtures.successResponse(
            ChannelTestResponse(data = ChannelTestResult(channelId = null, ok = true, detail = null))
        )
        vm.test()
        assertEquals(ChannelTestState.Success, vm.testState)
    }

    @Test
    fun `inline test failure maps detail`() = runTest {
        val vm = makeVm(withApiClient = true)
        vm.providerType = "ntfy"
        vm.name = "phone"
        vm.set("topic", JsonPrimitive("t"))
        coEvery { channelsClient.testInlineChannel(any()) } returns TestFixtures.successResponse(
            ChannelTestResponse(data = ChannelTestResult(channelId = null, ok = false, detail = "boom"))
        )
        vm.test()
        assertEquals(ChannelTestState.Failure("boom"), vm.testState)
    }
}
