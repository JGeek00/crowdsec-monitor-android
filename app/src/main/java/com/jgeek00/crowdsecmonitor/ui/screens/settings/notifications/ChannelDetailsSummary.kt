package com.jgeek00.crowdsecmonitor.ui.screens.settings.notifications

import com.jgeek00.crowdsecmonitor.data.models.NotificationProvider
import com.jgeek00.crowdsecmonitor.data.models.UserNotificationChannel
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive

/**
 * Read-only summary rows for the channel details screen, derived from the
 * provider definition. Pure logic (no Compose), mirroring the iOS
 * ChannelDetailsView summary computation:
 *
 * - With a definition: one row per non-secret field, in definition order;
 *   the shown value is the channel config value, falling back to the field
 *   default.
 * - Without a definition: fallback to the raw config keys (sorted), hiding the
 *   well-known secret keys `password` and `accessToken` (gotcha 13).
 * - Secrets are NEVER displayed (gotcha 13): definition fields with
 *   `secret == true` are filtered out, also in the fallback path.
 */
object ChannelDetailsSummary {

    /**
     * Fallback secret keys used when the provider definition is unavailable:
     * mirrors the iOS `["password", "accessToken"]`.
     */
    val FALLBACK_SECRET_KEYS: Set<String> = setOf("password", "accessToken")

    data class Row(val label: String, val value: String)

    /**
     * Resolves the set of secret config keys for a channel. With a definition
     * it is the set of fields flagged `secret`; otherwise the well-known
     * fallback keys.
     */
    fun secretKeys(definition: NotificationProvider?): Set<String> {
        if (definition == null) return FALLBACK_SECRET_KEYS
        return definition.fields
            .filter { it.secret == true }
            .map { it.key }
            .toSet()
    }

    /**
     * Builds the read-only rows. `definitionLabels` maps definition field keys
     * to already-resolved display labels (localized `labelKey`), computed by
     * the caller in composable context; unknown keys fall back to the raw key.
     */
    fun buildRows(
        channel: UserNotificationChannel,
        definition: NotificationProvider?,
        definitionLabels: Map<String, String> = emptyMap(),
        yesLabel: String,
        noLabel: String
    ): List<Row> {
        val secretKeys = secretKeys(definition)
        if (definition != null) {
            return definition.fields.mapNotNull { field ->
                if (field.key in secretKeys) return@mapNotNull null
                val value = displayText(
                    configValue = channel.config[field.key],
                    defaultValue = field.defaultValue,
                    yesLabel = yesLabel,
                    noLabel = noLabel
                ) ?: return@mapNotNull null
                Row(label = definitionLabels[field.key] ?: field.key, value = value)
            }
        }
        return channel.config.keys
            .filter { it !in secretKeys }
            .sorted()
            .mapNotNull { key ->
                val value = channel.config[key] ?: return@mapNotNull null
                val text = plainText(value, yesLabel, noLabel)
                Row(label = key, value = text)
            }
    }

    /**
     * Display text for one field: the channel config value wins; when absent
     * (missing or JSON null) the field default is shown instead. Returns null
     * when neither is present.
     */
    fun displayText(
        configValue: JsonElement?,
        defaultValue: JsonElement?,
        yesLabel: String,
        noLabel: String
    ): String? {
        val value = configValue ?: defaultValue ?: return null
        if (value is JsonNull) {
            val fallback = defaultValue ?: return null
            if (fallback is JsonNull) return null
            return plainText(fallback, yesLabel, noLabel)
        }
        return plainText(value, yesLabel, noLabel)
    }

    /**
     * Plain-text rendering of a dynamic JSON value, mirroring the iOS
     * `plainText(_:)`: strings verbatim, ints and doubles as numbers, booleans
     * as localized Yes/No.
     */
    fun plainText(value: JsonElement, yesLabel: String, noLabel: String): String {
        val primitive = value as? JsonPrimitive ?: return value.toString()
        if (primitive.isString) return primitive.content
        return when (primitive.content) {
            "true" -> yesLabel
            "false" -> noLabel
            else -> primitive.content
        }
    }
}
