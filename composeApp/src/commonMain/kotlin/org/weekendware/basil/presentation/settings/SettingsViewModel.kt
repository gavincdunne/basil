package org.weekendware.basil.presentation.settings

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import basil.composeapp.generated.resources.Res
import basil.composeapp.generated.resources.error_save_preference_failed
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.jetbrains.compose.resources.StringResource
import org.weekendware.basil.domain.model.BgUnit
import org.weekendware.basil.domain.usecase.GetBgUnitPreferenceUseCase
import org.weekendware.basil.domain.usecase.SetBgUnitPreferenceUseCase

/**
 * ViewModel for [SettingsScreen].
 *
 * Manages app-level preferences: BG unit selection (persisted) and
 * notification settings placeholder.
 */
class SettingsViewModel(
    private val getBgUnit: GetBgUnitPreferenceUseCase,
    private val setBgUnit: SetBgUnitPreferenceUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState(bgUnit = getBgUnit()))
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    fun onBgUnitChange(unit: BgUnit) {
        setBgUnit(unit)
            .onSuccess { _state.update { it.copy(bgUnit = unit, error = null) } }
            .onFailure { _state.update { it.copy(error = Res.string.error_save_preference_failed) } }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}

@Immutable
data class SettingsState(
    val bgUnit: BgUnit         = BgUnit.MGDL,
    val error: StringResource? = null
)
