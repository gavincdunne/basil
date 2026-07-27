package org.weekendware.basil.presentation.auth

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * QA test suite for [PasswordStrengthValidator], written ahead of
 * implementation per the team's test-first process. Covers threshold
 * classification (Weak/Medium/Strong), the length and special-character
 * boundaries, and requirement-list shape and determinism.
 *
 * Deliberately does not assert on [PasswordRequirement.label] contents —
 * only that a label exists — since Copywriter owns the exact wording.
 * Tests assert on strength classification, `met` values, and list
 * shape/order.
 */
class PasswordStrengthValidatorTest {

    @Test
    fun `empty password returns None with an empty requirements list`() {
        val (strength, requirements) = PasswordStrengthValidator.validate("")
        assertEquals(PasswordStrength.None, strength)
        assertTrue(requirements.isEmpty())
    }

    @Test
    fun `password meeting exactly one requirement is Weak`() {
        // "abc" meets only lowercase. Meeting literally zero of the five is
        // impossible for any non-empty password — every character is upper,
        // lower, digit, or special, so at least one requirement always holds.
        // The empty-password case above is the only true "nothing met" state.
        val (strength, requirements) = PasswordStrengthValidator.validate("abc")
        assertEquals(PasswordStrength.Weak, strength)
        assertEquals(5, requirements.size)
        assertEquals(1, requirements.count { it.met })
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
    fun `requirements list has exactly one entry per requirement in a fixed order`() {
        // An all-digit, 8-character password meets only length and number —
        // no letters means no uppercase/lowercase, and digits don't count as
        // special characters. met=true should land at exactly those two
        // positions — length (0) and number (3) — pinning down the actual
        // order (length, uppercase, lowercase, number, special), not just
        // the list's size; the individual boundary tests above each isolate
        // one index but never assert the full five-position layout at once.
        val (_, requirements) = PasswordStrengthValidator.validate("12345678")
        assertEquals(
            listOf(true, false, false, true, false),
            requirements.map { it.met },
        )
    }

    @Test
    fun `validate is deterministic for the same input`() {
        val first = PasswordStrengthValidator.validate("Abcdefg1!")
        val second = PasswordStrengthValidator.validate("Abcdefg1!")
        assertEquals(first.first, second.first)
        assertEquals(first.second.map { it.met }, second.second.map { it.met })
    }
}
