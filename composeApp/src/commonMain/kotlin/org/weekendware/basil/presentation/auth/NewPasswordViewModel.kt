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
 * State for the new-password screen (AUTH-07), reached via a password-reset
 * deep link.
 *
 * @property isPasswordVisible Shared by both fields — per the TDD, when a
 *   screen has both a new-password and confirm-password field, they mask
 *   or reveal together rather than toggling independently.
 */
@Immutable
data class NewPasswordUiState(
    val password: String = "",
    val confirmPassword: String = "",
    val isPasswordVisible: Boolean = false,
    val passwordStrength: PasswordStrength = PasswordStrength.None,
    val passwordRequirements: List<PasswordRequirement> = emptyList(),
    val isLoading: Boolean = false,
    val error: StringResource? = null,
) {
    val passwordsMatch: Boolean get() = confirmPassword.isNotEmpty() && password == confirmPassword
    val canSubmit: Boolean get() = passwordStrength == PasswordStrength.Strong && passwordsMatch && !isLoading
}

/**
 * ViewModel for AUTH-07. The sign-up CTA stays disabled until the password
 * is [PasswordStrength.Strong] and the confirm field matches it exactly.
 */
class NewPasswordViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(NewPasswordUiState())
    val state: StateFlow<NewPasswordUiState> = _state

    fun onPasswordChange(value: String) {
        val (strength, requirements) = PasswordStrengthValidator.validate(value)
        _state.update {
            it.copy(password = value, passwordStrength = strength, passwordRequirements = requirements, error = null)
        }
    }

    fun onConfirmPasswordChange(value: String) = _state.update { it.copy(confirmPassword = value, error = null) }
    fun onTogglePasswordVisibility() = _state.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }

    fun submit() {
        val current = _state.value
        if (!current.canSubmit) return

        _state.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            val result = authRepository.updatePassword(current.password)
            result.fold(
                onSuccess = { _state.update { it.copy(isLoading = false) } },
                onFailure = { _state.update { it.copy(isLoading = false, error = Res.string.error_auth_failed) } }
            )
        }
    }
}
