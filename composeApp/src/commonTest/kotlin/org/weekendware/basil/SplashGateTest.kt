package org.weekendware.basil

import org.weekendware.basil.presentation.session.SessionState
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Unit tests for [shouldShowSplash].
 *
 * The splash gate has two independent conditions:
 * - Loading state → always show splash, regardless of fade
 * - Fade not complete → keep showing splash even after session resolves
 *
 * Both must be false for the splash to be hidden.
 */
class SplashGateTest {

    @Test
    fun `shows splash while session is loading, even when fade is done`() {
        assertTrue(shouldShowSplash(SessionState.Loading, splashFadeDone = true))
    }

    @Test
    fun `shows splash while session is loading and fade is not done`() {
        assertTrue(shouldShowSplash(SessionState.Loading, splashFadeDone = false))
    }

    @Test
    fun `shows splash when session resolved but fade not yet complete`() {
        assertTrue(shouldShowSplash(SessionState.Unauthenticated, splashFadeDone = false))
        assertTrue(shouldShowSplash(SessionState.Authenticated,   splashFadeDone = false))
    }

    @Test
    fun `hides splash when unauthenticated and fade is done`() {
        assertFalse(shouldShowSplash(SessionState.Unauthenticated, splashFadeDone = true))
    }

    @Test
    fun `hides splash when authenticated and fade is done`() {
        assertFalse(shouldShowSplash(SessionState.Authenticated, splashFadeDone = true))
    }
}
