package com.jgeek00.crowdsecmonitor.data.models

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

@Serializable(with = NotificationConditionValueSerializer::class)
sealed class NotificationConditionValue {

    data class Single(val value: String) : NotificationConditionValue()

    data class Multiple(val values: List<String>) : NotificationConditionValue()

    val asArray: List<String>
        get() = when (this) {
            is Single -> listOf(value)
            is Multiple -> values
        }
}

object NotificationConditionValueSerializer : KSerializer<NotificationConditionValue> {

    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("NotificationConditionValue")

    override fun serialize(encoder: Encoder, value: NotificationConditionValue) {
        (encoder as JsonEncoder).encodeJsonElement(valueToJsonElement(value))
    }

    override fun deserialize(decoder: Decoder): NotificationConditionValue {
        return valueFromJsonElement((decoder as JsonDecoder).decodeJsonElement())
    }

    internal fun valueToJsonElement(value: NotificationConditionValue): kotlinx.serialization.json.JsonElement {
        return when (value) {
            is NotificationConditionValue.Single -> JsonPrimitive(value.value)
            is NotificationConditionValue.Multiple -> JsonArray(value.values.map { JsonPrimitive(it) })
        }
    }

    internal fun valueFromJsonElement(element: kotlinx.serialization.json.JsonElement): NotificationConditionValue {
        return when (element) {
            is JsonPrimitive -> {
                if (!element.isString) {
                    throw SerializationException("Condition value must be a string or an array of strings")
                }
                NotificationConditionValue.Single(element.content)
            }
            is JsonArray -> NotificationConditionValue.Multiple(
                element.map { item ->
                    val primitive = item as? JsonPrimitive
                        ?: throw SerializationException("Condition value array must contain only strings")
                    if (!primitive.isString) {
                        throw SerializationException("Condition value array must contain only strings")
                    }
                    primitive.content
                }
            )
            else -> throw SerializationException("Condition value must be a string or an array of strings")
        }
    }
}

@Serializable(with = NotificationConditionNodeSerializer::class)
sealed class NotificationConditionNode {

    data class Leaf(
        val field: String,
        val operator: String,
        val value: NotificationConditionValue
    ) : NotificationConditionNode()

    data class And(val children: List<NotificationConditionNode>) : NotificationConditionNode()

    data class Or(val children: List<NotificationConditionNode>) : NotificationConditionNode()

    data class Not(val child: NotificationConditionNode) : NotificationConditionNode()
}

object NotificationConditionNodeSerializer : KSerializer<NotificationConditionNode> {

    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("NotificationConditionNode")

    override fun serialize(encoder: Encoder, value: NotificationConditionNode) {
        (encoder as JsonEncoder).encodeJsonElement(toJsonObject(value))
    }

    override fun deserialize(decoder: Decoder): NotificationConditionNode {
        return fromJsonObject((decoder as JsonDecoder).decodeJsonElement().jsonObject)
    }

    private fun toJsonObject(node: NotificationConditionNode): JsonObject {
        return when (node) {
            is NotificationConditionNode.Leaf -> buildJsonObject {
                put("type", "leaf")
                put("field", node.field)
                put("operator", node.operator)
                put("value", NotificationConditionValueSerializer.valueToJsonElement(node.value))
            }
            is NotificationConditionNode.And -> buildJsonObject {
                put("type", "and")
                put("children", JsonArray(node.children.map { toJsonObject(it) }))
            }
            is NotificationConditionNode.Or -> buildJsonObject {
                put("type", "or")
                put("children", JsonArray(node.children.map { toJsonObject(it) }))
            }
            is NotificationConditionNode.Not -> buildJsonObject {
                put("type", "not")
                put("child", toJsonObject(node.child))
            }
        }
    }

    private fun fromJsonObject(obj: JsonObject): NotificationConditionNode {
        val type = obj["type"]?.jsonPrimitive?.content
            ?: throw SerializationException("Condition node is missing 'type'")
        return when (type) {
            "leaf" -> NotificationConditionNode.Leaf(
                field = obj["field"]?.jsonPrimitive?.content
                    ?: throw SerializationException("Leaf condition is missing 'field'"),
                operator = obj["operator"]?.jsonPrimitive?.content
                    ?: throw SerializationException("Leaf condition is missing 'operator'"),
                value = NotificationConditionValueSerializer.valueFromJsonElement(
                    obj["value"] ?: throw SerializationException("Leaf condition is missing 'value'")
                )
            )
            "and" -> NotificationConditionNode.And(
                (obj["children"] ?: throw SerializationException("'and' condition is missing 'children'"))
                    .jsonArray.map { fromJsonObject(it.jsonObject) }
            )
            "or" -> NotificationConditionNode.Or(
                (obj["children"] ?: throw SerializationException("'or' condition is missing 'children'"))
                    .jsonArray.map { fromJsonObject(it.jsonObject) }
            )
            "not" -> NotificationConditionNode.Not(
                fromJsonObject(
                    obj["child"]?.jsonObject
                        ?: throw SerializationException("'not' condition is missing 'child'")
                )
            )
            else -> throw SerializationException("Unknown condition type: $type")
        }
    }

}

@Serializable
data class NotificationThreshold(
    val count: Int,
    val windowSeconds: Int,
    val cooldownSeconds: Int? = null
)

@Serializable
data class UserNotification(
    val id: Int,
    val name: String,
    val description: String? = null,
    val enabled: Boolean,
    val condition: NotificationConditionNode,
    val threshold: NotificationThreshold? = null,
    val message: String,
    val includeAlertInfo: Boolean,
    val channelIds: List<Int>,
    // These two are snake_case on the wire; everything else is camelCase.
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

@Serializable
data class NotificationHistoryChannel(
    val type: String,
    val ok: Boolean,
    val detail: String? = null
)

@Serializable
data class NotificationHistoryEntry(
    val id: String,
    val notificationId: Int,
    val notificationName: String,
    val message: String,
    val triggeredAt: String,
    val channels: List<NotificationHistoryChannel>
)

@Serializable
data class NotificationsListResponse(
    @SerialName("data") val data: List<UserNotification>
)

@Serializable
data class NotificationDetailResponse(
    @SerialName("data") val data: UserNotification
)

@Serializable
data class NotificationHistoryResponse(
    @SerialName("data") val data: List<NotificationHistoryEntry>,
    @SerialName("total") val total: Int
)

@Serializable
data class DeleteNotificationResponse(
    @SerialName("message") val message: String
)

// Optional request fields default to null: with encodeDefaults = false, the
// shared Json instance omits null-valued fields from the body instead of sending
// explicit null, so backend defaults apply.

@Serializable
data class CreateNotificationRequest(
    val name: String,
    val description: String? = null,
    val enabled: Boolean? = null,
    val condition: NotificationConditionNode,
    val threshold: NotificationThreshold? = null,
    val message: String,
    val includeAlertInfo: Boolean,
    val channelIds: List<Int>
)

@Serializable
data class UpdateNotificationRequest(
    val name: String? = null,
    val description: String? = null,
    val enabled: Boolean? = null,
    val condition: NotificationConditionNode? = null,
    val threshold: NotificationThreshold? = null,
    val message: String? = null,
    val includeAlertInfo: Boolean? = null,
    val channelIds: List<Int>? = null
)

@Serializable
data class ToggleNotificationRequest(
    val enabled: Boolean
)
