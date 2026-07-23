package org.weekendware.basil.presentation.onboarding

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
import org.weekendware.basil.presentation.auth.PasswordRequirement
import org.weekendware.basil.presentation.auth.PasswordStrength
import org.weekendware.basil.presentation.auth.PasswordStrengthValidator

/** Which step of the "Save your progress" flow is showing. */
enum class SaveProgressStep { ChooseMethod, EmailEntry }

/**
 * State for [SaveProgressScreen] — shown at the end of onboarding to create
 * an account (AUTH-12 choose-method, AUTH-12b email/password entry).
 *
 * @property isPasswordVisible Shared by password and confirm-password
 *   fields — see [org.weekendware.basil.presentation.auth.NewPasswordUiState]
 *   for the same rule and its rationale.
 */
@Immutable
data class SaveProgressUiState(
    val step: SaveProgressStep = SaveProgressStep.ChooseMethod,
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val passwordStrength: PasswordStrength = PasswordStrength.None,
    val passwordRequirements: List<PasswordRequirement> = emptyList(),
    val isLoading: Boolean = false,
    val error: StringResource? = null,
) {
    val canCreateAccount: Boolean get() = passwordStrength == PasswordStrength.Strong && email.isNotBlank() && !isLoading
}

/**
 * ViewModel for [SaveProgressScreen]. Only handles account creation itself
 * — syncing the already-collected local onboarding answers to Supabase
 * once the account exists is
 * [org.weekendware.basil.presentation.onboarding.OnboardingViewModel]'s job,
 * not this screen's, matching the TDD's `syncLocalToSupabase(userId)` call
 * "once, after successful sign-up."
 */
class SaveProgressViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SaveProgressUiState())
    val state: StateFlow<SaveProgressUiState> = _state

    fun onContinueWithEmail() = _state.update { it.copy(step = SaveProgressStep.EmailEntry) }
    fun onBackToChooseMethod() = _state.update { it.copy(step = SaveProgressStep.ChooseMethod, error = null) }

    fun onEmailChange(value: String) = _state.update { it.copy(email = value, error = null) }
    fun onTogglePasswordVisibility() = _state.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }

    fun onPasswordChange(value: String) {
        val (strength, requirements) = PasswordStrengthValidator.validate(value)
        _state.update {
            it.copy(password = value, passwordStrength = strength, passwordRequirements = requirements, error = null)
        }
    }

    fun onGoogleSignUp() = signUpWithProvider(authRepository::signInWithGoogle)
    fun onAppleSignUp() = signUpWithProvider(authRepository::signInWithApple)

    fun onCreateAccount() {
        val current = _state.value
        if (!current.canCreateAccount) return

        _state.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            val result = authRepository.signUp(current.email, current.password)
            result.fold(
                onSuccess = { _state.update { it.copy(isLoading = false) } },
                onFailure = { _state.update { it.copy(isLoading = false, error = Res.string.error_auth_failed) } }
            )
        }
    }

    private fun signUpWithProvider(provider: suspend () -> Result<Unit>) {
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
