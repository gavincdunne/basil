package org.weekendware.basil.data.repository

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * QA test suite for [DeepLinkHandler], written ahead of implementation per
 * the team's test-first process.
 *
 * All cases are expected to fail with `NotImplementedError` until Backend
 * Builder implements [DeepLinkHandler.handle] — which itself depends on
 * [DeepLinkValidator.isAllowed] also being implemented (see
 * `DeepLinkValidatorTest`).
 */
class DeepLinkHandlerTest {

    @Test
    fun `an allowed reset-password url is forwarded to the auth repository`() = runTest {
        val repo = FakeAuthRepository()
        val handler = DeepLinkHandler(repo)

        handler.handle("basil://reset-password?token=abc123")

        assertEquals("basil://reset-password?token=abc123", repo.lastHandledDeepLink)
    }

    @Test
    fun `an allowed auth-callback url is forwarded to the auth repository`() = runTest {
        val repo = FakeAuthRepository()
        val handler = DeepLinkHandler(repo)

        handler.handle("basil://auth/callback?code=xyz")

        assertEquals("basil://auth/callback?code=xyz", repo.lastHandledDeepLink)
    }

    @Test
    fun `a rejected url is dropped and never reaches the auth repository`() = runTest {
        val repo = FakeAuthRepository()
        val handler = DeepLinkHandler(repo)

        handler.handle("basil://reset-password-evil.com")

        assertNull(repo.lastHandledDeepLink)
    }

    @Test
    fun `a rejected url does not throw`() = runTest {
        val repo = FakeAuthRepository()
        val handler = DeepLinkHandler(repo)

        // Should complete normally — dropped silently, not an exception.
        handler.handle("https://not-basil-at-all.com")
    }
}
