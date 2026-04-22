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
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.weekendware.basil.data.repository.LogRepository
import org.weekendware.basil.domain.model.LogEntry

/**
 * UI state for the [DashboardScreen].
 *
 * @property todayEntries All log entries recorded today, ordered newest first.
 * @property lastBgEntry  The most recent entry that contains a blood glucose
 *   value, regardless of whether it was recorded today. Used to populate the
 *   summary card at the top of the dashboard.
 */
data class DashboardState(
    val todayEntries: List<LogEntry> = emptyList(),
    val lastBgEntry: LogEntry? = null
)

/**
 * ViewModel for [DashboardScreen].
 *
 * Observes [LogRepository.getRecent] as a [kotlinx.coroutines.flow.Flow] so the
 * dashboard updates automatically whenever an entry is inserted or deleted —
 * no manual refresh calls required.
 *
 * @param logRepository Source of truth for all log entry data.
 * @param coroutineScope Scope used to collect the repository Flow. Defaults to
 *   an app-lifetime scope; override in tests with a [kotlinx.coroutines.test.TestScope].
 */
class DashboardViewModel(
    private val logRepository: LogRepository,
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
    val state: StateFlow<DashboardState> = logRepository.getRecent(100)
        .map { recent ->
            DashboardState(
                todayEntries = recent.filter { it.isToday() },
                lastBgEntry = recent.firstOrNull { it.bgValue != null }
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

    // ── Helpers ───────────────────────────────────────────────

    private fun LogEntry.isToday(): Boolean {
        val tz = TimeZone.currentSystemDefault()
        val today = Clock.System.now().toLocalDateTime(tz).date
        val entryDate = Instant.fromEpochMilliseconds(timestamp).toLocalDateTime(tz).date
        return entryDate == today
    }
}
