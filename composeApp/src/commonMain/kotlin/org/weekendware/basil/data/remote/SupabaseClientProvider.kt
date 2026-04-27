package org.weekendware.basil.data.remote

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import org.weekendware.basil.BuildKonfig

/**
 * Creates and configures the shared [SupabaseClient] singleton.
 *
 * URL and anon key are injected from [BuildKonfig] at compile time, so
 * dev / staging / prod each connect to their own Supabase project without
 * any runtime switching logic.
 */
fun createSupabaseClient(): SupabaseClient = createSupabaseClient(
    supabaseUrl = BuildKonfig.SUPABASE_URL,
    supabaseKey = BuildKonfig.SUPABASE_ANON_KEY
) {
    install(Auth)
}
