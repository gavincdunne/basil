package org.weekendware.basil.presentation.settings

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import basil.composeapp.generated.resources.Res
import basil.composeapp.generated.resources.error_auth_failed
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.weekendware.basil.data.repository.AuthRepository

/**
 * @property isSigningOut True while [SettingsViewModel.onSignOut]'s repository
 *   call is in flight.
 * @property error User-facing error string resource when [SettingsViewModel.onSignOut]'s
 *   repository call fails, or null when there is none.
 */
@Immutable
data class SettingsState(
    val isSigningOut: Boolean = false,
    val error: StringResource? = null,
)

/**
 * ViewModel for [SettingsScreen]. Spec: `spec-splash-auth-07222026.md`,
 * AC24.
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
        _state.update { it.copy(isSigningOut = true, error = null) }
        viewModelScope.launch {
            authRepository.signOut().fold(
                onSuccess = { _state.update { it.copy(isSigningOut = false) } },
                onFailure = { _state.update { it.copy(isSigningOut = false, error = Res.string.error_auth_failed) } }
            )
        }
    }
}
