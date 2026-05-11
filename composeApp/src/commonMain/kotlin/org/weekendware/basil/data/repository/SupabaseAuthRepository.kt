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

    override fun isSignedIn(): Boolean =
        client.auth.currentUserOrNull() != null
}
