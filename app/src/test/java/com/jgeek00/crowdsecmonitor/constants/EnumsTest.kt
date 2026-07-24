package com.jgeek00.crowdsecmonitor.constants

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EnumsTest {

    @Test
    fun `ThemeMode has expected values`() {
        val values = Enums.ThemeMode.values()
        assertEquals(3, values.size)
        assertTrue(values.contains(Enums.ThemeMode.SYSTEM))
        assertTrue(values.contains(Enums.ThemeMode.LIGHT))
        assertTrue(values.contains(Enums.ThemeMode.DARK))
    }

    @Test
    fun `DashboardBoxSummaryType has expected values`() {
        val values = Enums.DashboardBoxSummaryType.values()
        assertEquals(2, values.size)
        assertEquals(Enums.DashboardBoxSummaryType.ALERTS, Enums.DashboardBoxSummaryType.valueOf("ALERTS"))
        assertEquals(Enums.DashboardBoxSummaryType.DECISIONS, Enums.DashboardBoxSummaryType.valueOf("DECISIONS"))
    }

    @Test
    fun `DashboardItemType has expected values`() {
        val values = Enums.DashboardItemType.values()
        assertEquals(4, values.size)
        assertEquals(Enums.DashboardItemType.COUNTRY, Enums.DashboardItemType.valueOf("COUNTRY"))
        assertEquals(Enums.DashboardItemType.IP_OWNER, Enums.DashboardItemType.valueOf("IP_OWNER"))
        assertEquals(Enums.DashboardItemType.SCENARIO, Enums.DashboardItemType.valueOf("SCENARIO"))
        assertEquals(Enums.DashboardItemType.TARGET, Enums.DashboardItemType.valueOf("TARGET"))
    }

    @Test
    fun `SectionHeaderPaddingTop has expected values`() {
        val values = Enums.SectionHeaderPaddingTop.values()
        assertEquals(3, values.size)
        assertEquals(Enums.SectionHeaderPaddingTop.NONE, Enums.SectionHeaderPaddingTop.valueOf("NONE"))
        assertEquals(Enums.SectionHeaderPaddingTop.SMALL, Enums.SectionHeaderPaddingTop.valueOf("SMALL"))
        assertEquals(Enums.SectionHeaderPaddingTop.NORMAL, Enums.SectionHeaderPaddingTop.valueOf("NORMAL"))
    }

    @Test
    fun `ListType has expected values`() {
        val values = Enums.ListType.values()
        assertEquals(2, values.size)
        assertEquals(Enums.ListType.BLOCKLIST, Enums.ListType.valueOf("BLOCKLIST"))
        assertEquals(Enums.ListType.ALLOWLIST, Enums.ListType.valueOf("ALLOWLIST"))
    }
}
