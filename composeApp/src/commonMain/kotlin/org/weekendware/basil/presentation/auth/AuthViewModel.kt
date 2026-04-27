package org.weekendware.basil.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.weekendware.basil.data.repository.AuthRepository

/**
 * Form state for the authentication screen.
 *
 * @property email        Current value of the email field.
 * @property password     Current value of the password field.
 * @property isSignUp     When true the form submits as sign-up; false = sign-in.
 * @property isLoading    True while an auth network call is in flight.
 * @property error        User-facing error message, or null when there is none.
 */
data class AuthFormState(
    val email: String = "",
    val password: String = "",
    val isSignUp: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
) {
    val canSubmit: Boolean get() = email.isNotBlank() && password.length >= 6 && !isLoading
}

/**
 * ViewModel for [AuthScreen].
 *
 * Delegates credential operations to [AuthRepository] and surfaces
 * results through [state]. Navigation after a successful auth is handled
 * reactively by [SessionViewModel] — no explicit success callback is needed.
 */
class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AuthFormState())

    /** The current form state observed by [AuthScreen]. */
    val state: StateFlow<AuthFormState> = _state

    fun onEmailChange(value: String) = _state.update { it.copy(email = value, error = null) }
    fun onPasswordChange(value: String) = _state.update { it.copy(password = value, error = null) }
    fun toggleMode() = _state.update { it.copy(isSignUp = !it.isSignUp, error = null) }

    /** Submits sign-in or sign-up depending on [AuthFormState.isSignUp]. */
    fun submit() {
        val current = _state.value
        if (!current.canSubmit) return

        _state.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            val result = if (current.isSignUp) {
                authRepository.signUp(current.email, current.password)
            } else {
                authRepository.signIn(current.email, current.password)
            }

            result.fold(
                onSuccess = {
                    // SessionViewModel will observe the auth state change and
                    // navigate automatically — nothing to do here.
                    _state.update { it.copy(isLoading = false) }
                },
                onFailure = { error ->
                    _state.update { it.copy(isLoading = false, error = error.message ?: "Authentication failed") }
                }
            )
        }
    }
}
