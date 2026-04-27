package org.weekendware.basil.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email

/**
 * [AuthRepository] implementation backed by Supabase Auth.
 *
 * Session persistence is handled automatically by the supabase-kt Auth
 * plugin — tokens are stored in platform-native secure storage and
 * restored on the next app launch.
 */
class SupabaseAuthRepository(
    private val client: SupabaseClient
) : AuthRepository {

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
