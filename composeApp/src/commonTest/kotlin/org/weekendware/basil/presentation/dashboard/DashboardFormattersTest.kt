package org.weekendware.basil.presentation.dashboard

import org.weekendware.basil.domain.model.BgUnit
import kotlin.test.Test
import kotlin.test.assertEquals

class DashboardFormattersTest {

    // ── glucoseStatus ─────────────────────────────────────────

    @Test
    fun `glucoseStatus returns VERY_LOW below 54 mgdl`() {
        assertEquals(GlucoseStatus.VERY_LOW, glucoseStatus(40.0, BgUnit.MGDL))
        assertEquals(GlucoseStatus.VERY_LOW, glucoseStatus(53.9, BgUnit.MGDL))
    }

    @Test
    fun `glucoseStatus returns LOW between 54 and 69 mgdl`() {
        assertEquals(GlucoseStatus.LOW, glucoseStatus(54.0, BgUnit.MGDL))
        assertEquals(GlucoseStatus.LOW, glucoseStatus(65.0, BgUnit.MGDL))
        assertEquals(GlucoseStatus.LOW, glucoseStatus(69.9, BgUnit.MGDL))
    }

    @Test
    fun `glucoseStatus returns IN_RANGE between 70 and 180 mgdl`() {
        assertEquals(GlucoseStatus.IN_RANGE, glucoseStatus(70.0, BgUnit.MGDL))
        assertEquals(GlucoseStatus.IN_RANGE, glucoseStatus(100.0, BgUnit.MGDL))
        assertEquals(GlucoseStatus.IN_RANGE, glucoseStatus(180.0, BgUnit.MGDL))
    }

    @Test
    fun `glucoseStatus returns HIGH between 181 and 250 mgdl`() {
        assertEquals(GlucoseStatus.HIGH, glucoseStatus(181.0, BgUnit.MGDL))
        assertEquals(GlucoseStatus.HIGH, glucoseStatus(220.0, BgUnit.MGDL))
        assertEquals(GlucoseStatus.HIGH, glucoseStatus(250.0, BgUnit.MGDL))
    }

    @Test
    fun `glucoseStatus returns VERY_HIGH above 250 mgdl`() {
        assertEquals(GlucoseStatus.VERY_HIGH, glucoseStatus(251.0, BgUnit.MGDL))
        assertEquals(GlucoseStatus.VERY_HIGH, glucoseStatus(400.0, BgUnit.MGDL))
    }

    @Test
    fun `glucoseStatus converts mmolL to mgdl before classifying`() {
        // 5.5 mmol/L * 18.016 ≈ 99.1 mg/dL → IN_RANGE
        assertEquals(GlucoseStatus.IN_RANGE, glucoseStatus(5.5, BgUnit.MMOLL))
        // 3.0 mmol/L * 18.016 ≈ 54.0 mg/dL → LOW
        assertEquals(GlucoseStatus.LOW, glucoseStatus(3.0, BgUnit.MMOLL))
        // 2.9 mmol/L * 18.016 ≈ 52.2 mg/dL → VERY_LOW
        assertEquals(GlucoseStatus.VERY_LOW, glucoseStatus(2.9, BgUnit.MMOLL))
    }

    // ── toFormattedValue ──────────────────────────────────────

    @Test
    fun `toFormattedValue returns integer string for whole numbers`() {
        assertEquals("120", 120.0.toFormattedValue())
        assertEquals("0", 0.0.toFormattedValue())
        assertEquals("5", 5.0.toFormattedValue())
    }

    @Test
    fun `toFormattedValue returns one decimal place for fractional numbers`() {
        assertEquals("5.5", 5.5.toFormattedValue())
        assertEquals("10.3", 10.3.toFormattedValue())
        assertEquals("99.9", 99.9.toFormattedValue())
    }

    @Test
    fun `toFormattedValue handles negative fractional numbers`() {
        assertEquals("-5.5", (-5.5).toFormattedValue())
    }

    // ── GlucoseStatus labels ──────────────────────────────────

    @Test
    fun `GlucoseStatus labels are human readable`() {
        assertEquals("Very Low",  GlucoseStatus.VERY_LOW.label)
        assertEquals("Low",       GlucoseStatus.LOW.label)
        assertEquals("In Range",  GlucoseStatus.IN_RANGE.label)
        assertEquals("High",      GlucoseStatus.HIGH.label)
        assertEquals("Very High", GlucoseStatus.VERY_HIGH.label)
    }
}
