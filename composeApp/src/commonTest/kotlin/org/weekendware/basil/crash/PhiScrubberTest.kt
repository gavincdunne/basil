package org.weekendware.basil.crash

import io.sentry.kotlin.multiplatform.SentryEvent
import io.sentry.kotlin.multiplatform.protocol.Breadcrumb
import io.sentry.kotlin.multiplatform.protocol.SentryException
import io.sentry.kotlin.multiplatform.protocol.User
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PhiScrubberTest {

    // ── scrubEvent: user identity ─────────────────────────────

    @Test
    fun `scrubEvent removes user identity`() {
        val event = SentryEvent().apply { user = User() }
        PhiScrubber.scrubEvent(event)
        assertNull(event.user)
    }

    @Test
    fun `scrubEvent clears event contexts`() {
        val event = SentryEvent().apply {
            contexts = mutableMapOf("device" to "iPhone", "os" to "iOS")
        }
        PhiScrubber.scrubEvent(event)
        assertTrue(event.contexts.isEmpty())
    }

    // ── scrubEvent: health package exception scrubbing ────────

    @Test
    fun `scrubEvent clears exception value for logging module`() {
        val event = eventWithException(module = "org.weekendware.basil.logging", value = "BG: 14.2")
        PhiScrubber.scrubEvent(event)
        assertNull(event.exceptions!!.first().value)
    }

    @Test
    fun `scrubEvent clears exception value for dashboard module`() {
        val event = eventWithException(module = "org.weekendware.basil.dashboard", value = "dose: 4u")
        PhiScrubber.scrubEvent(event)
        assertNull(event.exceptions!!.first().value)
    }

    @Test
    fun `scrubEvent clears exception value for data module`() {
        val event = eventWithException(module = "org.weekendware.basil.data", value = "carbs: 60g")
        PhiScrubber.scrubEvent(event)
        assertNull(event.exceptions!!.first().value)
    }

    @Test
    fun `scrubEvent clears exception value for log module`() {
        val event = eventWithException(module = "org.weekendware.basil.log", value = "reading 3.1")
        PhiScrubber.scrubEvent(event)
        assertNull(event.exceptions!!.first().value)
    }

    @Test
    fun `scrubEvent clears exception value for auth module`() {
        val event = eventWithException(module = "org.weekendware.basil.auth", value = "token hint")
        PhiScrubber.scrubEvent(event)
        assertNull(event.exceptions!!.first().value)
    }

    @Test
    fun `scrubEvent module matching is case insensitive`() {
        val event = eventWithException(module = "org.weekendware.basil.LOGGING", value = "BG: 5.4")
        PhiScrubber.scrubEvent(event)
        assertNull(event.exceptions!!.first().value)
    }

    // ── scrubEvent: non-health exceptions are preserved ───────

    @Test
    fun `scrubEvent preserves exception value for non-health module`() {
        val event = eventWithException(module = "org.weekendware.basil.ui", value = "layout error")
        PhiScrubber.scrubEvent(event)
        assertEquals("layout error", event.exceptions!!.first().value)
    }

    @Test
    fun `scrubEvent preserves exception type for health module`() {
        val event = eventWithException(module = "org.weekendware.basil.logging", type = "NumberFormatException")
        PhiScrubber.scrubEvent(event)
        assertEquals("NumberFormatException", event.exceptions!!.first().type)
    }

    @Test
    fun `scrubEvent preserves exception module field`() {
        val event = eventWithException(module = "org.weekendware.basil.logging")
        PhiScrubber.scrubEvent(event)
        assertEquals("org.weekendware.basil.logging", event.exceptions!!.first().module)
    }

    @Test
    fun `scrubEvent with null module does not scrub exception value`() {
        val event = eventWithException(module = null, value = "safe error")
        PhiScrubber.scrubEvent(event)
        assertEquals("safe error", event.exceptions!!.first().value)
    }

    // ── scrubEvent: mixed exception lists ─────────────────────

    @Test
    fun `scrubEvent only scrubs health exceptions in a mixed list`() {
        val event = SentryEvent().apply {
            exceptions = mutableListOf(
                SentryException(type = "A", value = "phi data",   module = "org.weekendware.basil.logging", threadId = null),
                SentryException(type = "B", value = "safe error", module = "org.weekendware.basil.ui",      threadId = null),
            )
        }
        PhiScrubber.scrubEvent(event)
        assertNull(event.exceptions!![0].value)
        assertEquals("safe error", event.exceptions!![1].value)
    }

    @Test
    fun `scrubEvent handles empty exceptions list gracefully`() {
        val event = SentryEvent().apply { exceptions = mutableListOf() }
        PhiScrubber.scrubEvent(event)   // must not throw
        assertTrue(event.exceptions!!.isEmpty())
    }

    // ── scrubBreadcrumb ───────────────────────────────────────

    @Test
    fun `scrubBreadcrumb clears data payload`() {
        val breadcrumb = Breadcrumb().apply {
            setData(mutableMapOf("bg" to 8.5, "insulin" to 2.0))
        }
        PhiScrubber.scrubBreadcrumb(breadcrumb)
        assertTrue(breadcrumb.getData()?.isEmpty() ?: true)
    }

    @Test
    fun `scrubBreadcrumb preserves category`() {
        val breadcrumb = Breadcrumb().apply {
            category = "navigation"
            setData(mutableMapOf("screen" to "Dashboard"))
        }
        PhiScrubber.scrubBreadcrumb(breadcrumb)
        assertEquals("navigation", breadcrumb.category)
    }

    @Test
    fun `scrubBreadcrumb preserves type`() {
        val breadcrumb = Breadcrumb().apply {
            type = "ui.click"
            setData(mutableMapOf("element" to "save_button"))
        }
        PhiScrubber.scrubBreadcrumb(breadcrumb)
        assertEquals("ui.click", breadcrumb.type)
    }

    @Test
    fun `scrubBreadcrumb preserves message`() {
        val breadcrumb = Breadcrumb().apply {
            message = "user navigated to dashboard"
            setData(mutableMapOf("bg" to 6.1))
        }
        PhiScrubber.scrubBreadcrumb(breadcrumb)
        assertEquals("user navigated to dashboard", breadcrumb.message)
    }

    // ── helpers ───────────────────────────────────────────────

    private fun eventWithException(
        module: String?,
        value: String? = "sensitive value",
        type: String  = "RuntimeException",
    ) = SentryEvent().apply {
        exceptions = mutableListOf(
            SentryException(type = type, value = value, module = module, threadId = null)
        )
    }
}
