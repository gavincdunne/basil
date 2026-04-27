package org.weekendware.basil.crash

import io.sentry.kotlin.multiplatform.Sentry
import org.weekendware.basil.BuildKonfig

/**
 * Initialises the Sentry SDK.
 *
 * Called once at app startup from each platform's entry point, before
 * any user-facing UI is shown. DSN is injected from [BuildKonfig] at
 * compile time so dev / staging / prod each report to the correct project.
 *
 * Crashes and unhandled exceptions are captured automatically by the SDK
 * after this call. Use [Sentry.captureException] or [Sentry.captureMessage]
 * for manual capture at known error boundaries.
 */
fun initSentry() {
    Sentry.init { options ->
        options.dsn = BuildKonfig.SENTRY_DSN
        options.environment = BuildKonfig.FLAVOR
        options.debug = BuildKonfig.FLAVOR == "dev"
    }
}
