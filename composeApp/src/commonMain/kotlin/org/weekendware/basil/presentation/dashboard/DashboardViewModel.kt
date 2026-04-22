package org.weekendware.basil.presentation.dashboard

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import org.weekendware.basil.domain.model.LogEntry
import org.weekendware.basil.domain.usecase.GetLastBgReadingUseCase
import org.weekendware.basil.domain.usecase.GetTodayEntriesUseCase
import org.weekendware.basil.domain.usecase.ObserveRecentLogsUseCase

/**
 * UI state for the [DashboardScreen].
 *
 * @property todayEntries All log entries recorded today, ordered newest first.
 * @property lastBgEntry  The most recent entry that contains a blood glucose
 *   value, regardless of whether it was recorded today.
 */
data class DashboardState(
    val todayEntries: List<LogEntry> = emptyList(),
    val lastBgEntry: LogEntry? = null
)

/**
 * ViewModel for [DashboardScreen].
 *
 * Delegates all data access and business logic to use cases. The [state]
 * Flow updates automatically whenever the underlying log table changes.
 */
class DashboardViewModel(
    observeRecentLogs: ObserveRecentLogsUseCase,
    private val getTodayEntries: GetTodayEntriesUseCase,
    private val getLastBgReading: GetLastBgReadingUseCase,
    coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
) {

    private val _showLogSheet = MutableStateFlow(false)

    /**
     * Whether the log-entry bottom sheet is currently visible.
     * Observed by [DashboardScreen] to conditionally compose [LogEntrySheet].
     */
    val showLogSheet: StateFlow<Boolean> = _showLogSheet

    /**
     * The current dashboard UI state, derived live from the repository Flow.
     * Emits a new [DashboardState] whenever the underlying log table changes.
     */
    val state: StateFlow<DashboardState> = observeRecentLogs()
        .map { recent ->
            DashboardState(
                todayEntries = getTodayEntries(recent),
                lastBgEntry = getLastBgReading(recent)
            )
        }
        .stateIn(
            scope = coroutineScope,
            started = SharingStarted.Eagerly,
            initialValue = DashboardState()
        )

    /** Opens the log-entry bottom sheet. */
    fun openLogSheet() = _showLogSheet.update { true }

    /** Closes the log-entry bottom sheet. */
    fun closeLogSheet() = _showLogSheet.update { false }
}
