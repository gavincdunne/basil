package org.weekendware.basil.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * [AuthRepository] implementation backed by Supabase Auth.
 *
 * Session persistence is handled automatically by the supabase-kt Auth
 * plugin — tokens are stored in platform-native secure storage and
 * restored on the next app launch.
 *
 * [sessionFlow] maps [Auth.sessionStatus] to a plain `Boolean` so the
 * rest of the app has no direct dependency on supabase-kt types.
 */
class SupabaseAuthRepository(
    private val client: SupabaseClient
) : AuthRepository {

    override val sessionFlow: Flow<Boolean> =
        client.auth.sessionStatus.map { it is SessionStatus.Authenticated }

    override suspend fun signUp(email: String, password: String): Result<Unit> =
        runCatching {
            client.auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }
        }

    override suspend fun signIn(email: String, password: String): Result<Unit> =
        runCatching {
            client.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
        }

    override suspend fun signOut(): Result<Unit> =
        runCatching {
            client.auth.signOut()
        }

    override fun currentUserId(): String? =
        client.auth.currentUserOrNull()?.id

    override fun currentUserEmail(): String? =
        client.auth.currentUserOrNull()?.email

    override fun isSignedIn(): Boolean =
        client.auth.currentUserOrNull() != null

    // ── Social sign-in, password reset, email verification, passkeys,
    //    deep links, and detect-by-email — pending implementation. ──
    // Stubs so the module compiles ahead of the feature build. QA's test
    // suite in commonTest exercises these through FakeAuthRepository until
    // Backend Builder implements each one for real.

    override suspend fun signInWithGoogle(): Result<Unit> =
        TODO("Not yet implemented — see tdd-splash-auth-07222026.md, Google Sign In on KMP")

    override suspend fun signInWithApple(): Result<Unit> =
        TODO("Not yet implemented — see tdd-splash-auth-07222026.md, Apple Sign In on KMP")

    override suspend fun resetPassword(email: String): Result<Unit> =
        TODO("Not yet implemented — see tdd-splash-auth-07222026.md, Supabase deep links (password reset)")

    override suspend fun resendVerificationEmail(): Result<Unit> =
        TODO("Not yet implemented — see tdd-splash-auth-07222026.md, AC12")

    override fun isEmailVerified(): Boolean =
        TODO("Not yet implemented — see tdd-splash-auth-07222026.md, Email verification state")

    override fun daysSinceSignup(): Long =
        TODO("Not yet implemented — see tdd-splash-auth-07222026.md, Email verification 30-day window")

    override suspend fun registerPasskey(): Result<Unit> =
        TODO("Not yet implemented — see tdd-splash-auth-07222026.md, Passkeys on KMP")

    override suspend fun signInWithPasskey(): Result<Unit> =
        TODO("Not yet implemented — see tdd-splash-auth-07222026.md, Passkeys on KMP")

    override fun hasPasskeyEnrolled(): Boolean =
        TODO("Not yet implemented — see tdd-splash-auth-07222026.md, passkey_enrolled DataStore key")

    override suspend fun handleDeepLink(url: String): Result<Unit> =
        TODO("Not yet implemented — see tdd-splash-auth-07222026.md, DeepLinkHandler")

    override fun lastUsedEmail(): String? =
        TODO("Not yet implemented — see tdd-splash-auth-07222026.md, stored_auth_email DataStore key")
}
