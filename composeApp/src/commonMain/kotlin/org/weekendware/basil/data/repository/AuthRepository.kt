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

    /** Returns the currently authenticated user's email, or null if signed out. */
    fun currentUserEmail(): String?

    /** True if a valid session exists. */
    fun isSignedIn(): Boolean

    // ── Splash, Auth & Brand Foundation — added ahead of implementation ──
    // See tdd-splash-auth-07222026.md, "Interface design". Signatures only;
    // SupabaseAuthRepository stubs these with TODO() for Backend Builder.

    /** Signs in via Google. Platform-specific implementation. */
    suspend fun signInWithGoogle(): Result<Unit>

    /** Signs in via Apple. iOS only natively; OAuth redirect on other platforms. */
    suspend fun signInWithApple(): Result<Unit>

    /** Sends a password reset email with a deep link back to the app. */
    suspend fun resetPassword(email: String): Result<Unit>

    /** Resends the verification email to the current user. */
    suspend fun resendVerificationEmail(): Result<Unit>

    /** True if the current user's email is confirmed. */
    fun isEmailVerified(): Boolean

    /** Elapsed days since the current user account was created. */
    fun daysSinceSignup(): Long

    /** Returns the FIDO2 registration challenge from Supabase and initiates platform credential creation. */
    suspend fun registerPasskey(): Result<Unit>

    /** Returns the FIDO2 authentication challenge from Supabase and initiates platform credential assertion. */
    suspend fun signInWithPasskey(): Result<Unit>

    /** True if the user has completed FIDO2 registration on this device. */
    fun hasPasskeyEnrolled(): Boolean

    /** Handles an incoming deep link URL (password reset, OAuth callback). */
    suspend fun handleDeepLink(url: String): Result<Unit>

    /** Returns the last-used email for detect-by-email default mode. */
    fun lastUsedEmail(): String?
}
