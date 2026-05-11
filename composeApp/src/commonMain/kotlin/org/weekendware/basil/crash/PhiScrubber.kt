package org.weekendware.basil.crash

import io.sentry.kotlin.multiplatform.SentryEvent
import io.sentry.kotlin.multiplatform.protocol.Breadcrumb

/**
 * Scrubs Protected Health Information (PHI) from Sentry events and breadcrumbs
 * before they leave the device.
 *
 * ## What is scrubbed and why
 *
 * Sentry captures exception messages and breadcrumb payloads automatically.
 * Health values (BG readings, insulin doses, carbs) are numeric and can appear
 * in exception messages if a parse or validation fails mid-entry. Rather than
 * attempting to detect specific patterns — which is fragile — we take the
 * conservative approach:
 *
 * - **User identity** is removed so events cannot be linked to an individual.
 *   Stack traces and exception types are sufficient for debugging.
 *
 * - **Exception messages** (`value` field) are cleared for exceptions originating
 *   from health-data packages. Exception *type* and the full *stack trace* are
 *   preserved — they are the useful debugging artefacts.
 *
 * - **Breadcrumb data payloads** are cleared. Category and type
 *   (navigation, lifecycle) are kept; only the `data` map is wiped because it
 *   can carry form field values.
 *
 * - **Event contexts** are cleared to remove any ambient key/value context.
 */
internal object PhiScrubber {

    private val healthPackages = listOf("logging", "dashboard", "data", "log", "auth")

    fun scrubEvent(event: SentryEvent): SentryEvent {
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

    fun scrubBreadcrumb(breadcrumb: Breadcrumb): Breadcrumb {
        // Wipe the data payload from every breadcrumb. Category and type
        // (navigation, lifecycle) are kept; only the data map is wiped
        // because it can carry form field values.
        breadcrumb.setData(mutableMapOf<String, Any>())
        return breadcrumb
    }
}
