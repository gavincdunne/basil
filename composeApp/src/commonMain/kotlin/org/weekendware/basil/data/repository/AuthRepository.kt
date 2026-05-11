package org.weekendware.basil.data.repository

import kotlinx.coroutines.flow.Flow

/**
 * Contract for authentication operations.
 *
 * All methods return [Result] so callers can handle errors without
 * catching exceptions directly. [sessionFlow] emits whenever the
 * sign-in state changes so the UI layer can react without polling.
 */
interface AuthRepository {

    /**
     * Emits `true` when a valid session exists, `false` when signed out.
     * Replays the current state immediately on collection.
     */
    val sessionFlow: Flow<Boolean>

    /** Signs up a new user with [email] and [password]. */
    suspend fun signUp(email: String, password: String): Result<Unit>

    /** Signs in an existing user with [email] and [password]. */
    suspend fun signIn(email: String, password: String): Result<Unit>

    /** Signs out the current user and clears the local session. */
    suspend fun signOut(): Result<Unit>

    /** Returns the currently authenticated user's ID, or null if signed out. */
    fun currentUserId(): String?

    /** True if a valid session exists. */
    fun isSignedIn(): Boolean
}
