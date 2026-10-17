package com.jgeek00.crowdsecmonitor.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jgeek00.crowdsecmonitor.data.models.ChannelTestResponse
import com.jgeek00.crowdsecmonitor.data.models.CreateChannelRequest
import com.jgeek00.crowdsecmonitor.data.models.NotificationProvider
import com.jgeek00.crowdsecmonitor.data.models.ProviderField
import com.jgeek00.crowdsecmonitor.data.models.ProviderFieldCondition
import com.jgeek00.crowdsecmonitor.data.models.ProviderSection
import com.jgeek00.crowdsecmonitor.data.models.TestChannelRequest
import com.jgeek00.crowdsecmonitor.data.models.TestInlineChannelRequest
import com.jgeek00.crowdsecmonitor.data.models.UpdateChannelRequest
import com.jgeek00.crowdsecmonitor.data.models.UserNotificationChannel
import com.jgeek00.crowdsecmonitor.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import javax.inject.Inject

sealed class ChannelTestState {
    data object Idle : ChannelTestState()
    data object Testing : ChannelTestState()
    data object Success : ChannelTestState()
    data class Failure(val detail: String) : ChannelTestState()
}

/**
 * Channel creation/edit form. Field visibility, conditional requiredness and
 * exclusivity follow the provider definitions; client-side validation is light
 * on purpose because the backend enforces strictly. Saving always sends the
 * full config, never a partial one; testing uses the saved channel endpoint
 * when editing and the inline test endpoint otherwise.
 */
@HiltViewModel
class ChannelFormViewModel @Inject constructor(
    private val sessionManager: SessionManager
) : ViewModel() {

    private var editing: UserNotificationChannel? = null

    val isEditing: Boolean
        get() = editing != null

    var providers by mutableStateOf<List<NotificationProvider>>(emptyList())

    var providerType by mutableStateOf<String?>(null)

    var values by mutableStateOf<Map<String, JsonElement>>(emptyMap())

    var name by mutableStateOf("")

    var testState by mutableStateOf<ChannelTestState>(ChannelTestState.Idle)

    var isSaving by mutableStateOf(false)

    var saveError by mutableStateOf(false)

    var loadError by mutableStateOf(false)

    val definition: NotificationProvider?
        get() = providers.firstOrNull { it.type == providerType }

    fun load(channel: UserNotificationChannel) {
        editing = channel
        providerType = channel.type.value
        name = channel.name
        values = channel.config
    }

    fun loadProviders() {
        val apiClient = sessionManager.apiClient ?: return
        viewModelScope.launch {
            try {
                val response = apiClient.notificationChannels.fetchProviders()
                providers = response.body.providers
                applyDefaults()
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                loadError = true
            }
        }
    }

    fun selectProvider(type: String) {
        providerType = type
        applyDefaults()
    }

    /**
     * Fill definition defaults for missing keys so displayed selections
     * always match a picker tag (avoids invalid-selection warnings).
     */
    private fun applyDefaults() {
        val definition = definition ?: return
        val filled = values.toMutableMap()
        for (field in definition.fields) {
            if (filled[field.key] == null && field.defaultValue != null) {
                filled[field.key] = field.defaultValue
            }
        }
        values = filled
    }

    private fun isPresent(value: JsonElement?): Boolean {
        if (value == null || value is JsonNull) return false
        if (value is JsonPrimitive) {
            return if (value.isString) value.content.trim().isNotEmpty() else true
        }
        return true
    }

    private fun conditionHolds(condition: ProviderFieldCondition): Boolean {
        val value = values[condition.field]
        val present = condition.present
        if (present != null) {
            return present == isPresent(value)
        }
        val expected = condition.equalsValue
        if (expected != null) {
            return value == expected
        }
        return false
    }

    fun visibleFields(): List<ProviderField> {
        val definition = definition ?: return emptyList()
        return definition.fields.filter { field ->
            val visibleIf = field.visibleIf ?: return@filter true
            conditionHolds(visibleIf)
        }
    }

    /**
     * Visible fields grouped by section in definition order; fields without
     * (or with unknown) section come last, ungrouped (`section` is null).
     */
    fun groupedFields(): List<Pair<ProviderSection?, List<ProviderField>>> {
        val definition = definition ?: return emptyList()
        val visible = visibleFields()
        val known = definition.sections.orEmpty().map { it.key }.toSet()
        val groups = mutableListOf<Pair<ProviderSection?, List<ProviderField>>>()
        for (section in definition.sections.orEmpty()) {
            val fields = visible.filter { it.section == section.key }
            if (fields.isNotEmpty()) {
                groups.add(section to fields)
            }
        }
        val ungrouped = visible.filter { it.section == null || it.section !in known }
        if (ungrouped.isNotEmpty()) {
            groups.add(null to ungrouped)
        }
        return groups
    }

    fun isFieldValid(field: ProviderField): Boolean {
        val value = values[field.key]
        val required = field.required == true ||
            (field.requiredIf?.let { conditionHolds(it) } == true)
        if (!isPresent(value)) {
            return !required
        }
        return when (field.type) {
            "text", "password" -> {
                val primitive = value as? JsonPrimitive ?: return false
                if (!primitive.isString) return false
                val text = primitive.content
                if (field.minLength != null && text.length < field.minLength) return false
                if (field.maxLength != null && text.length > field.maxLength) return false
                field.regex?.let { pattern ->
                    val regex = runCatching { Regex(pattern) }.getOrNull() ?: return false
                    return regex.containsMatchIn(text)
                }
                true
            }
            "email" -> {
                val primitive = value as? JsonPrimitive ?: return false
                if (!primitive.isString) return false
                primitive.content.split(",").all { part ->
                    val address = part.trim()
                    address.contains("@") && address.contains(".")
                }
            }
            "url" -> {
                val primitive = value as? JsonPrimitive ?: return false
                if (!primitive.isString) return false
                val text = primitive.content
                text.startsWith("http://") || text.startsWith("https://")
            }
            "number" -> {
                val primitive = value as? JsonPrimitive ?: return false
                val number: Double = if (primitive.isString) {
                    val text = primitive.content
                    if (text.isEmpty()) return false
                    text.toDoubleOrNull() ?: return false
                } else {
                    primitive.content.toDoubleOrNull() ?: return false
                }
                if (field.integer == true && number % 1.0 != 0.0) return false
                if (field.min != null && number < field.min) return false
                if (field.max != null && number > field.max) return false
                true
            }
            "boolean" -> isPresent(value)
            "select" -> {
                val primitive = value as? JsonPrimitive ?: return false
                if (!primitive.isString) return false
                field.options?.any { it.value == primitive.content } == true
            }
            else -> true
        }
    }

    fun providerValid(): Boolean {
        if (providerType == null) return false
        val fields = visibleFields()
        if (!fields.all { isFieldValid(it) }) return false
        val present = values.keys.filter { isPresent(values[it]) }.toSet()
        for (field in fields) {
            if (field.key in present) {
                val exclusive = field.exclusiveWith
                if (exclusive != null && exclusive.any { it in present }) {
                    return false
                }
            }
        }
        return true
    }

    fun nameValid(): Boolean {
        return name.trim().isNotEmpty()
    }

    /**
     * Builds the config payload from definition fields with present values.
     * Number strings are coerced to int (when the field is integer) or double.
     * Returns null when the provider is not selected or not valid.
     */
    fun buildPayload(): Map<String, JsonElement>? {
        val definition = definition ?: return null
        if (!providerValid()) return null
        val payload = mutableMapOf<String, JsonElement>()
        for (field in definition.fields) {
            val value = values[field.key] ?: continue
            if (!isPresent(value)) continue
            if (field.type == "number") {
                val primitive = value as? JsonPrimitive
                if (primitive != null && primitive.isString) {
                    val text = primitive.content
                    if (field.integer == true) {
                        val intValue = text.toIntOrNull()
                        if (intValue != null) {
                            payload[field.key] = JsonPrimitive(intValue)
                            continue
                        }
                    }
                    val doubleValue = text.toDoubleOrNull()
                    if (doubleValue != null) {
                        payload[field.key] = JsonPrimitive(doubleValue)
                    }
                } else {
                    payload[field.key] = value
                }
            } else {
                payload[field.key] = value
            }
        }
        return payload
    }

    suspend fun test() {
        val apiClient = sessionManager.apiClient ?: return
        val type = providerType ?: return
        val payload = buildPayload() ?: return
        testState = ChannelTestState.Testing
        try {
            val message = "Test notification from \"${name.trim()}\""
            val editing = this.editing
            val result = if (editing != null) {
                apiClient.notificationChannels.testChannel(
                    editing.id,
                    TestChannelRequest(message = message)
                )
            } else {
                apiClient.notificationChannels.testInlineChannel(
                    TestInlineChannelRequest(type = type, config = payload, message = message)
                )
            }
            val data = result.body.data
            testState = if (data.ok) {
                ChannelTestState.Success
            } else {
                ChannelTestState.Failure(data.detail ?: "Unknown error")
            }
        } catch (e: CancellationException) {
            testState = ChannelTestState.Idle
            throw e
        } catch (e: Exception) {
            testState = ChannelTestState.Failure(e.message ?: "Unknown error")
        }
    }

    /**
     * Creates or updates the channel sending the full config (never partial).
     * Returns the saved channel or null on failure (with [saveError] set).
     */
    suspend fun save(): UserNotificationChannel? {
        val apiClient = sessionManager.apiClient ?: return null
        val type = providerType ?: return null
        val payload = buildPayload() ?: return null
        isSaving = true
        try {
            val editing = this.editing
            return if (editing != null) {
                val body = UpdateChannelRequest(
                    name = name.trim(),
                    type = type,
                    config = payload
                )
                apiClient.notificationChannels.updateChannel(editing.id, body).body.data
            } else {
                val body = CreateChannelRequest(
                    name = name.trim(),
                    type = type,
                    config = payload
                )
                apiClient.notificationChannels.createChannel(body).body.data
            }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            saveError = true
            return null
        } finally {
            isSaving = false
        }
    }
}
