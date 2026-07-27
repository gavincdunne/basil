package org.weekendware.basil.presentation.session

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * QA test suite for [authenticatedDestination] and [unauthenticatedDestination]
 * — the pure routing decisions for full silent account provisioning and the
 * 30-day email-verification soft-block.
 */
class SessionStateRoutingTest {

    @Test
    fun `onboarding-incomplete routes to Onboarding regardless of account state`() {
        assertEquals(
            UnauthenticatedDestination.Onboarding,
            unauthenticatedDestination(SessionState.Unauthenticated(onboardingComplete = false, hasAccount = false)),
        )
        assertEquals(
            UnauthenticatedDestination.Onboarding,
            unauthenticatedDestination(SessionState.Unauthenticated(onboardingComplete = false, hasAccount = true)),
        )
    }

    @Test
    fun `onboarding-complete with no account routes to SaveProgress`() {
        assertEquals(
            UnauthenticatedDestination.SaveProgress,
            unauthenticatedDestination(SessionState.Unauthenticated(onboardingComplete = true, hasAccount = false)),
        )
    }

    @Test
    fun `onboarding-complete with an existing account routes to Auth`() {
        assertEquals(
            UnauthenticatedDestination.Auth,
            unauthenticatedDestination(SessionState.Unauthenticated(onboardingComplete = true, hasAccount = true)),
        )
    }

    @Test
    fun `verified user routes to Normal regardless of account age`() {
        assertEquals(
            AuthenticatedDestination.Normal,
            authenticatedDestination(SessionState.Authenticated(isEmailVerified = true, daysSinceSignup = 0)),
        )
        assertEquals(
            AuthenticatedDestination.Normal,
            authenticatedDestination(SessionState.Authenticated(isEmailVerified = true, daysSinceSignup = 500)),
        )
    }

    @Test
    fun `unverified user within the grace period routes to Normal`() {
        assertEquals(
            AuthenticatedDestination.Normal,
            authenticatedDestination(SessionState.Authenticated(isEmailVerified = false, daysSinceSignup = 0)),
        )
        assertEquals(
            AuthenticatedDestination.Normal,
            authenticatedDestination(SessionState.Authenticated(isEmailVerified = false, daysSinceSignup = 29)),
        )
    }

    @Test
    fun `unverified user at exactly the 30-day boundary routes to the verification wall`() {
        assertEquals(
            AuthenticatedDestination.VerificationWall,
            authenticatedDestination(SessionState.Authenticated(isEmailVerified = false, daysSinceSignup = 30)),
        )
    }

    @Test
    fun `unverified user well past 30 days routes to the verification wall`() {
        assertEquals(
            AuthenticatedDestination.VerificationWall,
            authenticatedDestination(SessionState.Authenticated(isEmailVerified = false, daysSinceSignup = 100)),
        )
    }
}
