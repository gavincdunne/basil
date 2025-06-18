package org.weekendware.basil.presentation.settings

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SettingsViewModel {
    private val _title = MutableStateFlow("Settings")
    val title: StateFlow<String> = _title
}