package org.weekendware.basil.presentation.settings

import basil.composeapp.generated.resources.Res
import basil.composeapp.generated.resources.error_save_preference_failed
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.weekendware.basil.data.repository.PreferencesRepository
import org.weekendware.basil.domain.model.BgUnit
import org.weekendware.basil.domain.usecase.GetBgUnitPreferenceUseCase
import org.weekendware.basil.domain.usecase.SetBgUnitPreferenceUseCase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SettingsViewModelTest {

    private val preferencesRepository = mock<PreferencesRepository>()
    private val getBgUnitPreference = GetBgUnitPreferenceUseCase(preferencesRepository)
    private val setBgUnitPreference = SetBgUnitPreferenceUseCase(preferencesRepository)

    private fun makeVm(unit: BgUnit = BgUnit.MGDL): SettingsViewModel {
        whenever(preferencesRepository.getBgUnit()).thenReturn(unit)
        return SettingsViewModel(getBgUnitPreference, setBgUnitPreference)
    }

    // ── initial state ─────────────────────────────────────────

    @Test
    fun `initial bgUnit is loaded from preferences`() {
        val vm = makeVm(BgUnit.MMOLL)
        assertEquals(BgUnit.MMOLL, vm.state.value.bgUnit)
    }

    @Test
    fun `initial error is null`() {
        val vm = makeVm()
        assertNull(vm.state.value.error)
    }

    // ── BG unit change ────────────────────────────────────────

    @Test
    fun `onBgUnitChange updates bgUnit in state`() {
        val vm = makeVm(BgUnit.MGDL)
        vm.onBgUnitChange(BgUnit.MMOLL)
        assertEquals(BgUnit.MMOLL, vm.state.value.bgUnit)
    }

    @Test
    fun `onBgUnitChange persists unit to preferences`() {
        val vm = makeVm()
        vm.onBgUnitChange(BgUnit.MMOLL)
        verify(preferencesRepository).setBgUnit(BgUnit.MMOLL)
    }

    @Test
    fun `onBgUnitChange sets error when preference write fails`() {
        val vm = makeVm()
        doThrow(RuntimeException("DB error")).whenever(preferencesRepository).setBgUnit(BgUnit.MMOLL)

        vm.onBgUnitChange(BgUnit.MMOLL)

        assertEquals(Res.string.error_save_preference_failed, vm.state.value.error)
    }

    @Test
    fun `onBgUnitChange does not update bgUnit in state when write fails`() {
        val vm = makeVm(BgUnit.MGDL)
        doThrow(RuntimeException("DB error")).whenever(preferencesRepository).setBgUnit(BgUnit.MMOLL)

        vm.onBgUnitChange(BgUnit.MMOLL)

        assertEquals(BgUnit.MGDL, vm.state.value.bgUnit)
    }

    // ── clearError ────────────────────────────────────────────

    @Test
    fun `clearError removes error from state`() {
        val vm = makeVm()
        doThrow(RuntimeException("DB error")).whenever(preferencesRepository).setBgUnit(BgUnit.MMOLL)
        vm.onBgUnitChange(BgUnit.MMOLL)
        assertEquals(Res.string.error_save_preference_failed, vm.state.value.error)

        vm.clearError()

        assertNull(vm.state.value.error)
    }
}
