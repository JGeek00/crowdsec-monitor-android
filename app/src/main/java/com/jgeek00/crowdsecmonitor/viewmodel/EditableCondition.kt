package com.jgeek00.crowdsecmonitor.viewmodel

import com.jgeek00.crowdsecmonitor.data.models.NotificationConditionNode
import com.jgeek00.crowdsecmonitor.data.models.NotificationConditionValue

// UI-friendly subset of the wire condition DSL, with bidirectional conversion to
// [NotificationConditionNode]. Conversion is faithful: no normalization (e.g. OR
// merging of same-field leaves) happens here; that is the form ViewModel's job.

enum class ConditionField(val wireName: String) {
    SCENARIO("scenario"),
    COUNTRY("country"),
    TARGET("target"),
    IP_OWNER("ipOwner");

    companion object {
        fun fromWire(value: String): ConditionField {
            return entries.firstOrNull { it.wireName == value } ?: SCENARIO
        }
    }
}

enum class ConditionOperator(val wireName: String) {
    EQUALS("equals"),
    NOT_EQUALS("not_equals"),
    IN("in"),
    NOT_IN("not_in"),
    CONTAINS("contains");

    val needsMultipleValues: Boolean
        get() = this == IN || this == NOT_IN

    companion object {
        fun fromWire(value: String): ConditionOperator {
            return entries.firstOrNull { it.wireName == value } ?: EQUALS
        }
    }
}

data class EditableLeaf(
    val field: ConditionField = ConditionField.SCENARIO,
    val op: ConditionOperator = ConditionOperator.EQUALS,
    val values: List<String> = emptyList()
) {

    val isValid: Boolean
        get() = values.isNotEmpty() && values.all { it.isNotEmpty() }

    companion object {
        val Empty: EditableLeaf = EditableLeaf()
    }
}

sealed class EditableCondition {

    data class Leaf(val leaf: EditableLeaf) : EditableCondition()

    data class And(val children: List<EditableCondition>) : EditableCondition()

    data class Or(val children: List<EditableCondition>) : EditableCondition()

    data class Not(val child: EditableCondition) : EditableCondition()

    val leafCount: Int
        get() = when (this) {
            is Leaf -> 1
            is And -> children.sumOf { it.leafCount }
            is Or -> children.sumOf { it.leafCount }
            is Not -> child.leafCount
        }

    val isValid: Boolean
        get() = when (this) {
            is Leaf -> leaf.isValid
            is And -> children.isNotEmpty() && children.all { it.isValid }
            is Or -> children.isNotEmpty() && children.all { it.isValid }
            is Not -> child.isValid
        }

    fun toApi(): NotificationConditionNode {
        return when (this) {
            is Leaf -> {
                val value = if (leaf.values.size == 1) {
                    NotificationConditionValue.Single(leaf.values.first())
                } else {
                    NotificationConditionValue.Multiple(leaf.values)
                }
                NotificationConditionNode.Leaf(
                    field = leaf.field.wireName,
                    operator = leaf.op.wireName,
                    value = value
                )
            }
            is And -> NotificationConditionNode.And(children.map { it.toApi() })
            is Or -> NotificationConditionNode.Or(children.map { it.toApi() })
            is Not -> NotificationConditionNode.Not(child.toApi())
        }
    }

    companion object {
        fun fromApi(node: NotificationConditionNode): EditableCondition {
            return when (node) {
                is NotificationConditionNode.Leaf -> Leaf(
                    EditableLeaf(
                        field = ConditionField.fromWire(node.field),
                        op = ConditionOperator.fromWire(node.operator),
                        values = node.value.asArray
                    )
                )
                is NotificationConditionNode.And -> And(node.children.map { fromApi(it) })
                is NotificationConditionNode.Or -> Or(node.children.map { fromApi(it) })
                is NotificationConditionNode.Not -> Not(fromApi(node.child))
            }
        }
    }
}
