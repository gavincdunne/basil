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

    /** A valid session exists; the user is signed in. */
    data object Authenticated : SessionState

    /** No session — the user must sign in. */
    data object Unauthenticated : SessionState
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
    authRepository: AuthRepository
) : ViewModel() {

    val state: StateFlow<SessionState> = authRepository.sessionFlow
        .map { isSignedIn ->
            if (isSignedIn) SessionState.Authenticated else SessionState.Unauthenticated
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = SessionState.Loading
        )
}
