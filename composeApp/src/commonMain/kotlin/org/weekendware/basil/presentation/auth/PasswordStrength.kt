package org.weekendware.basil.presentation.auth

import org.jetbrains.compose.resources.StringResource

/**
 * Strength classification for a candidate password, driving the 3-segment
 * strength bar on sign-up and the reset flow's new-password step.
 *
 * See `spec-splash-auth-07222026.md` AC18 and
 * `tdd-splash-auth-07222026.md` "Data structures and algorithms".
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
 * @property label Copy string identifying the requirement — pending Copywriter
 *   pass (`Res.string.auth_req_*`); not referenced by the validator stub below.
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
 *
 * **Not implemented here.** This signature is QA scaffolding so
 * `PasswordStrengthValidatorTest` compiles and runs (red) ahead of
 * implementation. Backend Builder implements the body per the algorithm
 * above and the TDD; every test in this suite is expected to fail with
 * `NotImplementedError` until then.
 */
object PasswordStrengthValidator {
    fun validate(password: String): Pair<PasswordStrength, List<PasswordRequirement>> {
        TODO("Not yet implemented — see tdd-splash-auth-07222026.md, AC18")
    }
}
