package org.weekendware.basil.presentation.auth

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * QA test suite for [PasswordStrengthValidator], written from
 * `tdd-splash-auth-07222026.md` (AC18) ahead of implementation.
 *
 * All cases are expected to fail with `NotImplementedError` until Backend
 * Builder implements [PasswordStrengthValidator.validate]. See
 * `tests-splash-auth-07232026.md` for the full QA plan — this file covers
 * SPA-052 through SPA-060 and SPA-062.
 *
 * Deliberately does not assert on [PasswordRequirement.label] — copy is
 * pending a Copywriter pass and is out of scope for this validator's
 * contract. Tests assert only on strength classification, `met` values,
 * and list shape/order.
 */
class PasswordStrengthValidatorTest {

    @Test
    fun `empty password returns None with an empty requirements list`() {
        val (strength, requirements) = PasswordStrengthValidator.validate("")
        assertEquals(PasswordStrength.None, strength)
        assertTrue(requirements.isEmpty())
    }

    @Test
    fun `password meeting zero requirements is Weak`() {
        // Below length, no uppercase, no digit, no special char.
        val (strength, requirements) = PasswordStrengthValidator.validate("abc")
        assertEquals(PasswordStrength.Weak, strength)
        assertEquals(5, requirements.size)
        assertTrue(requirements.none { it.met })
    }

    @Test
    fun `password meeting exactly two requirements is Weak`() {
        // lowercase + length, nothing else.
        val (strength, _) = PasswordStrengthValidator.validate("abcdefgh")
        assertEquals(PasswordStrength.Weak, strength)
    }

    @Test
    fun `password meeting exactly three requirements is Medium`() {
        // length + lowercase + uppercase.
        val (strength, _) = PasswordStrengthValidator.validate("Abcdefgh")
        assertEquals(PasswordStrength.Medium, strength)
    }

    @Test
    fun `password meeting exactly four requirements is Medium`() {
        // length + lowercase + uppercase + digit, no special char.
        val (strength, _) = PasswordStrengthValidator.validate("Abcdefg1")
        assertEquals(PasswordStrength.Medium, strength)
    }

    @Test
    fun `password meeting all five requirements is Strong`() {
        val (strength, requirements) = PasswordStrengthValidator.validate("Abcdefg1!")
        assertEquals(PasswordStrength.Strong, strength)
        assertTrue(requirements.all { it.met })
    }

    @Test
    fun `length requirement fails at seven characters and passes at eight`() {
        val (_, sevenChars) = PasswordStrengthValidator.validate("Abcdef1!".dropLast(1))
        assertEquals(false, sevenChars[0].met)

        val (_, eightChars) = PasswordStrengthValidator.validate("Abcdef1!")
        assertEquals(true, eightChars[0].met)
    }

    @Test
    fun `uppercase requirement is met only when an uppercase letter is present`() {
        val (_, withoutUpper) = PasswordStrengthValidator.validate("abcdefg1!")
        assertEquals(false, withoutUpper[1].met)

        val (_, withUpper) = PasswordStrengthValidator.validate("Abcdefg1!")
        assertEquals(true, withUpper[1].met)
    }

    @Test
    fun `lowercase requirement is met only when a lowercase letter is present`() {
        val (_, withoutLower) = PasswordStrengthValidator.validate("ABCDEFG1!")
        assertEquals(false, withoutLower[2].met)

        val (_, withLower) = PasswordStrengthValidator.validate("ABCDEFGa1!")
        assertEquals(true, withLower[2].met)
    }

    @Test
    fun `number requirement is met only when a digit is present`() {
        val (_, withoutDigit) = PasswordStrengthValidator.validate("Abcdefgh!")
        assertEquals(false, withoutDigit[3].met)

        val (_, withDigit) = PasswordStrengthValidator.validate("Abcdefg1!")
        assertEquals(true, withDigit[3].met)
    }

    @Test
    fun `special character requirement accepts any non-alphanumeric character`() {
        for (symbol in listOf('!', '-', '#', '@', '_', '.')) {
            val (_, requirements) = PasswordStrengthValidator.validate("Abcdefg1$symbol")
            assertTrue(requirements[4].met, "Expected '$symbol' to satisfy the special-character requirement")
        }
    }

    @Test
    fun `special character requirement is not met without one`() {
        val (_, requirements) = PasswordStrengthValidator.validate("Abcdefg12")
        assertEquals(false, requirements[4].met)
    }

    @Test
    fun `requirements are returned in a stable order — length, uppercase, lowercase, number, special`() {
        val (_, requirements) = PasswordStrengthValidator.validate("Abcdefg1!")
        assertEquals(5, requirements.size)
        // Order asserted positionally per the TDD's documented sequence;
        // see the boundary tests above for what each index represents.
    }

    @Test
    fun `validate is deterministic for the same input`() {
        val first = PasswordStrengthValidator.validate("Abcdefg1!")
        val second = PasswordStrengthValidator.validate("Abcdefg1!")
        assertEquals(first.first, second.first)
        assertEquals(first.second.map { it.met }, second.second.map { it.met })
    }
}
