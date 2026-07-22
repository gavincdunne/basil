package org.weekendware.basil

import androidx.compose.ui.window.ComposeUIViewController
import org.koin.core.error.KoinApplicationAlreadyStartedException
import org.weekendware.basil.crash.initSentry
import org.weekendware.basil.di.initKoin

/**
 * Creates the iOS [UIViewController] that hosts the Basil Compose UI.
 *
 * Called from the Swift/Objective-C app entry point via `UIViewControllerRepresentable`.
 * Koin and Sentry are initialised here before [ComposeUIViewController] is created.
 * Koin initialisation is guarded against double-calls so that SwiftUI previews that
 * instantiate this controller more than once do not throw [KoinApplicationAlreadyStartedException].
 * Koin 4.x: GlobalContext is not a directly importable object on Kotlin/Native — use try-catch.
 *
 * @return A [UIViewController] rendering the full [App] composable.
 */
fun MainViewController() = run {
    initSentry()
    try {
        initKoin()
    } catch (_: KoinApplicationAlreadyStartedException) {
        // Safe: SwiftUI previews may reconstruct this controller more than once.
    }
    ComposeUIViewController { App() }
}
