package org.weekendware.basil.presentation.dashboard

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class DashboardViewModel {
    private val _showLogSheet = MutableStateFlow(false)
    val showLogSheet: StateFlow<Boolean> = _showLogSheet

    fun openLogSheet() = _showLogSheet.update { true }
    fun closeLogSheet() = _showLogSheet.update { false }
}
