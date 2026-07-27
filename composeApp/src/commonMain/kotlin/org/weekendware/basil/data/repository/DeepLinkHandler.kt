package org.weekendware.basil.data.repository

/**
 * Shared entry point for an incoming deep link (password reset, OAuth
 * callback), invoked by each platform's native URL-receiving mechanism —
 * Android's `onNewIntent`, iOS's `.onOpenURL`, Desktop's local callback
 * server. Identical across platforms, so unlike [AuthRepository] this is a
 * plain class, not `expect`/`actual`.
 *
 * See [DeepLinkHandlerTest] for the full contract.
 */
class DeepLinkHandler(private val authRepository: AuthRepository) {

    /**
     * Validates [url] against [DeepLinkValidator] and, if allowed, hands it
     * to [AuthRepository.handleDeepLink]. A [url] outside the allow-list is
     * dropped silently — no exception, no crash.
     */
    suspend fun handle(url: String) {
        if (!DeepLinkValidator.isAllowed(url)) return
        authRepository.handleDeepLink(url)
    }
}
