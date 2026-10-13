package com.jgeek00.crowdsecmonitor.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
enum class NotificationChannelType(val value: String) {
    @SerialName("ntfy") NTFY("ntfy"),
    @SerialName("email") EMAIL("email")
}

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

// Optional request fields default to null: with encodeDefaults = false, the
// shared Json instance omits null-valued fields from the body instead of sending
// explicit null, so backend defaults apply.

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
