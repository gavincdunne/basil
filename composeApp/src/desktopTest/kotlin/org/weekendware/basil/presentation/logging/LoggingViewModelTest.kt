package org.weekendware.basil.presentation.logging

import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.weekendware.basil.data.repository.LogRepository
import org.weekendware.basil.data.repository.PreferencesRepository
import org.weekendware.basil.domain.model.BgUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LoggingViewModelTest {

    private val logRepository         = mock<LogRepository>()
    private val preferencesRepository = mock<PreferencesRepository>()

    private fun makeVm(): LoggingViewModel {
        whenever(preferencesRepository.getBgUnit()).thenReturn(BgUnit.MGDL)
        return LoggingViewModel(logRepository, preferencesRepository)
    }

    // ── initial state ─────────────────────────────────────────

    @Test
    fun `initial bgUnit is loaded from preferences`() {
        whenever(preferencesRepository.getBgUnit()).thenReturn(BgUnit.MMOLL)
        val vm = LoggingViewModel(logRepository, preferencesRepository)

        assertEquals(BgUnit.MMOLL, vm.state.value.bgUnit)
    }

    @Test
    fun `initial form fields are blank`() {
        val vm = makeVm()

        assertEquals("", vm.state.value.bgValue)
        assertEquals("", vm.state.value.insulinUnits)
        assertEquals("", vm.state.value.carbsGrams)
        assertFalse(vm.state.value.hasAnyValue)
    }

    // ── field updates ─────────────────────────────────────────

    @Test
    fun `onBgValueChange updates bgValue in state`() {
        val vm = makeVm()
        vm.onBgValueChange("120")
        assertEquals("120", vm.state.value.bgValue)
    }

    @Test
    fun `onInsulinChange updates insulinUnits in state`() {
        val vm = makeVm()
        vm.onInsulinChange("4")
        assertEquals("4", vm.state.value.insulinUnits)
    }

    @Test
    fun `onCarbsChange updates carbsGrams in state`() {
        val vm = makeVm()
        vm.onCarbsChange("45")
        assertEquals("45", vm.state.value.carbsGrams)
    }

    @Test
    fun `onBgUnitChange updates bgUnit and persists to preferences`() {
        val vm = makeVm()
        vm.onBgUnitChange(BgUnit.MMOLL)

        assertEquals(BgUnit.MMOLL, vm.state.value.bgUnit)
        verify(preferencesRepository).setBgUnit(BgUnit.MMOLL)
    }

    // ── reset ─────────────────────────────────────────────────

    @Test
    fun `reset clears all text fields`() {
        val vm = makeVm()
        vm.onBgValueChange("100")
        vm.onInsulinChange("4")
        vm.onCarbsChange("30")

        vm.reset()

        assertEquals("", vm.state.value.bgValue)
        assertEquals("", vm.state.value.insulinUnits)
        assertEquals("", vm.state.value.carbsGrams)
    }

    @Test
    fun `reset preserves the current bgUnit`() {
        whenever(preferencesRepository.getBgUnit()).thenReturn(BgUnit.MMOLL)
        val vm = LoggingViewModel(logRepository, preferencesRepository)
        vm.reset()

        assertEquals(BgUnit.MMOLL, vm.state.value.bgUnit)
    }

    // ── save ──────────────────────────────────────────────────

    @Test
    fun `save does nothing when form is empty`() {
        val vm = makeVm()
        var callbackInvoked = false

        vm.save { callbackInvoked = true }

        assertFalse(callbackInvoked)
    }

    @Test
    fun `save inserts entry and invokes callback when form has data`() {
        val vm = makeVm()
        vm.onBgValueChange("120")

        var callbackInvoked = false
        vm.save { callbackInvoked = true }

        verify(logRepository).insert(
            bgValue      = 120.0,
            bgUnit       = BgUnit.MGDL,
            insulinUnits = null,
            carbsGrams   = null
        )
        assertTrue(callbackInvoked)
    }

    @Test
    fun `save inserts insulin-only entry with null bgUnit`() {
        val vm = makeVm()
        vm.onInsulinChange("5")

        vm.save {}

        verify(logRepository).insert(
            bgValue      = null,
            bgUnit       = null,
            insulinUnits = 5.0,
            carbsGrams   = null
        )
    }

    @Test
    fun `save resets the form after successful insert`() {
        val vm = makeVm()
        vm.onBgValueChange("100")
        vm.onInsulinChange("4")

        vm.save {}

        assertEquals("", vm.state.value.bgValue)
        assertEquals("", vm.state.value.insulinUnits)
        assertFalse(vm.state.value.hasAnyValue)
    }

    @Test
    fun `save preserves bgUnit after reset`() {
        whenever(preferencesRepository.getBgUnit()).thenReturn(BgUnit.MMOLL)
        val vm = LoggingViewModel(logRepository, preferencesRepository)
        vm.onBgValueChange("5.5")

        vm.save {}

        assertEquals(BgUnit.MMOLL, vm.state.value.bgUnit)
    }
}
