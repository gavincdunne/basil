package org.weekendware.basil.crash

import io.sentry.kotlin.multiplatform.Sentry
import io.sentry.kotlin.multiplatform.SentryEvent
import io.sentry.kotlin.multiplatform.protocol.Breadcrumb
import org.weekendware.basil.BuildKonfig

/**
 * Initialises the Sentry SDK with PHI scrubbing applied to every event
 * before it leaves the device.
 *
 * ## What is scrubbed and why
 *
 * Sentry captures exception messages and breadcrumb payloads automatically.
 * Health values (BG readings, insulin doses, carbs) are numeric and can
 * appear in exception messages if a parse or validation fails mid-entry.
 * Rather than attempting to detect specific patterns — which is fragile —
 * we take the conservative approach:
 *
 * - **User identity** is removed so events cannot be linked to an individual.
 *   Stack traces and exception types are sufficient for debugging.
 *
 * - **Exception messages** (`value` field) are cleared for exceptions
 *   originating from health-data packages. Exception *type* and the full
 *   *stack trace* are preserved — they are the useful debugging artefacts.
 *
 * - **Breadcrumb data payloads** are cleared. Category and type
 *   (navigation, lifecycle) are kept; only the `data` map is wiped
 *   because it can carry form field values.
 *
 * - **Event contexts** are cleared to remove any ambient key/value context.
 */
fun initSentry() {
    Sentry.init { options ->
        options.dsn = BuildKonfig.SENTRY_DSN
        options.environment = BuildKonfig.FLAVOR
        options.debug = BuildKonfig.FLAVOR == "dev"

        options.beforeSend = { event -> scrubEvent(event) }
        options.beforeBreadcrumb = { breadcrumb -> scrubBreadcrumb(breadcrumb) }
    }
}

private val healthPackages = listOf("logging", "dashboard", "data", "log", "auth")

private fun scrubEvent(event: SentryEvent): SentryEvent {
    // Remove user identity.
    event.user = null

    // Clear exception messages from health-related packages.
    // SentryException is immutable, so replace the list with scrubbed copies.
    val scrubbed = event.exceptions?.map { exception ->
        val module = exception.module.orEmpty()
        if (healthPackages.any { module.contains(it, ignoreCase = true) }) {
            exception.copy(value = null)
        } else {
            exception
        }
    }
    if (scrubbed != null) event.exceptions = scrubbed.toMutableList()

    // Clear ambient context key/value pairs.
    event.contexts = mutableMapOf()

    return event
}

private fun scrubBreadcrumb(breadcrumb: Breadcrumb): Breadcrumb {
    // Wipe the data payload from every breadcrumb. Category and type are
    // kept — they describe navigation/lifecycle without health values.
    breadcrumb.setData(mutableMapOf<String, Any>())
    return breadcrumb
}
