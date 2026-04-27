package org.weekendware.basil

import androidx.compose.ui.window.ComposeUIViewController
import org.weekendware.basil.crash.initSentry
import org.weekendware.basil.di.initKoin

/**
 * Creates the iOS [UIViewController] that hosts the Basil Compose UI.
 *
 * Called from the Swift/Objective-C app entry point (typically `AppDelegate`
 * or a SwiftUI `UIViewControllerRepresentable`). Koin is initialised here
 * before [ComposeUIViewController] is created so that the DI graph is ready
 * when the first composable renders.
 *
 * **Note:** If [MainViewController] can be called more than once in your iOS
 * integration (e.g. during SwiftUI previews), guard against double-initialisation
 * of Koin by checking `getKoinApplicationOrNull()` before calling [initKoin].
 *
 * @return A [UIViewController] rendering the full [App] composable.
 */
fun MainViewController() = run {
    initSentry()
    initKoin()
    ComposeUIViewController { App() }
}
