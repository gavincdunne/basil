package org.weekendware.basil.presentation.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.weekendware.basil.data.repository.AuthRepository

/** Represents the resolved authentication state of the current session. */
sealed interface SessionState {
    /** Supabase is restoring the session from storage — do not navigate yet. */
    data object Loading : SessionState

    /**
     * A valid session exists; the user is signed in.
     *
     * @property isEmailVerified True once the user has confirmed their email.
     * @property daysSinceSignup Elapsed days since account creation — used to
     *   decide whether an unverified user is inside the grace period or past
     *   the soft-block threshold.
     */
    data class Authenticated(
        val isEmailVerified: Boolean,
        val daysSinceSignup: Long,
    ) : SessionState

    /** No session — the user must sign in. */
    data object Unauthenticated : SessionState
}

/** Where an authenticated user lands once their verification status is known. */
enum class AuthenticatedDestination { Normal, VerificationWall }

/**
 * Pure routing decision for an authenticated user. A user unverified for 30
 * or more days is walled off until they verify or request a new email;
 * everyone else — verified, or unverified but still within the grace
 * period — reaches the normal authenticated experience. Unverified-within-grace
 * users see a dismissible banner inside that experience, not this wall.
 */
fun authenticatedDestination(state: SessionState.Authenticated): AuthenticatedDestination =
    if (!state.isEmailVerified && state.daysSinceSignup >= 30) {
        AuthenticatedDestination.VerificationWall
    } else {
        AuthenticatedDestination.Normal
    }

/**
 * App-root ViewModel that owns the single source of truth for auth state.
 *
 * Collects [AuthRepository.sessionFlow] and exposes [state] as a
 * [StateFlow] so [App] can decide which nav graph to display without
 * any Supabase-specific types leaking into the UI layer.
 *
 * The initial value is [SessionState.Loading] to prevent a visible
 * flash between the unauthenticated and authenticated screens while
 * the SDK restores a stored session on cold start.
 */
class SessionViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    val state: StateFlow<SessionState> = authRepository.sessionFlow
        .map { isSignedIn ->
            if (isSignedIn) {
                SessionState.Authenticated(
                    isEmailVerified = authRepository.isEmailVerified(),
                    daysSinceSignup = authRepository.daysSinceSignup(),
                )
            } else {
                SessionState.Unauthenticated
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = SessionState.Loading
        )
}
