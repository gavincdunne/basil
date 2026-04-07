package org.weekendware.basil.di

import org.koin.core.module.Module
import org.koin.dsl.module
import org.weekendware.basil.data.local.database.DatabaseDriverFactory

/**
 * iOS implementation of [platformModule].
 *
 * Binds [DatabaseDriverFactory] as a singleton. No platform dependencies
 * are required on iOS — the factory uses [NativeSqliteDriver] directly.
 */
actual val platformModule: Module = module {
    single { DatabaseDriverFactory() }
}
