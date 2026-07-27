package org.weekendware.basil.presentation.onboarding

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import basil.composeapp.generated.resources.Res
import basil.composeapp.generated.resources.error_auth_failed
import io.github.jan.supabase.compose.auth.composable.NativeSignInResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.weekendware.basil.data.repository.AuthRepository
import org.weekendware.basil.domain.usecase.SyncOnboardingToSupabaseUseCase
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
 * ViewModel for [SaveProgressScreen]. Handles account creation and the
 * one-time sync of the already-collected local onboarding answers to
 * Supabase once the account exists — [SyncOnboardingToSupabaseUseCase],
 * matching the TDD's `syncLocalToSupabase(userId)` call "once, after
 * successful sign-up." This is the only place that sync can happen:
 * onboarding itself runs entirely pre-auth with no `userId` to sync to,
 * and by the time this screen shows, the onboarding conversation is
 * already over — nothing will call back into
 * [org.weekendware.basil.presentation.onboarding.OnboardingViewModel] to
 * trigger it from there.
 */
class SaveProgressViewModel(
    private val authRepository: AuthRepository,
    private val syncOnboardingToSupabase: SyncOnboardingToSupabaseUseCase,
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

    /** Called right before `.startFlow()` on compose-auth's remembered Google/Apple action, from [SaveProgressScreen]. */
    fun onSocialSignUpStarted() = _state.update { it.copy(isLoading = true, error = null) }

    /** The `onResult` callback passed to `rememberSignInWithGoogle`/`rememberSignInWithApple` — same shape as [org.weekendware.basil.presentation.auth.AuthViewModel.onSocialSignInResult]. */
    fun onSocialSignUpResult(result: NativeSignInResult) {
        when (result) {
            is NativeSignInResult.Success -> {
                val email = authRepository.currentUserEmail()
                email?.let(authRepository::recordLastUsedEmail)
                val userId = authRepository.currentUserId()
                if (userId != null && email != null) {
                    viewModelScope.launch { syncOnboardingToSupabase(userId, email) }
                }
                _state.update { it.copy(isLoading = false) }
            }
            is NativeSignInResult.ClosedByUser -> _state.update { it.copy(isLoading = false) }
            is NativeSignInResult.NetworkError, is NativeSignInResult.Error ->
                _state.update { it.copy(isLoading = false, error = Res.string.error_auth_failed) }
        }
    }

    fun onCreateAccount() {
        val current = _state.value
        if (!current.canCreateAccount) return

        _state.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            val result = authRepository.signUp(current.email, current.password)
            result.fold(
                onSuccess = {
                    val userId = authRepository.currentUserId()
                    if (userId != null) syncOnboardingToSupabase(userId, current.email)
                    _state.update { it.copy(isLoading = false) }
                },
                onFailure = { _state.update { it.copy(isLoading = false, error = Res.string.error_auth_failed) } }
            )
        }
    }
}
