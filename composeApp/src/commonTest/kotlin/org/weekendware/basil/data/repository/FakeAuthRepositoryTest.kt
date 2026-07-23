package org.weekendware.basil.data.repository

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Verifies [FakeAuthRepository]'s own contract for the splash-auth
 * additions. This is QA's test double, not production code, but its
 * behavior must be trustworthy — a fake that doesn't match the real
 * contract produces ViewModel tests that pass for the wrong reasons.
 *
 * Also serves as executable documentation of what
 * [SupabaseAuthRepository]'s real implementation must satisfy: success
 * establishes/updates the relevant state, failure leaves it untouched.
 */
class FakeAuthRepositoryTest {

    @Test
    fun `successful Google sign-in establishes a session`() = runTest {
        val repo = FakeAuthRepository()
        val result = repo.signInWithGoogle()
        assertTrue(result.isSuccess)
        assertTrue(repo.isSignedIn())
    }

    @Test
    fun `failed Google sign-in does not establish a session`() = runTest {
        val repo = FakeAuthRepository()
        repo.googleSignInResult = Result.failure(Exception("cancelled"))
        val result = repo.signInWithGoogle()
        assertTrue(result.isFailure)
        assertFalse(repo.isSignedIn())
    }

    @Test
    fun `successful Apple sign-in establishes a session`() = runTest {
        val repo = FakeAuthRepository()
        val result = repo.signInWithApple()
        assertTrue(result.isSuccess)
        assertTrue(repo.isSignedIn())
    }

    @Test
    fun `resetPassword records the requested email and call count`() = runTest {
        val repo = FakeAuthRepository()
        repo.resetPassword("user@example.com")
        assertEquals(1, repo.resetPasswordCallCount)
        assertEquals("user@example.com", repo.lastResetPasswordEmail)

        repo.resetPassword("user@example.com")
        assertEquals(2, repo.resetPasswordCallCount)
    }

    @Test
    fun `resendVerificationEmail increments call count independent of result`() = runTest {
        val repo = FakeAuthRepository()
        repo.resendVerificationResult = Result.failure(Exception("rate limited"))
        val result = repo.resendVerificationEmail()
        assertTrue(result.isFailure)
        assertEquals(1, repo.resendVerificationCallCount)
    }

    @Test
    fun `isEmailVerified and daysSinceSignup reflect configured state`() {
        val repo = FakeAuthRepository()
        repo.emailVerified = false
        repo.daysSinceSignupValue = 31
        assertFalse(repo.isEmailVerified())
        assertEquals(31L, repo.daysSinceSignup())
    }

    @Test
    fun `successful passkey registration sets hasPasskeyEnrolled`() = runTest {
        val repo = FakeAuthRepository()
        assertFalse(repo.hasPasskeyEnrolled())
        repo.registerPasskey()
        assertTrue(repo.hasPasskeyEnrolled())
    }

    @Test
    fun `failed passkey registration leaves hasPasskeyEnrolled false`() = runTest {
        val repo = FakeAuthRepository()
        repo.registerPasskeyResult = Result.failure(Exception("user cancelled"))
        repo.registerPasskey()
        assertFalse(repo.hasPasskeyEnrolled())
    }

    @Test
    fun `successful passkey sign-in establishes a session`() = runTest {
        val repo = FakeAuthRepository()
        val result = repo.signInWithPasskey()
        assertTrue(result.isSuccess)
        assertTrue(repo.isSignedIn())
    }

    @Test
    fun `failed passkey sign-in does not establish a session`() = runTest {
        val repo = FakeAuthRepository()
        repo.signInWithPasskeyResult = Result.failure(Exception("biometric failed"))
        val result = repo.signInWithPasskey()
        assertTrue(result.isFailure)
        assertFalse(repo.isSignedIn())
    }

    @Test
    fun `handleDeepLink records the url and returns the configured result`() = runTest {
        val repo = FakeAuthRepository()
        repo.handleDeepLinkResult = Result.failure(Exception("rejected"))
        val result = repo.handleDeepLink("basil://reset-password?token=abc")
        assertTrue(result.isFailure)
        assertEquals("basil://reset-password?token=abc", repo.lastHandledDeepLink)
    }

    @Test
    fun `lastUsedEmail is null before any sign-in and set after a successful one`() = runTest {
        val repo = FakeAuthRepository()
        assertNull(repo.lastUsedEmail())
        repo.signIn("returning@example.com", "password123")
        assertEquals("returning@example.com", repo.lastUsedEmail())
    }

    @Test
    fun `lastUsedEmail is set on sign-up as well as sign-in`() = runTest {
        val repo = FakeAuthRepository()
        repo.signUp("new@example.com", "password123")
        assertEquals("new@example.com", repo.lastUsedEmail())
    }
}
