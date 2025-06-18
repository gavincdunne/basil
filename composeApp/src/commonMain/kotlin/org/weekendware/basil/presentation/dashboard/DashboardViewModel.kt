package org.weekendware.basil.presentation.dashboard

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class DashboardViewModel {
    private val _title = MutableStateFlow("Dashboard")
    val title: StateFlow<String> = _title
}