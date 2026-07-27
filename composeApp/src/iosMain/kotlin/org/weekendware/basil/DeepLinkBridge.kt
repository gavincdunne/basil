package org.weekendware.basil

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.mp.KoinPlatform
import org.weekendware.basil.data.repository.DeepLinkHandler

/**
 * Entry point for `ContentView.swift`'s `.onOpenURL` — Swift has no
 * concept of Kotlin coroutines, so this exposes a plain synchronous
 * function that launches the suspend call itself. Mirrors
 * [MainViewController] as the other Kotlin declaration Swift calls into
 * directly (`DeepLinkBridgeKt.handleDeepLink(url)` from Swift).
 *
 * Fire-and-forget by design, same as Android's `lifecycleScope.launch` in
 * `MainActivity.handleDeepLink` — there is nothing to cancel or await from
 * the Swift side.
 */
@OptIn(DelicateCoroutinesApi::class)
fun handleDeepLink(url: String) {
    val deepLinkHandler = KoinPlatform.getKoin().get<DeepLinkHandler>()
    GlobalScope.launch {
        deepLinkHandler.handle(url)
    }
}
