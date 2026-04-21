package org.weekendware.basil.presentation.dashboard

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
    val lastBgEntry:  LogEntry?      = null
)

/**
 * ViewModel for [DashboardScreen].
 *
 * Loads today's log entries from [LogRepository] and derives the most recent
 * blood glucose reading for the summary card. Call [refresh] after a new entry
 * is saved to keep the dashboard in sync.
 *
 * @param logRepository Source of truth for all log entry data.
 */
class DashboardViewModel(private val logRepository: LogRepository) {

    private val _showLogSheet = MutableStateFlow(false)

    /**
     * Whether the log-entry bottom sheet is currently visible.
     * Observed by [DashboardScreen] to conditionally compose [LogEntrySheet].
     */
    val showLogSheet: StateFlow<Boolean> = _showLogSheet

    private val _state = MutableStateFlow(DashboardState())

    /** The current dashboard UI state, observed by [DashboardScreen]. */
    val state: StateFlow<DashboardState> = _state

    init {
        refresh()
    }

    /** Opens the log-entry bottom sheet. */
    fun openLogSheet() = _showLogSheet.update { true }

    /** Closes the log-entry bottom sheet. */
    fun closeLogSheet() = _showLogSheet.update { false }

    /**
     * Reloads log entries from the database and updates [state].
     *
     * Call this after a new entry is saved so the dashboard reflects the
     * latest data without requiring a full recomposition cycle.
     */
    fun refresh() {
        val recent      = logRepository.getRecent(100)
        val todayEntries = recent.filter { it.isToday() }
        val lastBgEntry  = recent.firstOrNull { it.bgValue != null }
        _state.update { DashboardState(todayEntries = todayEntries, lastBgEntry = lastBgEntry) }
    }

    // ── Helpers ───────────────────────────────────────────────

    private fun LogEntry.isToday(): Boolean {
        val tz    = TimeZone.currentSystemDefault()
        val today = Clock.System.now().toLocalDateTime(tz).date
        val entryDate = Instant.fromEpochMilliseconds(timestamp).toLocalDateTime(tz).date
        return entryDate == today
    }
}
