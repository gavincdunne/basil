package org.weekendware.basil.data.repository

/**
 * Validates an incoming deep link URL against the exact allow-list before
 * any platform `DeepLinkHandler` passes it to
 * `client.auth.exchangeCodeForSession(url)`.
 *
 * Only two link shapes are ever valid: a password-reset link or an OAuth
 * callback. Anything else — including a crafted URL where the attacker
 * controls the trailing path or host segment — must be rejected. This
 * prevents an open-redirect-style attack where a lookalike deep link tricks
 * the app into exchanging an attacker-controlled code.
 *
 * See [DeepLinkValidatorTest] for the full contract, including the
 * boundary case that rules out a naive `startsWith` implementation.
 */
object DeepLinkValidator {

    /** The only two deep link shapes Basil ever handles. */
    private const val RESET_PASSWORD = "basil://reset-password"
    private const val AUTH_CALLBACK = "basil://auth/callback"

    /**
     * True only if [url] is exactly one of the allowed link shapes, or that
     * shape followed by a query string (`?...`). A URL that merely starts
     * with the allowed string as raw characters — e.g.
     * `basil://reset-password-evil.com` — returns false: the allowed shape
     * must end at a path boundary (end of string or `?`), not continue with
     * arbitrary further characters.
     */
    fun isAllowed(url: String): Boolean =
        isExactMatch(url, RESET_PASSWORD) || isExactMatch(url, AUTH_CALLBACK)

    private fun isExactMatch(url: String, allowed: String): Boolean =
        url == allowed || url.startsWith("$allowed?")
}
