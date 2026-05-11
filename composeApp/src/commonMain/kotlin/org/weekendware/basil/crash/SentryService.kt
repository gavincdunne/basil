package org.weekendware.basil.crash

import io.sentry.kotlin.multiplatform.Sentry
import org.weekendware.basil.BuildKonfig

/**
 * Initialises the Sentry SDK and wires [PhiScrubber] so that PHI is stripped
 * from every event and breadcrumb before it leaves the device.
 */
fun initSentry() {
    Sentry.init { options ->
        options.dsn         = BuildKonfig.SENTRY_DSN
        options.environment = BuildKonfig.FLAVOR
        options.debug       = BuildKonfig.FLAVOR == "dev"

        options.beforeSend        = { event      -> PhiScrubber.scrubEvent(event) }
        options.beforeBreadcrumb  = { breadcrumb -> PhiScrubber.scrubBreadcrumb(breadcrumb) }
    }
}
