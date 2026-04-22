package org.weekendware.basil.presentation.logging

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import org.weekendware.basil.data.repository.LogRepository
import org.weekendware.basil.data.repository.PreferencesRepository
import org.weekendware.basil.domain.model.BgUnit

/**
 * Represents the current state of the log-entry form in [LogEntrySheet].
 *
 * All numeric inputs are held as [String] so the user can type freely;
 * conversion to [Double] happens at save time.
 *
 * @property bgValue      Raw text value for the blood glucose field. Empty means not entered.
 * @property bgUnit       The currently-selected BG unit (mg/dL or mmol/L).
 * @property insulinUnits Raw text value for the insulin units field. Empty means not entered.
 * @property carbsGrams   Raw text value for the carbohydrates field in grams. Empty means not entered.
 * @property hasAnyValue  True if at least one field has been filled in, enabling the Save button.
 */
data class LogFormState(
    val bgValue:      String  = "",
    val bgUnit:       BgUnit  = BgUnit.MGDL,
    val insulinUnits: String  = "",
    val carbsGrams:   String  = ""
) {
    val hasAnyValue: Boolean
        get() = bgValue.isNotBlank() || insulinUnits.isNotBlank() || carbsGrams.isNotBlank()
}

/**
 * ViewModel for the [LogEntrySheet].
 *
 * Manages form state, persists the user's preferred BG unit via
 * [PreferencesRepository], and delegates entry saving to [LogRepository].
 *
 * The preferred BG unit is loaded from [PreferencesRepository] on
 * initialisation so the toggle reflects the user's last choice every time
 * the sheet is opened.
 *
 * @param logRepository         Persists log entries to the local database.
 * @param preferencesRepository Persists and retrieves user preferences.
 */
class LoggingViewModel(
    private val logRepository: LogRepository,
    private val preferencesRepository: PreferencesRepository,
    @Suppress("UnusedPrivateMember")
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
) {
    private val _state = MutableStateFlow(LogFormState())

    /** The current form state, observed by [LogEntrySheet]. */
    val state: StateFlow<LogFormState> = _state

    init {
        // Restore the user's last-selected BG unit so the toggle is consistent
        // across sheet open/close cycles.
        _state.update { it.copy(bgUnit = preferencesRepository.getBgUnit()) }
    }

    /** Updates the blood glucose text field value. */
    fun onBgValueChange(value: String) = _state.update { it.copy(bgValue = value) }

    /**
     * Updates the selected BG unit and persists the choice so it is
     * remembered the next time the sheet is opened.
     *
     * @param unit The newly-selected [BgUnit].
     */
    fun onBgUnitChange(unit: BgUnit) {
        _state.update { it.copy(bgUnit = unit) }
        preferencesRepository.setBgUnit(unit)
    }

    /** Updates the insulin units text field value. */
    fun onInsulinChange(value: String) = _state.update { it.copy(insulinUnits = value) }

    /** Updates the carbohydrates text field value. */
    fun onCarbsChange(value: String) = _state.update { it.copy(carbsGrams = value) }

    /**
     * Resets the form to its empty state while preserving the selected BG unit.
     *
     * Called by [DashboardScreen] when the FAB is tapped, ensuring the sheet
     * always opens clean without stale values from a previous session.
     */
    fun reset() {
        _state.update { LogFormState(bgUnit = it.bgUnit) }
    }

    /**
     * Validates the current form state and, if at least one field is populated,
     * saves a new [LogEntry] to the database, resets the form, and invokes [onSuccess].
     *
     * A BG unit is only written to the log if a BG value was actually entered.
     * No-ops silently when [LogFormState.hasAnyValue] is false.
     *
     * @param onSuccess Callback invoked after a successful save; typically dismisses the sheet.
     */
    fun save(onSuccess: () -> Unit) {
        val current = _state.value
        if (!current.hasAnyValue) return

        logRepository.insert(
            bgValue      = current.bgValue.toDoubleOrNull(),
            bgUnit       = if (current.bgValue.isNotBlank()) current.bgUnit else null,
            insulinUnits = current.insulinUnits.toDoubleOrNull(),
            carbsGrams   = current.carbsGrams.toDoubleOrNull()
        )
        reset()
        onSuccess()
    }
}
