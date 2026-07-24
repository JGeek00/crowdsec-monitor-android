package com.jgeek00.crowdsecmonitor.data.models

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ModelMappingsTest {

    @Test
    fun `toDecisionsListResponseItem maps all fields including nested source`() {
        val decision = AlertDetailsDecision(
            id = 1,
            alertId = 100,
            origin = "crowdsec",
            type = "ban",
            scope = "ip",
            value = "1.2.3.4",
            expiration = "2026-12-31T23:59:59Z",
            scenario = "crowdsec/ssh-bf",
            simulated = false,
            source = AlertSource(
                asName = "Test AS", asNumber = "12345", cn = "US",
                ip = "1.2.3.4", latitude = 40.0, longitude = -74.0,
                range = "1.2.3.0/24", scope = "ip", value = "1.2.3.4"
            ),
            crowdsecCreatedAt = "2026-07-24T10:00:00Z"
        )

        val result = decision.toDecisionsListResponseItem()

        assertEquals(1, result.id)
        assertEquals(100, result.alertId)
        assertEquals("crowdsec/ssh-bf", result.scenario)
        assertEquals(false, result.simulated)
        assertEquals("Test AS", result.source.asName)
        assertEquals("12345", result.source.asNumber)
        assertEquals("US", result.source.cn)
        assertEquals("1.2.3.4", result.source.ip)
        assertEquals(40.0, result.source.latitude!!, 0.001)
        assertEquals(-74.0, result.source.longitude!!, 0.001)
        assertEquals("1.2.3.0/24", result.source.range)
        assertEquals("ip", result.source.scope)
        assertEquals("1.2.3.4", result.source.value)
    }

    @Test
    fun `toAlertsListResponseAlert maps all fields including nested source and empty collections`() {
        val decision = DecisionItemAlert(
            id = 42,
            uuid = "uuid-042",
            scenario = "crowdsec/http-bf",
            scenarioVersion = "2.0",
            scenarioHash = "def456",
            message = "HTTP brute force",
            capacity = 20,
            leakspeed = "2s",
            simulated = true,
            remediation = false,
            eventsCount = 10,
            machineId = "machine-042",
            source = DecisionSource(
                asName = null, asNumber = null, cn = "ES",
                ip = "5.6.7.8", latitude = null, longitude = null,
                range = null, scope = "ip", value = "5.6.7.8"
            ),
            meta = emptyList(),
            events = emptyList(),
            crowdsecCreatedAt = "2026-07-24T12:00:00Z",
            startAt = "2026-07-24T11:00:00Z",
            stopAt = "2026-07-24T12:00:00Z"
        )

        val result = decision.toAlertsListResponseAlert()

        assertEquals(42, result.id)
        assertEquals("uuid-042", result.uuid)
        assertEquals("crowdsec/http-bf", result.scenario)
        assertEquals(true, result.simulated)
        assertEquals(false, result.remediation)
        assertEquals("ES", result.source.cn)
        assertEquals("5.6.7.8", result.source.ip)
        assertNull(result.source.asName)
        assertNull(result.source.asNumber)
        assertNull(result.source.latitude)
        assertNull(result.source.range)
        assertEquals(emptyList<AlertItemMeta>(), result.meta)
        assertEquals(emptyList<AlertEvent>(), result.events)
    }
}
