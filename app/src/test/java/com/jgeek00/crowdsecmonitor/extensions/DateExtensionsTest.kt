package com.jgeek00.crowdsecmonitor.extensions

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant

/**
 * Verifies timestamp parsing against the API canonical format
 * (`YYYY-MM-DD HH:MM:SS ±ZZZZ ZZZ`) — see specs/006-fix-timestamp-format.
 */
class DateExtensionsTest {

    @Test
    fun toInstant_parsesCanonicalDuplicatedOffset() {
        assertEquals(
            Instant.parse("2026-09-24T14:19:29Z"),
            "2026-09-24 16:19:29 +0200 +0200".toInstant()
        )
    }

    @Test
    fun toInstant_parsesCanonicalForeignOffset() {
        assertEquals(
            Instant.parse("2026-09-23T12:31:28Z"),
            "2026-09-23 20:31:28 +0800 +0800".toInstant()
        )
    }

    @Test
    fun toInstant_parsesCanonicalNegativeOffset() {
        assertEquals(
            Instant.parse("2026-09-23T19:31:28Z"),
            "2026-09-23 14:31:28 -0500 -0500".toInstant()
        )
    }

    @Test
    fun toInstant_parsesIsoUtc() {
        assertEquals(
            Instant.parse("2026-07-23T00:00:00Z"),
            "2026-07-23T00:00:00Z".toInstant()
        )
    }

    @Test
    fun toInstant_returnsNullOnGarbage() {
        assertNull("not-a-timestamp".toInstant())
    }
}
