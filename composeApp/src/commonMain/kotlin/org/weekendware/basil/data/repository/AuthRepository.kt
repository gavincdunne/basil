package org.weekendware.basil.data.repository

/**
 * Contract for authentication operations.
 *
 * All methods return [Result] so callers can handle errors without
 * catching exceptions directly.
 */
interface AuthRepository {

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
