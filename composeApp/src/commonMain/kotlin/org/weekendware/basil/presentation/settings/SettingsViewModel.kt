package org.weekendware.basil.presentation.settings

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * ViewModel for [SettingsScreen].
 *
 * Currently a placeholder. As settings options are implemented (BG unit,
 * notification preferences, theme selection, etc.) this ViewModel will
 * expose state flows for each setting and coordinate persistence via
 * [PreferencesRepository].
 *
 * @param coroutineScope Scope for async operations. Override in tests with a [kotlinx.coroutines.test.TestScope].
 */
class SettingsViewModel(
    @Suppress("UnusedPrivateMember")
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
) {
    private val _title = MutableStateFlow("Settings")

    /** The screen title, displayed in the top app bar. */
    val title: StateFlow<String> = _title
}
