package com.jgeek00.crowdsecmonitor.viewmodel

import com.jgeek00.crowdsecmonitor.data.models.NotificationConditionNode
import com.jgeek00.crowdsecmonitor.data.models.NotificationConditionValue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EditableConditionTest {

    @Test
    fun `empty leaf defaults to scenario equals with no values and is invalid`() {
        val leaf = EditableLeaf.Empty
        assertEquals(ConditionField.SCENARIO, leaf.field)
        assertEquals(ConditionOperator.EQUALS, leaf.op)
        assertTrue(leaf.values.isEmpty())
        assertFalse(leaf.isValid)
    }

    @Test
    fun `leaf with values is valid`() {
        val leaf = EditableLeaf(
            field = ConditionField.COUNTRY,
            op = ConditionOperator.IN,
            values = listOf("US", "DE")
        )
        assertTrue(leaf.isValid)
    }

    @Test
    fun `leaf with empty value is invalid`() {
        val leaf = EditableLeaf(
            field = ConditionField.COUNTRY,
            op = ConditionOperator.IN,
            values = listOf("US", "")
        )
        assertFalse(leaf.isValid)
    }

    @Test
    fun `in and notIn need multiple values, others do not`() {
        assertTrue(ConditionOperator.IN.needsMultipleValues)
        assertTrue(ConditionOperator.NOT_IN.needsMultipleValues)
        assertFalse(ConditionOperator.EQUALS.needsMultipleValues)
        assertFalse(ConditionOperator.NOT_EQUALS.needsMultipleValues)
        assertFalse(ConditionOperator.CONTAINS.needsMultipleValues)
    }

    @Test
    fun `operator wire names match the API DSL`() {
        assertEquals("equals", ConditionOperator.EQUALS.wireName)
        assertEquals("not_equals", ConditionOperator.NOT_EQUALS.wireName)
        assertEquals("in", ConditionOperator.IN.wireName)
        assertEquals("not_in", ConditionOperator.NOT_IN.wireName)
        assertEquals("contains", ConditionOperator.CONTAINS.wireName)
    }

    @Test
    fun `field wire names match the API DSL`() {
        assertEquals("scenario", ConditionField.SCENARIO.wireName)
        assertEquals("country", ConditionField.COUNTRY.wireName)
        assertEquals("target", ConditionField.TARGET.wireName)
        assertEquals("ipOwner", ConditionField.IP_OWNER.wireName)
    }

    @Test
    fun `valid and group is valid`() {
        val condition = EditableCondition.And(
            listOf(
                EditableCondition.Leaf(EditableLeaf(values = listOf("a"))),
                EditableCondition.Leaf(EditableLeaf(values = listOf("b")))
            )
        )
        assertTrue(condition.isValid)
    }

    @Test
    fun `empty and and or groups are invalid`() {
        assertFalse(EditableCondition.And(emptyList()).isValid)
        assertFalse(EditableCondition.Or(emptyList()).isValid)
    }

    @Test
    fun `group with an invalid child is invalid`() {
        val condition = EditableCondition.Or(
            listOf(
                EditableCondition.Leaf(EditableLeaf(values = listOf("a"))),
                EditableCondition.Leaf(EditableLeaf(values = emptyList()))
            )
        )
        assertFalse(condition.isValid)
    }

    @Test
    fun `not delegates validity to its child`() {
        assertTrue(
            EditableCondition.Not(EditableCondition.Leaf(EditableLeaf(values = listOf("a")))).isValid
        )
        assertFalse(
            EditableCondition.Not(EditableCondition.Leaf(EditableLeaf(values = emptyList()))).isValid
        )
    }

    @Test
    fun `leafCount counts leaves in nested groups`() {
        val condition = EditableCondition.And(
            listOf(
                EditableCondition.Or(
                    listOf(
                        EditableCondition.Leaf(EditableLeaf(values = listOf("a"))),
                        EditableCondition.Leaf(EditableLeaf(values = listOf("b")))
                    )
                ),
                EditableCondition.Not(
                    EditableCondition.Leaf(EditableLeaf(values = listOf("c")))
                )
            )
        )
        assertEquals(3, condition.leafCount)
        assertEquals(1, EditableCondition.Leaf(EditableLeaf()).leafCount)
    }

    @Test
    fun `single value leaf encodes as string`() {
        val condition = EditableCondition.Leaf(
            EditableLeaf(
                field = ConditionField.SCENARIO,
                op = ConditionOperator.EQUALS,
                values = listOf("crowdsec/ssh-bf")
            )
        )
        val node = condition.toApi()
        assertTrue(node is NotificationConditionNode.Leaf)
        assertEquals(
            NotificationConditionValue.Single("crowdsec/ssh-bf"),
            (node as NotificationConditionNode.Leaf).value
        )
        assertEquals("scenario", node.field)
        assertEquals("equals", node.operator)
    }

    @Test
    fun `multiple value leaf encodes as array`() {
        val condition = EditableCondition.Leaf(
            EditableLeaf(
                field = ConditionField.TARGET,
                op = ConditionOperator.IN,
                values = listOf("1.2.3.4", "5.6.7.8")
            )
        )
        val node = condition.toApi()
        assertTrue(node is NotificationConditionNode.Leaf)
        assertEquals(
            NotificationConditionValue.Multiple(listOf("1.2.3.4", "5.6.7.8")),
            (node as NotificationConditionNode.Leaf).value
        )
    }

    @Test
    fun `groups and not convert recursively`() {
        val condition = EditableCondition.Not(
            EditableCondition.Or(
                listOf(
                    EditableCondition.Leaf(EditableLeaf(values = listOf("a"))),
                    EditableCondition.Leaf(EditableLeaf(values = listOf("b")))
                )
            )
        )
        val node = condition.toApi()
        assertTrue(node is NotificationConditionNode.Not)
        assertTrue((node as NotificationConditionNode.Not).child is NotificationConditionNode.Or)
        assertEquals(2, condition.leafCount)
    }

    @Test
    fun `wire round-trip preserves tree and values`() {
        val api = NotificationConditionNode.And(
            listOf(
                NotificationConditionNode.Leaf(
                    field = "scenario",
                    operator = "equals",
                    value = NotificationConditionValue.Single("crowdsec/ssh-bf")
                ),
                NotificationConditionNode.Or(
                    listOf(
                        NotificationConditionNode.Leaf(
                            field = "country",
                            operator = "in",
                            value = NotificationConditionValue.Multiple(listOf("US", "DE"))
                        ),
                        NotificationConditionNode.Not(
                            NotificationConditionNode.Leaf(
                                field = "ipOwner",
                                operator = "contains",
                                value = NotificationConditionValue.Single("amazon")
                            )
                        )
                    )
                )
            )
        )

        val editable = EditableCondition.fromApi(api)
        assertEquals(api, editable.toApi())
        assertEquals(3, editable.leafCount)
        assertTrue(editable.isValid)
    }

    @Test
    fun `unknown field defaults to scenario`() {
        val condition = EditableCondition.fromApi(
            NotificationConditionNode.Leaf(
                field = "origin",
                operator = "equals",
                value = NotificationConditionValue.Single("x")
            )
        )
        val leaf = (condition as EditableCondition.Leaf).leaf
        assertEquals(ConditionField.SCENARIO, leaf.field)
        assertEquals(ConditionOperator.EQUALS, leaf.op)
        assertEquals(listOf("x"), leaf.values)
    }

    @Test
    fun `unknown operator defaults to equals`() {
        val condition = EditableCondition.fromApi(
            NotificationConditionNode.Leaf(
                field = "target",
                operator = "starts_with",
                value = NotificationConditionValue.Single("1.2.3.4")
            )
        )
        val leaf = (condition as EditableCondition.Leaf).leaf
        assertEquals(ConditionField.TARGET, leaf.field)
        assertEquals(ConditionOperator.EQUALS, leaf.op)
    }

    @Test
    fun `wire string value decodes to single-value leaf`() {
        val condition = EditableCondition.fromApi(
            NotificationConditionNode.Leaf(
                field = "country",
                operator = "equals",
                value = NotificationConditionValue.Single("US")
            )
        )
        val leaf = (condition as EditableCondition.Leaf).leaf
        assertEquals(listOf("US"), leaf.values)
        assertTrue(leaf.isValid)
    }

    @Test
    fun `OR of same-field leaves is not auto-merged by conversion`() {
        // Merging belongs to the form ViewModel; conversion must stay faithful.
        val api = NotificationConditionNode.Or(
            listOf(
                NotificationConditionNode.Leaf(
                    field = "country",
                    operator = "equals",
                    value = NotificationConditionValue.Single("US")
                ),
                NotificationConditionNode.Leaf(
                    field = "country",
                    operator = "equals",
                    value = NotificationConditionValue.Single("DE")
                )
            )
        )
        val editable = EditableCondition.fromApi(api)
        assertEquals(2, editable.leafCount)
        assertEquals(api, editable.toApi())
    }
}
