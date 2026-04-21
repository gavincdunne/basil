package org.weekendware.basil.presentation.logging

import org.weekendware.basil.domain.model.BgUnit
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.assertEquals

class LogFormStateTest {

    @Test
    fun `hasAnyValue is false when all fields are blank`() {
        val state = LogFormState()
        assertFalse(state.hasAnyValue)
    }

    @Test
    fun `hasAnyValue is true when bgValue is filled`() {
        val state = LogFormState(bgValue = "120")
        assertTrue(state.hasAnyValue)
    }

    @Test
    fun `hasAnyValue is true when insulinUnits is filled`() {
        val state = LogFormState(insulinUnits = "4")
        assertTrue(state.hasAnyValue)
    }

    @Test
    fun `hasAnyValue is true when carbsGrams is filled`() {
        val state = LogFormState(carbsGrams = "45")
        assertTrue(state.hasAnyValue)
    }

    @Test
    fun `hasAnyValue is false when fields contain only whitespace`() {
        val state = LogFormState(bgValue = "  ", insulinUnits = "\t", carbsGrams = " ")
        assertFalse(state.hasAnyValue)
    }

    @Test
    fun `default bgUnit is MGDL`() {
        assertEquals(BgUnit.MGDL, LogFormState().bgUnit)
    }

    @Test
    fun `copy preserves all fields correctly`() {
        val state = LogFormState(bgValue = "100", bgUnit = BgUnit.MMOLL, insulinUnits = "3", carbsGrams = "30")
        val copy = state.copy(bgValue = "110")
        assertEquals("110",       copy.bgValue)
        assertEquals(BgUnit.MMOLL, copy.bgUnit)
        assertEquals("3",          copy.insulinUnits)
        assertEquals("30",         copy.carbsGrams)
    }
}
