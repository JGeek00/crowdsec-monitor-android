package com.jgeek00.crowdsecmonitor.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Wire models for notification channels. Mirrors the iOS Monitor models
 * (NotificationChannel.swift) and the backend contract at /api/v1/notification-channels.
 */

// MARK: - Channel type (built-in providers)

@Serializable
enum class NotificationChannelType(val value: String) {
    @SerialName("ntfy") NTFY("ntfy"),
    @SerialName("email") EMAIL("email")
}

// MARK: - Channel

@Serializable
data class UserNotificationChannel(
    val id: Int,
    val name: String,
    val type: NotificationChannelType,
    @SerialName("supportsAlertInfo") val supportsAlertInfo: Boolean,
    val config: Map<String, JsonElement> = emptyMap(),
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

// MARK: - Responses

@Serializable
data class NotificationChannelsListResponse(
    @SerialName("data") val data: List<UserNotificationChannel>
)

@Serializable
data class NotificationChannelDetailResponse(
    @SerialName("data") val data: UserNotificationChannel
)

@Serializable
data class DeleteNotificationChannelResponse(
    @SerialName("message") val message: String
)

@Serializable
data class ChannelTestResult(
    @SerialName("channelId") val channelId: Int? = null,
    @SerialName("ok") val ok: Boolean,
    @SerialName("detail") val detail: String? = null
)

@Serializable
data class ChannelTestResponse(
    @SerialName("data") val data: ChannelTestResult
)

// MARK: - Request bodies
//
// Omit-vs-null semantics: optional fields are declared as nullable properties
// with `= null` default values. The shared Json instance in HttpClient keeps
// kotlinx.serialization's default encodeDefaults = false, so null-valued optional
// fields are omitted from the JSON body entirely instead of being sent as
// explicit `null` — backend defaults then apply.

@Serializable
data class CreateChannelRequest(
    val name: String,
    val type: String,
    val config: Map<String, JsonElement> = emptyMap()
)

@Serializable
data class UpdateChannelRequest(
    val name: String? = null,
    val type: String? = null,
    val config: Map<String, JsonElement>? = null
)

@Serializable
data class TestChannelRequest(
    val message: String? = null
)

@Serializable
data class TestInlineChannelRequest(
    val type: String,
    val config: Map<String, JsonElement> = emptyMap(),
    val message: String? = null
)
