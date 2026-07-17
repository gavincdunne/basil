package org.weekendware.basil.di

import org.koin.core.module.Module
import org.koin.dsl.module
import org.weekendware.basil.data.local.database.DatabaseDriverFactory
import org.weekendware.basil.data.local.database.DatabaseKeyProvider

/**
 * iOS implementation of [platformModule].
 *
 * Registers [DatabaseKeyProvider] (Keychain) and [DatabaseDriverFactory]
 * (SQLCipher-backed) as singletons. No additional platform dependencies required
 * beyond the iOS Keychain (always available) and SQLCipher SPM package (see
 * [DatabaseDriverFactory] for the Xcode linking requirement).
 */
actual val platformModule: Module = module {
    single { DatabaseKeyProvider() }
    single { DatabaseDriverFactory(get()) }
}
