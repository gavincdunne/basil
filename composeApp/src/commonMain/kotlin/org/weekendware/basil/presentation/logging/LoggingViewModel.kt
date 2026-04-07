package org.weekendware.basil.presentation.logging

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import org.weekendware.basil.data.repository.LogRepository
import org.weekendware.basil.data.repository.PreferencesRepository
import org.weekendware.basil.domain.model.BgUnit

data class LogFormState(
    val bgValue: String = "",
    val bgUnit: BgUnit = BgUnit.MGDL,
    val insulinUnits: String = "",
    val carbsGrams: String = ""
) {
    val hasAnyValue: Boolean
        get() = bgValue.isNotBlank() || insulinUnits.isNotBlank() || carbsGrams.isNotBlank()
}

class LoggingViewModel(
    private val logRepository: LogRepository,
    private val preferencesRepository: PreferencesRepository
) {
    private val _state = MutableStateFlow(LogFormState())
    val state: StateFlow<LogFormState> = _state

    init {
        _state.update { it.copy(bgUnit = preferencesRepository.getBgUnit()) }
    }

    fun onBgValueChange(value: String) = _state.update { it.copy(bgValue = value) }

    fun onBgUnitChange(unit: BgUnit) {
        _state.update { it.copy(bgUnit = unit) }
        preferencesRepository.setBgUnit(unit)
    }

    fun onInsulinChange(value: String) = _state.update { it.copy(insulinUnits = value) }

    fun onCarbsChange(value: String) = _state.update { it.copy(carbsGrams = value) }

    fun reset() {
        _state.update { LogFormState(bgUnit = it.bgUnit) }
    }

    fun save(onSuccess: () -> Unit) {
        val current = _state.value
        if (!current.hasAnyValue) return

        logRepository.insert(
            bgValue = current.bgValue.toDoubleOrNull(),
            bgUnit = if (current.bgValue.isNotBlank()) current.bgUnit else null,
            insulinUnits = current.insulinUnits.toDoubleOrNull(),
            carbsGrams = current.carbsGrams.toDoubleOrNull()
        )
        reset()
        onSuccess()
    }
}
