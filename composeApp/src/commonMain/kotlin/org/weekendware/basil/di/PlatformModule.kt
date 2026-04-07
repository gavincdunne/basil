package org.weekendware.basil.di

import org.koin.core.module.Module

/**
 * Platform-specific Koin module declaration.
 *
 * Each platform provides an `actual` implementation that registers
 * platform-specific dependencies:
 * - **Android** — binds [DatabaseDriverFactory] with an Android [Context].
 * - **iOS** — binds [DatabaseDriverFactory] with no dependencies.
 * - **Desktop** — binds [DatabaseDriverFactory] with no dependencies.
 *
 * Included in Koin startup by [initKoin].
 */
expect val platformModule: Module
