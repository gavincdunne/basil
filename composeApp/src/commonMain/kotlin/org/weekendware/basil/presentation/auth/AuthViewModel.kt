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
import io.github.jan.supabase.compose.auth.composable.NativeSignInResult
import org.jetbrains.compose.resources.StringResource
import org.weekendware.basil.data.repository.AuthRepository

/** Whether the auth screen is presenting a returning user's sign-in, or a new user's path to onboarding. */
enum class AuthMode { SignIn, SignUp }

/**
 * State for the email-first auth screen.
 *
 * @property mode                  Whether the entered email matched a stored one (SignIn) or not (SignUp).
 * @property email                 Current value of the email field.
 * @property password              Current value of the password field. Unused while [emailStep] is true.
 * @property emailStep             True while the user is on the email-only step; false once past it.
 * @property isLoading             True while an auth network call is in flight.
 * @property isPasswordVisible     True when the password field shows plain text instead of masked dots.
 * @property error                 User-facing error string resource, or null when there is none.
 * @property isPasskeyAvailable    True if this device has an enrolled passkey. While true (and
 *   [biometricAttemptCount] is under 2) the biometric prompt replaces the email step entirely.
 * @property biometricAttemptCount 0 = auto-triggering/scanning, 1 = first failure (retry offered),
 *   2 = second failure ([isPasskeyAvailable] is set false and the standard form takes over).
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
    val isPasskeyAvailable: Boolean = false,
    val biometricAttemptCount: Int = 0,
) {
    val canContinueEmail: Boolean get() = email.isNotBlank() && !isLoading
    val canSubmit: Boolean get() = password.isNotBlank() && !isLoading
    val showPasskeyPrompt: Boolean get() = isPasskeyAvailable && biometricAttemptCount < 2
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

    private val _state = MutableStateFlow(
        AuthUiState(isPasskeyAvailable = authRepository.hasPasskeyEnrolled())
    )

    /** The current form state observed by [AuthScreen]. */
    val state: StateFlow<AuthUiState> = _state

    /**
     * Called from a `LaunchedEffect(Unit)` the instant the biometric prompt
     * composes — fires automatically, no button tap. Also what "Try again"
     * calls directly on the first failure. On a second failure, falls
     * through to the standard form with a contextual error message.
     *
     * **Not implemented here.** QA scaffolding only — see
     * [AuthViewModelTest] for the full contract (attempt-count transitions,
     * the second-failure fallback message, and the successful-auth case).
     */
    fun onPasskeyScreenEntered() {
        TODO("Not yet implemented — see tdd-splash-auth-07222026.md, AC18b/19b biometric attempt states")
    }

    /** "Try again" on the first failure — re-invokes the ceremony directly, same as the auto-trigger. */
    fun onPasskeyRetry() {
        TODO("Not yet implemented — see tdd-splash-auth-07222026.md, AC18b/19b biometric attempt states")
    }

    /** Escape hatch from the biometric prompt to the standard form, without recording it as a failure. */
    fun onUseDifferentAccountFromPasskey() = _state.update { it.copy(isPasskeyAvailable = false) }

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

    /**
     * Called right before `.startFlow()` on compose-auth's remembered
     * Google/Apple sign-in action, from [AuthScreen]. The actual OAuth or
     * native-credential ceremony runs entirely inside that composable,
     * outside this ViewModel and outside [AuthRepository] — this only
     * manages the loading/error state around it.
     */
    fun onSocialSignInStarted() = _state.update { it.copy(isLoading = true, error = null) }

    /**
     * The `onResult` callback passed to compose-auth's
     * `rememberSignInWithGoogle`/`rememberSignInWithApple`. One handler for
     * both providers — the state transitions are identical regardless of
     * which one the user tapped.
     */
    fun onSocialSignInResult(result: NativeSignInResult) {
        when (result) {
            is NativeSignInResult.Success -> {
                authRepository.currentUserEmail()?.let(authRepository::recordLastUsedEmail)
                _state.update { it.copy(isLoading = false) }
            }
            is NativeSignInResult.ClosedByUser -> _state.update { it.copy(isLoading = false) }
            is NativeSignInResult.NetworkError, is NativeSignInResult.Error ->
                _state.update { it.copy(isLoading = false, error = Res.string.error_auth_failed) }
        }
    }
}
