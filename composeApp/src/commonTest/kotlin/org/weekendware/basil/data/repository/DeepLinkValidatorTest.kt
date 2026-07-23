package org.weekendware.basil.data.repository

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * QA test suite for [DeepLinkValidator], written from
 * `tdd-splash-auth-07222026.md` ahead of implementation.
 *
 * All cases are expected to fail until Backend Builder implements
 * [DeepLinkValidator.isAllowed]. Covers SPA-098–SPA-100 in
 * `tests-splash-auth-07232026.md`.
 */
class DeepLinkValidatorTest {

    @Test
    fun `bare reset-password link is allowed`() {
        assertTrue(DeepLinkValidator.isAllowed("basil://reset-password"))
    }

    @Test
    fun `reset-password link with a query string is allowed`() {
        assertTrue(DeepLinkValidator.isAllowed("basil://reset-password?token=abc123"))
    }

    @Test
    fun `bare auth callback link is allowed`() {
        assertTrue(DeepLinkValidator.isAllowed("basil://auth/callback"))
    }

    @Test
    fun `auth callback link with a query string is allowed`() {
        assertTrue(DeepLinkValidator.isAllowed("basil://auth/callback?code=xyz"))
    }

    @Test
    fun `a lookalike link that merely starts with the allowed string is rejected`() {
        // "basil://reset-password-evil.com" is a raw string prefix of
        // "basil://reset-password" as characters — a naive startsWith()
        // check would wrongly accept this. The allowed shape ends at the
        // path boundary (end of string or '?'), not at an arbitrary
        // continuation of the same characters.
        assertFalse(DeepLinkValidator.isAllowed("basil://reset-password-evil.com"))
        assertFalse(DeepLinkValidator.isAllowed("basil://reset-password.attacker.com"))
        assertFalse(DeepLinkValidator.isAllowed("basil://auth/callbackXYZ"))
    }

    @Test
    fun `wrong scheme is rejected even with an otherwise valid path`() {
        assertFalse(DeepLinkValidator.isAllowed("https://reset-password"))
        assertFalse(DeepLinkValidator.isAllowed("http://basil.app/reset-password"))
    }

    @Test
    fun `unrelated basil scheme paths are rejected`() {
        assertFalse(DeepLinkValidator.isAllowed("basil://home"))
        assertFalse(DeepLinkValidator.isAllowed("basil://chat"))
    }

    @Test
    fun `empty and blank input is rejected`() {
        assertFalse(DeepLinkValidator.isAllowed(""))
        assertFalse(DeepLinkValidator.isAllowed("   "))
    }

    @Test
    fun `path traversal style suffix is rejected`() {
        assertFalse(DeepLinkValidator.isAllowed("basil://reset-password/../../auth/callback"))
    }

    @Test
    fun `case sensitivity — scheme is not matched case-insensitively`() {
        // Custom URL schemes should be handled literally; do not silently
        // widen the allow-list via case-insensitive matching.
        assertFalse(DeepLinkValidator.isAllowed("BASIL://reset-password"))
    }
}
