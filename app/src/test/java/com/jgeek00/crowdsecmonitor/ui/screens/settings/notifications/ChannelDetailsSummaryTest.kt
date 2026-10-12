package com.jgeek00.crowdsecmonitor.ui.screens.settings.notifications

import com.jgeek00.crowdsecmonitor.data.models.NotificationChannelType
import com.jgeek00.crowdsecmonitor.data.models.NotificationProvider
import com.jgeek00.crowdsecmonitor.data.models.ProviderField
import com.jgeek00.crowdsecmonitor.data.models.UserNotificationChannel
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pure-logic tests for the channel details summary rows: secret hiding
 * (gotcha 13), value fallback to field defaults, and the raw-config-keys
 * fallback when the provider definition is unavailable.
 */
class ChannelDetailsSummaryTest {

    private val yesLabel = "Yes"
    private val noLabel = "No"

    private fun field(
        key: String,
        type: String = "text",
        secret: Boolean? = null,
        defaultValue: JsonPrimitive? = null
    ) = ProviderField(
        key = key,
        labelKey = "field_${key}",
        type = type,
        secret = secret,
        defaultValue = defaultValue
    )

    private fun provider(vararg fields: ProviderField) = NotificationProvider(
        type = "ntfy",
        icon = "ntfy",
        labelKey = "provider_ntfy",
        supportsTest = true,
        supportsAlertInfo = true,
        fields = fields.toList()
    )

    private fun channel(config: Map<String, JsonElement>) = UserNotificationChannel(
        id = 1,
        name = "My channel",
        type = NotificationChannelType.NTFY,
        supportsAlertInfo = true,
        config = config
    )

    private fun labelsFor(definition: NotificationProvider?): Map<String, String> =
        definition?.fields?.associate { it.key to ("label:" + it.key) } ?: emptyMap()

    // MARK: - Secret keys

    @Test
    fun `secret keys come from definition fields flagged secret`() {
        val definition = provider(
            field("topic"),
            field("password", secret = true),
            field("accessToken", type = "password", secret = true)
        )
        assertEquals(
            setOf("password", "accessToken"),
            ChannelDetailsSummary.secretKeys(definition)
        )
    }

    @Test
    fun `secret keys fall back to well known keys without definition`() {
        assertEquals(
            ChannelDetailsSummary.FALLBACK_SECRET_KEYS,
            ChannelDetailsSummary.secretKeys(null)
        )
        assertEquals(
            setOf("password", "accessToken"),
            ChannelDetailsSummary.FALLBACK_SECRET_KEYS
        )
    }

    // MARK: - Rows from definition

    @Test
    fun `definition rows hide secrets and use config values`() {
        val definition = provider(
            field("topic"),
            field("password", secret = true)
        )
        val rows = ChannelDetailsSummary.buildRows(
            channel = channel(config = mapOf("topic" to JsonPrimitive("my-topic"))),
            definition = definition,
            definitionLabels = labelsFor(definition),
            yesLabel = yesLabel,
            noLabel = noLabel
        )
        assertEquals(listOf(ChannelDetailsSummary.Row("label:topic", "my-topic")), rows)
    }

    @Test
    fun `definition rows fall back to field defaults`() {
        val definition = provider(
            field("topic"),
            field("port", type = "number", defaultValue = JsonPrimitive(25))
        )
        val rows = ChannelDetailsSummary.buildRows(
            channel = channel(config = mapOf("topic" to JsonPrimitive("my-topic"))),
            definition = definition,
            definitionLabels = labelsFor(definition),
            yesLabel = yesLabel,
            noLabel = noLabel
        )
        assertEquals(
            listOf(
                ChannelDetailsSummary.Row("label:topic", "my-topic"),
                ChannelDetailsSummary.Row("label:port", "25")
            ),
            rows
        )
    }

    @Test
    fun `definition rows skip absent values`() {
        val definition = provider(field("topic"), field("host"))
        val rows = ChannelDetailsSummary.buildRows(
            channel = channel(config = emptyMap()),
            definition = definition,
            definitionLabels = labelsFor(definition),
            yesLabel = yesLabel,
            noLabel = noLabel
        )
        assertEquals(emptyList<ChannelDetailsSummary.Row>(), rows)
    }

    @Test
    fun `definition rows render booleans with localized labels`() {
        val definition = provider(field("secure", type = "boolean"))
        val rows = ChannelDetailsSummary.buildRows(
            channel = channel(config = mapOf("secure" to JsonPrimitive(true))),
            definition = definition,
            definitionLabels = labelsFor(definition),
            yesLabel = yesLabel,
            noLabel = noLabel
        )
        assertEquals(listOf(ChannelDetailsSummary.Row("label:secure", "Yes")), rows)
    }

    // MARK: - Fallback rows (definition unavailable)

    @Test
    fun `fallback rows use raw sorted config keys and hide known secrets`() {
        val rows = ChannelDetailsSummary.buildRows(
            channel = channel(
                config = mapOf(
                    "topic" to JsonPrimitive("my-topic"),
                    "password" to JsonPrimitive("hunter2"),
                    "accessToken" to JsonPrimitive("token-value"),
                    "host" to JsonPrimitive("https://example.com")
                )
            ),
            definition = null,
            definitionLabels = emptyMap(),
            yesLabel = yesLabel,
            noLabel = noLabel
        )
        // Gotcha 13: password/accessToken are never displayed, even in the
        // fallback path.
        assertEquals(
            listOf(
                ChannelDetailsSummary.Row("host", "https://example.com"),
                ChannelDetailsSummary.Row("topic", "my-topic")
            ),
            rows
        )
    }

    // MARK: - displayText / plainText

    @Test
    fun `displayText prefers config value then default`() {
        assertEquals(
            "from-config",
            ChannelDetailsSummary.displayText(
                configValue = JsonPrimitive("from-config"),
                defaultValue = JsonPrimitive("from-default"),
                yesLabel = yesLabel,
                noLabel = noLabel
            )
        )
        assertEquals(
            "from-default",
            ChannelDetailsSummary.displayText(
                configValue = null,
                defaultValue = JsonPrimitive("from-default"),
                yesLabel = yesLabel,
                noLabel = noLabel
            )
        )
        assertNull(
            ChannelDetailsSummary.displayText(
                configValue = null,
                defaultValue = null,
                yesLabel = yesLabel,
                noLabel = noLabel
            )
        )
    }

    @Test
    fun `displayText treats JSON null as absent`() {
        assertEquals(
            "from-default",
            ChannelDetailsSummary.displayText(
                configValue = JsonNull,
                defaultValue = JsonPrimitive("from-default"),
                yesLabel = yesLabel,
                noLabel = noLabel
            )
        )
        assertNull(
            ChannelDetailsSummary.displayText(
                configValue = JsonNull,
                defaultValue = JsonNull,
                yesLabel = yesLabel,
                noLabel = noLabel
            )
        )
    }

    @Test
    fun `plainText renders strings numbers and booleans`() {
        assertEquals(
            "hello",
            ChannelDetailsSummary.plainText(JsonPrimitive("hello"), yesLabel, noLabel)
        )
        assertEquals(
            "25",
            ChannelDetailsSummary.plainText(JsonPrimitive(25), yesLabel, noLabel)
        )
        assertEquals(
            "1.5",
            ChannelDetailsSummary.plainText(JsonPrimitive(1.5), yesLabel, noLabel)
        )
        assertEquals(
            "Yes",
            ChannelDetailsSummary.plainText(JsonPrimitive(true), yesLabel, noLabel)
        )
        assertEquals(
            "No",
            ChannelDetailsSummary.plainText(JsonPrimitive(false), yesLabel, noLabel)
        )
    }

    @Test
    fun `fallback rows render booleans with localized labels`() {
        val rows = ChannelDetailsSummary.buildRows(
            channel = channel(config = mapOf("secure" to JsonPrimitive(false))),
            definition = null,
            definitionLabels = emptyMap(),
            yesLabel = yesLabel,
            noLabel = noLabel
        )
        assertTrue(rows.first().value == noLabel)
    }
}
