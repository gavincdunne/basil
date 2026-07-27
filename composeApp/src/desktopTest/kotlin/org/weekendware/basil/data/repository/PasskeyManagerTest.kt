package org.weekendware.basil.data.repository

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * QA test suite for the Desktop [PasskeyManager] — the only platform whose
 * behavior (unsupported, always fails) is specified precisely enough to
 * verify without real hardware, a simulator, or platform credential UI.
 * Android and iOS ceremonies need platform-specific test infrastructure
 * (Robolectric-style `CredentialManager` mocking, XCTest) that doesn't
 * exist in this project yet — flagged as a follow-up, not written here.
 *
 * Both cases are expected to fail until Backend Builder implements the
 * Desktop no-op per the TDD (`Result.failure(UnsupportedOperationException)`).
 */
class PasskeyManagerTest {

    @Test
    fun `createCredential is unsupported on Desktop`() = runTest {
        val manager = PasskeyManager()
        val result = manager.createCredential("{}")
        assertTrue(result.isFailure)
        assertIs<UnsupportedOperationException>(result.exceptionOrNull())
    }

    @Test
    fun `getCredential is unsupported on Desktop`() = runTest {
        val manager = PasskeyManager()
        val result = manager.getCredential("{}")
        assertTrue(result.isFailure)
        assertIs<UnsupportedOperationException>(result.exceptionOrNull())
    }
}
