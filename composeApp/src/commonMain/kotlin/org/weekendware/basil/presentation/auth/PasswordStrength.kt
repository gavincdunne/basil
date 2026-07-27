package org.weekendware.basil.presentation.auth

import basil.composeapp.generated.resources.Res
import basil.composeapp.generated.resources.auth_req_length
import basil.composeapp.generated.resources.auth_req_lowercase
import basil.composeapp.generated.resources.auth_req_number
import basil.composeapp.generated.resources.auth_req_special
import basil.composeapp.generated.resources.auth_req_uppercase
import org.jetbrains.compose.resources.StringResource

/**
 * Strength classification for a candidate password, driving the 3-segment
 * strength bar on sign-up and the reset flow's new-password step.
 */
sealed class PasswordStrength {
    /** No input yet — strength bar is hidden. */
    data object None : PasswordStrength()

    /** 0–2 of 5 requirements met — 1 red segment. */
    data object Weak : PasswordStrength()

    /** 3–4 of 5 requirements met — 2 amber segments. */
    data object Medium : PasswordStrength()

    /** All 5 requirements met — 3 green segments. */
    data object Strong : PasswordStrength()
}

/**
 * One row in the live requirements checklist shown below the strength bar.
 *
 * @property label Copy string identifying the requirement.
 * @property met   True once the candidate password satisfies this requirement.
 */
data class PasswordRequirement(val label: StringResource, val met: Boolean)

/**
 * Validates a candidate password against the five table-stakes requirements
 * and classifies overall strength.
 *
 * **Pure function — no side effects, no ViewModel state, no I/O.**
 * `AuthViewModel.onPasswordChanged` calls this synchronously and writes both
 * outputs to `AuthUiState`.
 *
 * Requirements, in order: minimum 8 characters, uppercase letter, lowercase
 * letter, number, special character (any non-alphanumeric character).
 * Strength mapping: `met.size` 0–2 → [PasswordStrength.Weak], 3–4 →
 * [PasswordStrength.Medium], 5 → [PasswordStrength.Strong]. An empty password
 * returns [PasswordStrength.None] paired with an empty requirements list.
 */
object PasswordStrengthValidator {
    fun validate(password: String): Pair<PasswordStrength, List<PasswordRequirement>> {
        if (password.isEmpty()) return PasswordStrength.None to emptyList()

        val requirements = listOf(
            PasswordRequirement(Res.string.auth_req_length, password.length >= 8),
            PasswordRequirement(Res.string.auth_req_uppercase, password.any { it.isUpperCase() }),
            PasswordRequirement(Res.string.auth_req_lowercase, password.any { it.isLowerCase() }),
            PasswordRequirement(Res.string.auth_req_number, password.any { it.isDigit() }),
            PasswordRequirement(Res.string.auth_req_special, password.any { !it.isLetterOrDigit() }),
        )

        val metCount = requirements.count { it.met }
        val strength = when {
            metCount <= 2 -> PasswordStrength.Weak
            metCount <= 4 -> PasswordStrength.Medium
            else          -> PasswordStrength.Strong
        }

        return strength to requirements
    }
}
