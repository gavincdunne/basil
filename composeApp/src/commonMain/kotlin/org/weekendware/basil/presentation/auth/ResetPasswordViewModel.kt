package org.weekendware.basil.presentation.auth

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
 * State for the password reset request screen.
 *
 * @property email     Email the reset link is sent to — prefilled from the sign-in screen.
 * @property isSent     True once a reset email has been sent for the current [email].
 * @property isLoading  True while the send/resend network call is in flight.
 * @property error      User-facing error string resource, or null when there is none.
 */
@Immutable
data class ResetPasswordUiState(
    val email: String = "",
    val isSent: Boolean = false,
    val isLoading: Boolean = false,
    val error: StringResource? = null,
) {
    val canSend: Boolean get() = email.isNotBlank() && !isLoading
}

/**
 * ViewModel for the reset-request screen (AUTH-05 request / AUTH-06 sent).
 * "Resend email" on the sent state re-uses [sendResetLink].
 */
class ResetPasswordViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ResetPasswordUiState())
    val state: StateFlow<ResetPasswordUiState> = _state

    fun onEmailChange(value: String) = _state.update { it.copy(email = value, error = null) }

    fun sendResetLink() {
        val current = _state.value
        if (!current.canSend) return

        _state.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            val result = authRepository.resetPassword(current.email)
            result.fold(
                onSuccess = { _state.update { it.copy(isLoading = false, isSent = true) } },
                onFailure = { _state.update { it.copy(isLoading = false, error = Res.string.error_auth_failed) } }
            )
        }
    }
}
