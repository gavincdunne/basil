package org.weekendware.basil.presentation.settings

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.weekendware.basil.data.repository.AuthRepository

/**
 * @property isSigningOut True while [SettingsViewModel.onSignOut]'s repository
 *   call is in flight.
 */
@Immutable
data class SettingsState(
    val isSigningOut: Boolean = false,
)

/**
 * ViewModel for [SettingsScreen].
 *
 * **Not implemented here.** QA scaffolding only — see
 * [SettingsViewModelTest] for the full contract. Spec:
 * `spec-splash-auth-07222026.md`, AC24.
 */
class SettingsViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state

    /**
     * Signs the current user out. Navigation back to the unauthenticated
     * flow happens reactively — [org.weekendware.basil.presentation.session.SessionViewModel]
     * observes [AuthRepository.sessionFlow] and routes automatically once
     * it flips to unauthenticated. No explicit navigation call needed here.
     */
    fun onSignOut() {
        TODO("Not yet implemented — see spec-splash-auth-07222026.md, AC24")
    }
}
