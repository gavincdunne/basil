package org.weekendware.basil.presentation.session

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * QA test suite for [authenticatedDestination] — the pure routing decision
 * that gates the 30-day email-verification soft-block.
 */
class SessionStateRoutingTest {

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
