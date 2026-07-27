package org.weekendware.basil.data.remote

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.compose.auth.ComposeAuth
import io.github.jan.supabase.compose.auth.appleNativeLogin
import io.github.jan.supabase.compose.auth.googleNativeLogin
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import org.weekendware.basil.BuildKonfig

/**
 * Creates and configures the shared [SupabaseClient] singleton.
 *
 * URL and anon key are injected from [BuildKonfig] at compile time, so
 * dev / staging / prod each connect to their own Supabase project without
 * any runtime switching logic.
 *
 * [ComposeAuth] enables native Google Sign-In (Android, via Credential
 * Manager) and native Apple Sign-In (iOS, via AuthenticationServices).
 * Every other platform/provider combination — iOS Google, Android Apple,
 * and all of Desktop — falls back to compose-auth's own standard OAuth
 * browser-redirect flow automatically; nothing extra to configure for that
 * on our side. `googleNativeLogin`'s `serverClientId` is the Web Client ID
 * from Google Cloud Console (the same one registered with Supabase's
 * Google provider), not an Android client ID.
 */
fun createSupabaseClient(): SupabaseClient = createSupabaseClient(
    supabaseUrl = BuildKonfig.SUPABASE_URL,
    supabaseKey = BuildKonfig.SUPABASE_ANON_KEY
) {
    install(Auth)
    install(Storage)
    install(Postgrest)
    install(ComposeAuth) {
        googleNativeLogin(serverClientId = BuildKonfig.GOOGLE_WEB_CLIENT_ID)
        appleNativeLogin()
    }
}
