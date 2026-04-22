package org.weekendware.basil.presentation.logging

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import org.weekendware.basil.domain.model.BgUnit
import org.weekendware.basil.domain.usecase.GetBgUnitPreferenceUseCase
import org.weekendware.basil.domain.usecase.SaveLogEntryUseCase
import org.weekendware.basil.domain.usecase.SetBgUnitPreferenceUseCase

/**
 * Represents the current state of the log-entry form in [LogEntrySheet].
 *
 * All numeric inputs are held as [String] so the user can type freely;
 * conversion to [Double] happens at save time inside [SaveLogEntryUseCase].
 *
 * @property bgValue      Raw text value for the blood glucose field.
 * @property bgUnit       The currently-selected BG unit (mg/dL or mmol/L).
 * @property insulinUnits Raw text value for the insulin units field.
 * @property carbsGrams   Raw text value for the carbohydrates field in grams.
 * @property hasAnyValue  True if at least one field has been filled in, enabling the Save button.
 * @property error        A user-facing error message, or null when there is no error.
 */
data class LogFormState(
    val bgValue: String = "",
    val bgUnit: BgUnit = BgUnit.MGDL,
    val insulinUnits: String = "",
    val carbsGrams: String = "",
    val error: String? = null
) {
    val hasAnyValue: Boolean
        get() = bgValue.isNotBlank() || insulinUnits.isNotBlank() || carbsGrams.isNotBlank()
}

/**
 * ViewModel for the [LogEntrySheet].
 *
 * Manages form state and delegates persistence to use cases. The preferred
 * BG unit is loaded on initialisation so the toggle reflects the user's
 * last choice every time the sheet is opened. Errors from use cases are
 * surfaced via [LogFormState.error].
 */
class LoggingViewModel(
    private val saveLogEntry: SaveLogEntryUseCase,
    private val getBgUnitPreference: GetBgUnitPreferenceUseCase,
    private val setBgUnitPreference: SetBgUnitPreferenceUseCase,
    @Suppress("UnusedPrivateMember")
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
) {
    private val _state = MutableStateFlow(LogFormState())

    /** The current form state, observed by [LogEntrySheet]. */
    val state: StateFlow<LogFormState> = _state

    init {
        _state.update { it.copy(bgUnit = getBgUnitPreference()) }
    }

    /** Updates the blood glucose text field value and clears any existing error. */
    fun onBgValueChange(value: String) = _state.update { it.copy(bgValue = value, error = null) }

    /**
     * Updates the selected BG unit and persists the choice.
     * Sets [LogFormState.error] if the preference write fails.
     *
     * @param unit The newly-selected [BgUnit].
     */
    fun onBgUnitChange(unit: BgUnit) {
        _state.update { it.copy(bgUnit = unit, error = null) }
        setBgUnitPreference(unit).onFailure {
            _state.update { s -> s.copy(error = "Failed to save unit preference") }
        }
    }

    /** Updates the insulin units text field value and clears any existing error. */
    fun onInsulinChange(value: String) = _state.update { it.copy(insulinUnits = value, error = null) }

    /** Updates the carbohydrates text field value and clears any existing error. */
    fun onCarbsChange(value: String) = _state.update { it.copy(carbsGrams = value, error = null) }

    /**
     * Resets the form to its empty state while preserving the selected BG unit.
     *
     * Called when the FAB is tapped so the sheet always opens clean.
     */
    fun reset() {
        _state.update { LogFormState(bgUnit = it.bgUnit) }
    }

    /**
     * Delegates to [SaveLogEntryUseCase]. Invokes [onSuccess] if the entry
     * was saved, no-ops silently when the form is empty, or sets
     * [LogFormState.error] if the save fails.
     *
     * @param onSuccess Callback invoked after a successful save.
     */
    fun save(onSuccess: () -> Unit) {
        val current = _state.value
        saveLogEntry(
            bgValue = current.bgValue,
            bgUnit = current.bgUnit,
            insulinUnits = current.insulinUnits,
            carbsGrams = current.carbsGrams
        ).fold(
            onSuccess = { saved ->
                if (saved) {
                    reset()
                    onSuccess()
                }
            },
            onFailure = {
                _state.update { s -> s.copy(error = "Failed to save entry. Please try again.") }
            }
        )
    }

    /** Clears the current error from state. */
    fun clearError() = _state.update { it.copy(error = null) }
}
