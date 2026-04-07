package org.weekendware.basil.presentation.dashboard

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

/**
 * ViewModel for the [DashboardScreen].
 *
 * Manages UI state that is scoped to the dashboard, currently limited to
 * controlling the visibility of the log-entry bottom sheet. As the dashboard
 * grows (recent log list, glucose summaries, trend indicators), this ViewModel
 * will be expanded to expose that state via additional [StateFlow]s.
 */
class DashboardViewModel {

    private val _showLogSheet = MutableStateFlow(false)

    /**
     * Whether the log-entry bottom sheet is currently visible.
     * Observed by [DashboardScreen] to conditionally compose [LogEntrySheet].
     */
    val showLogSheet: StateFlow<Boolean> = _showLogSheet

    /** Opens the log-entry bottom sheet. */
    fun openLogSheet() = _showLogSheet.update { true }

    /** Closes the log-entry bottom sheet. */
    fun closeLogSheet() = _showLogSheet.update { false }
}
