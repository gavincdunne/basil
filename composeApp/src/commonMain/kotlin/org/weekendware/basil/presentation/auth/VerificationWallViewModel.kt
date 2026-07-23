package org.weekendware.basil.presentation.auth

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.weekendware.basil.data.repository.AuthRepository

/**
 * State for [VerificationWallScreen] — the full-screen, non-dismissible
 * soft block shown once a user has gone 30+ days without verifying.
 *
 * @property email       The signed-in user's email, shown so they know
 *   which address to check.
 * @property canResend   False for 60 seconds after a resend, mirroring the
 *   same client-side cooldown used elsewhere for resend actions.
 */
@Immutable
data class VerificationWallUiState(
    val email: String = "",
    val isLoading: Boolean = false,
    val canResend: Boolean = true,
)

/**
 * ViewModel for [VerificationWallScreen]. The wall has exactly two exits:
 * verifying the email (handled outside the app, via the emailed link — this
 * screen just re-checks on next [org.weekendware.basil.presentation.session.SessionViewModel]
 * recomposition) or signing out to try a different account.
 */
class VerificationWallViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(VerificationWallUiState(email = authRepository.currentUserEmail().orEmpty()))
    val state: StateFlow<VerificationWallUiState> = _state

    fun onResend() {
        if (!_state.value.canResend) return
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            authRepository.resendVerificationEmail()
            _state.update { it.copy(isLoading = false, canResend = false) }
            delay(60_000)
            _state.update { it.copy(canResend = true) }
        }
    }

    fun onSignOut() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }
}
