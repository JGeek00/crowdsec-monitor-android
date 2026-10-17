package com.jgeek00.crowdsecmonitor.data.models

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationProviderModelsTest {

    // Same Json configuration as the shared HttpClient instance.
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    @Test
    fun `providers list response decodes field for field`() {
        val wire = """
            {
              "version": 2,
              "providers": [
                {
                  "type": "ntfy",
                  "descriptionKey": "providers.ntfy.description",
                  "icon": "ntfy",
                  "labelKey": "providers.ntfy.label",
                  "supportsTest": true,
                  "supportsAlertInfo": true,
                  "sections": [
                    {"key": "server", "labelKey": "providers.ntfy.section.server"}
                  ],
                  "fields": [
                    {
                      "key": "url",
                      "section": "server",
                      "labelKey": "providers.ntfy.url.label",
                      "placeholderKey": "providers.ntfy.url.placeholder",
                      "type": "url",
                      "required": true
                    },
                    {
                      "key": "priority",
                      "section": "server",
                      "labelKey": "providers.ntfy.priority.label",
                      "type": "number",
                      "min": 1.0,
                      "max": 5.0,
                      "integer": true,
                      "default": 3
                    }
                  ]
                }
              ]
            }
        """.trimIndent()

        val response = json.decodeFromString<ProvidersListResponse>(wire)

        assertEquals(2, response.version)
        val provider = response.providers.first()
        assertEquals("ntfy", provider.type)
        assertEquals("providers.ntfy.description", provider.descriptionKey)
        assertEquals("ntfy", provider.icon)
        assertEquals(true, provider.supportsTest)
        assertEquals(true, provider.supportsAlertInfo)
        assertEquals(1, provider.sections?.size)
        assertEquals("providers.ntfy.section.server", provider.sections?.first()?.labelKey)

        val urlField = provider.fields.first()
        assertEquals("url", urlField.key)
        assertEquals("server", urlField.section)
        assertEquals("providers.ntfy.url.placeholder", urlField.placeholderKey)
        assertEquals("url", urlField.type)
        assertEquals(true, urlField.required)
        assertNull(urlField.defaultValue)

        val priorityField = provider.fields.last()
        assertEquals(3.0, (priorityField.defaultValue as JsonPrimitive).content.toDouble(), 0.0)
        assertEquals(1.0, priorityField.min!!, 0.0)
        assertEquals(5.0, priorityField.max!!, 0.0)
        assertEquals(true, priorityField.integer)
    }

    @Test
    fun `provider field decodes default with json string and boolean`() {
        val wire = """
            {
              "type": "email",
              "icon": "email",
              "labelKey": "providers.email.label",
              "supportsTest": true,
              "supportsAlertInfo": false,
              "fields": [
                {
                  "key": "from",
                  "labelKey": "providers.email.from.label",
                  "type": "email",
                  "default": "alerts@example.com"
                },
                {
                  "key": "tls",
                  "labelKey": "providers.email.tls.label",
                  "type": "boolean",
                  "default": true
                }
              ]
            }
        """.trimIndent()

        val provider = json.decodeFromString<NotificationProvider>(wire)

        assertEquals("alerts@example.com", (provider.fields[0].defaultValue as JsonPrimitive).content)
        assertEquals(true, (provider.fields[1].defaultValue as JsonPrimitive).content.toBoolean())
    }

    @Test
    fun `provider field decodes visibleIf requiredIf and exclusiveWith`() {
        val wire = """
            {
              "type": "ntfy",
              "icon": "ntfy",
              "labelKey": "providers.ntfy.label",
              "supportsTest": true,
              "supportsAlertInfo": false,
              "fields": [
                {
                  "key": "tokenFile",
                  "labelKey": "providers.ntfy.tokenFile.label",
                  "type": "string",
                  "requiredIf": {"field": "authMethod", "equals": "token", "present": true},
                  "exclusiveWith": ["token"],
                  "visibleIf": {"field": "authMethod", "equals": "token", "present": true}
                },
                {
                  "key": "token",
                  "labelKey": "providers.ntfy.token.label",
                  "type": "string",
                  "secret": true
                }
              ]
            }
        """.trimIndent()

        val provider = json.decodeFromString<NotificationProvider>(wire)

        val tokenFileField = provider.fields.first()
        val requiredIf = tokenFileField.requiredIf
        assertEquals("authMethod", requiredIf?.field)
        assertEquals("token", (requiredIf?.equalsValue as JsonPrimitive).content)
        assertEquals(true, requiredIf.present)
        assertEquals(listOf("token"), tokenFileField.exclusiveWith)
        assertEquals("token", (tokenFileField.visibleIf?.equalsValue as JsonPrimitive).content)

        val tokenField = provider.fields.last()
        assertEquals(true, tokenField.secret)
        assertNull(tokenField.requiredIf)
    }

    @Test
    fun `provider field decodes select options and list flag`() {
        val wire = """
            {
              "type": "email",
              "icon": "email",
              "labelKey": "providers.email.label",
              "supportsTest": false,
              "supportsAlertInfo": false,
              "fields": [
                {
                  "key": "authMethod",
                  "labelKey": "providers.email.auth.label",
                  "type": "select",
                  "list": false,
                  "options": [
                    {"value": "none", "labelKey": "providers.email.auth.none"},
                    {"value": "password", "labelKey": "providers.email.auth.password"}
                  ]
                }
              ]
            }
        """.trimIndent()

        val provider = json.decodeFromString<NotificationProvider>(wire)

        val field = provider.fields.first()
        assertEquals("select", field.type)
        assertEquals(false, field.list)
        assertEquals(2, field.options?.size)
        assertEquals("none", field.options?.first()?.value)
        assertEquals("providers.email.auth.password", field.options?.last()?.labelKey)
    }

    @Test
    fun `unknown provider field type still decodes as open string`() {
        val wire = """
            {
              "type": "future",
              "icon": "bell",
              "labelKey": "providers.future.label",
              "supportsTest": false,
              "supportsAlertInfo": false,
              "fields": [
                {"key": "magic", "labelKey": "providers.future.magic.label", "type": "hologram"}
              ]
            }
        """.trimIndent()

        val provider = json.decodeFromString<NotificationProvider>(wire)

        assertEquals("hologram", provider.fields.first().type)
        assertNull(provider.fields.first().defaultValue)
    }

    @Test
    fun `provider models round-trip`() {
        val provider = NotificationProvider(
            type = "ntfy",
            descriptionKey = null,
            icon = "ntfy",
            labelKey = "providers.ntfy.label",
            supportsTest = true,
            supportsAlertInfo = true,
            sections = listOf(ProviderSection(key = "server", labelKey = "providers.ntfy.section.server")),
            fields = listOf(
                ProviderField(
                    key = "url",
                    labelKey = "providers.ntfy.url.label",
                    type = "url",
                    required = true,
                    options = null,
                    visibleIf = ProviderFieldCondition(field = "authMethod", equalsValue = JsonPrimitive("token"))
                )
            )
        )

        val encoded = json.encodeToString(provider)
        val decoded = json.decodeFromString<NotificationProvider>(encoded)

        assertEquals(provider, decoded)
        assertTrue(encoded.contains("\"visibleIf\""))
        assertTrue(encoded.contains("\"equals\":\"token\""))
    }
}
