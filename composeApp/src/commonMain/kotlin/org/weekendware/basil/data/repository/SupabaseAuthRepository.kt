package org.weekendware.basil.data.repository

import com.russhwolf.settings.Settings
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.OtpType
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock

/**
 * [AuthRepository] implementation backed by Supabase Auth.
 *
 * Session persistence is handled automatically by the supabase-kt Auth
 * plugin — tokens are stored in platform-native secure storage and
 * restored on the next app launch.
 *
 * [sessionFlow] maps [Auth.sessionStatus] to a plain `Boolean` so the
 * rest of the app has no direct dependency on supabase-kt types.
 *
 * [settings] backs [lastUsedEmail]/[recordLastUsedEmail] — those two are
 * declared non-`suspend` on [AuthRepository] (read synchronously from
 * [org.weekendware.basil.presentation.auth.AuthViewModel.onContinueEmail]
 * and the ViewModel's `init` block), which rules out the existing
 * DataStore-backed repositories elsewhere in this app — DataStore is
 * Flow-only. `multiplatform-settings` gives genuinely synchronous
 * cross-platform key-value storage instead, which is what this needs.
 */
class SupabaseAuthRepository(
    private val client: SupabaseClient,
    private val settings: Settings,
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

    // ── Password reset, email verification, deep links, and
    //    detect-by-email — implemented below. Passkeys (registerPasskey,
    //    signInWithPasskey, hasPasskeyEnrolled) stay TODO(): the TDD gates
    //    that work behind DevOps prerequisites (apple-app-site-association
    //    and assetlinks.json served from a live production domain) that
    //    don't exist yet — "do not begin step 10 until both files are live
    //    and verified." Google/Apple sign-in no longer live here at all —
    //    compose-auth's rememberSignInWithGoogle/rememberSignInWithApple
    //    call the SupabaseClient directly from AuthScreen; see AuthUiState
    //    changelog / AuthViewModel.onSocialSignInResult.

    override suspend fun resetPassword(email: String): Result<Unit> =
        runCatching {
            client.auth.resetPasswordForEmail(email, redirectUrl = "basil://reset-password")
        }

    override suspend fun updatePassword(newPassword: String): Result<Unit> =
        runCatching {
            client.auth.updateUser { password = newPassword }
        }

    override suspend fun resendVerificationEmail(): Result<Unit> =
        runCatching {
            val email = requireNotNull(currentUserEmail()) { "resendVerificationEmail() called with no signed-in user" }
            client.auth.resendEmail(OtpType.Email.SIGNUP, email)
        }

    override fun isEmailVerified(): Boolean =
        client.auth.currentUserOrNull()?.emailConfirmedAt != null

    override fun daysSinceSignup(): Long {
        val createdAt = client.auth.currentUserOrNull()?.createdAt ?: return 0L
        return (Clock.System.now() - createdAt).inWholeDays
    }

    override suspend fun registerPasskey(): Result<Unit> =
        TODO("Not yet implemented — blocked on DevOps passkey domain prerequisites, see tdd-splash-auth-07222026.md, Passkeys on KMP")

    override suspend fun signInWithPasskey(): Result<Unit> =
        TODO("Not yet implemented — blocked on DevOps passkey domain prerequisites, see tdd-splash-auth-07222026.md, Passkeys on KMP")

    override fun hasPasskeyEnrolled(): Boolean =
        TODO("Not yet implemented — blocked on DevOps passkey domain prerequisites, see tdd-splash-auth-07222026.md, passkey_enrolled key")

    /**
     * [DeepLinkHandler] has already validated [url] against
     * [DeepLinkValidator]'s allow-list before this is ever called — both
     * `basil://reset-password` and `basil://auth/callback` carry the PKCE
     * exchange code as a `code` query parameter, so the same exchange call
     * handles both link shapes.
     */
    override suspend fun handleDeepLink(url: String): Result<Unit> =
        runCatching {
            val code = requireNotNull(Url(url).parameters["code"]) { "Deep link had no code parameter: $url" }
            client.auth.exchangeCodeForSession(code)
        }.map {}

    private object Keys {
        const val LAST_USED_EMAIL = "stored_auth_email"
    }

    override fun lastUsedEmail(): String? = settings.getStringOrNull(Keys.LAST_USED_EMAIL)

    override fun recordLastUsedEmail(email: String) {
        settings.putString(Keys.LAST_USED_EMAIL, email)
    }
}
