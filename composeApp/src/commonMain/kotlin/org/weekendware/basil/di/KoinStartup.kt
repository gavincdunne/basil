package org.weekendware.basil.di

import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

/**
 * Initialises the Koin dependency injection container.
 *
 * Called once at app startup from each platform's entry point:
 * - **Android** — [MainActivity.onCreate], which also passes the
 *   `applicationContext` via [appDeclaration] for [DatabaseDriverFactory].
 * - **iOS** — [MainViewController] before [ComposeUIViewController] is created.
 * - **Desktop** — [main] before the Compose window is shown.
 *
 * The loaded modules are:
 * 1. [platformModule] — platform-specific bindings (e.g. [DatabaseDriverFactory]).
 * 2. [databaseModule] — [BasilDatabase] and all repositories.
 * 3. [useCaseModule] — all use cases.
 * 4. [sharedModule] — all ViewModels.
 *
 * @param appDeclaration Optional platform-level Koin configuration. Android
 *   uses this to register `Context` as a Koin singleton so [DatabaseDriverFactory]
 *   can receive it via `get()`.
 */
fun initKoin(appDeclaration: KoinAppDeclaration = {}) = startKoin {
    appDeclaration()
    modules(platformModule, databaseModule, useCaseModule, sharedModule)
}
