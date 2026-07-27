package org.weekendware.basil.presentation.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import org.weekendware.basil.data.repository.AuthRepository
import org.weekendware.basil.data.repository.OnboardingLocalRepository

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

    /**
     * No Supabase session exists. Full silent account provisioning means this
     * alone doesn't determine what the user sees next — see
     * [unauthenticatedDestination].
     *
     * @property onboardingComplete Whether the local (DataStore-persisted)
     *   onboarding conversation has finished. Local state, not account state
     *   — onboarding runs entirely pre-auth.
     * @property hasAccount Whether this device has ever completed a real
     *   sign-up/sign-in. Derived from [AuthRepository.lastUsedEmail] — the
     *   same detect-by-email signal the auth screen already uses, repurposed
     *   here as a "has this device provisioned an account before" proxy
     *   rather than introducing a second persisted flag for a materially
     *   similar question. Nothing currently clears it once set.
     */
    data class Unauthenticated(
        val onboardingComplete: Boolean,
        val hasAccount: Boolean,
    ) : SessionState
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

/** Where an unauthenticated user lands — full silent account provisioning. */
enum class UnauthenticatedDestination { Onboarding, SaveProgress, Auth }

/**
 * Pure routing decision for a user with no Supabase session. Onboarding runs
 * entirely pre-auth and unconditionally comes first; only once it's done does
 * account state matter. A user who finished onboarding but never created an
 * account sees "save your progress" framing rather than a cold sign-in form.
 * A user who has an account but is currently signed out (explicit sign-out,
 * expired session) goes straight to the standard auth screen — onboarding is
 * already done, there's nothing to save.
 */
fun unauthenticatedDestination(state: SessionState.Unauthenticated): UnauthenticatedDestination =
    when {
        !state.onboardingComplete -> UnauthenticatedDestination.Onboarding
        !state.hasAccount -> UnauthenticatedDestination.SaveProgress
        else -> UnauthenticatedDestination.Auth
    }

/**
 * App-root ViewModel that owns the single source of truth for auth state.
 *
 * Combines [AuthRepository.sessionFlow] with [OnboardingLocalRepository.state]
 * and exposes [state] as a [StateFlow] so [App] can decide which nav graph to
 * display without any Supabase-specific types leaking into the UI layer.
 *
 * The initial value is [SessionState.Loading] to prevent a visible
 * flash between the unauthenticated and authenticated screens while
 * the SDK restores a stored session on cold start.
 */
class SessionViewModel(
    private val authRepository: AuthRepository,
    private val onboardingLocalRepository: OnboardingLocalRepository,
) : ViewModel() {

    val state: StateFlow<SessionState> = combine(
        authRepository.sessionFlow,
        onboardingLocalRepository.state,
    ) { isSignedIn, onboarding ->
        if (isSignedIn) {
            SessionState.Authenticated(
                isEmailVerified = authRepository.isEmailVerified(),
                daysSinceSignup = authRepository.daysSinceSignup(),
            )
        } else {
            SessionState.Unauthenticated(
                onboardingComplete = onboarding.isComplete,
                hasAccount = authRepository.lastUsedEmail() != null,
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = SessionState.Loading
    )
}
