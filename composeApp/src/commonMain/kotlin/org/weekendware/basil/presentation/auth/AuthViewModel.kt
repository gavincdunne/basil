package org.weekendware.basil.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import androidx.compose.runtime.Immutable
import basil.composeapp.generated.resources.Res
import basil.composeapp.generated.resources.error_auth_failed
import org.jetbrains.compose.resources.StringResource
import org.weekendware.basil.data.repository.AuthRepository

/** Whether the auth screen is presenting a returning user's sign-in, or a new user's path to onboarding. */
enum class AuthMode { SignIn, SignUp }

/**
 * State for the email-first auth screen.
 *
 * @property mode              Whether the entered email matched a stored one (SignIn) or not (SignUp).
 * @property email             Current value of the email field.
 * @property password          Current value of the password field. Unused while [emailStep] is true.
 * @property emailStep         True while the user is on the email-only step; false once past it.
 * @property isLoading         True while an auth network call is in flight.
 * @property isPasswordVisible True when the password field shows plain text instead of masked dots.
 * @property error             User-facing error string resource, or null when there is none.
 */
@Immutable
data class AuthUiState(
    val mode: AuthMode = AuthMode.SignIn,
    val email: String = "",
    val password: String = "",
    val emailStep: Boolean = true,
    val isLoading: Boolean = false,
    val isPasswordVisible: Boolean = false,
    val error: StringResource? = null,
) {
    val canContinueEmail: Boolean get() = email.isNotBlank() && !isLoading
    val canSubmit: Boolean get() = password.isNotBlank() && !isLoading
}

/**
 * ViewModel for [AuthScreen].
 *
 * Email-first, detect-by-email flow: the user enters an email and taps
 * Continue; if it matches [AuthRepository.lastUsedEmail] (the only
 * detection signal available — Supabase's enumeration protection rules out
 * a real server-side lookup) the screen advances to a password step,
 * otherwise it shows the no-account-found state. Delegates credential
 * operations to [AuthRepository] and surfaces results through [state].
 * Navigation after a successful auth is handled reactively by
 * [org.weekendware.basil.presentation.session.SessionViewModel] — no
 * explicit success callback is needed.
 */
class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())

    /** The current form state observed by [AuthScreen]. */
    val state: StateFlow<AuthUiState> = _state

    fun onEmailChange(value: String) = _state.update { it.copy(email = value, error = null) }
    fun onPasswordChange(value: String) = _state.update { it.copy(password = value, error = null) }
    fun onTogglePasswordVisibility() = _state.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }

    /** Advances from the email step, choosing SignIn or SignUp mode via the detect-by-email heuristic. */
    fun onContinueEmail() {
        val current = _state.value
        if (!current.canContinueEmail) return
        val recognized = current.email == authRepository.lastUsedEmail()
        _state.update {
            it.copy(emailStep = false, mode = if (recognized) AuthMode.SignIn else AuthMode.SignUp)
        }
    }

    /** Returns to a fresh email step, discarding the entered email and password. */
    fun onUseDifferentAccount() = _state.update { AuthUiState() }

    /** Submits sign-in with the current email/password. No-op outside [AuthMode.SignIn] — this screen never signs up directly. */
    fun submit() {
        val current = _state.value
        if (current.mode != AuthMode.SignIn || !current.canSubmit) return

        _state.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            val result = authRepository.signIn(current.email, current.password)
            result.fold(
                onSuccess = { _state.update { it.copy(isLoading = false) } },
                onFailure = { _state.update { it.copy(isLoading = false, error = Res.string.error_auth_failed) } }
            )
        }
    }

    fun onGoogleSignIn() = signInWithProvider(authRepository::signInWithGoogle)
    fun onAppleSignIn() = signInWithProvider(authRepository::signInWithApple)

    private fun signInWithProvider(provider: suspend () -> Result<Unit>) {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            val result = provider()
            result.fold(
                onSuccess = { _state.update { it.copy(isLoading = false) } },
                onFailure = { _state.update { it.copy(isLoading = false, error = Res.string.error_auth_failed) } }
            )
        }
    }
}
