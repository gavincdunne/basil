package org.weekendware.basil.di

import org.koin.core.module.Module
import org.koin.dsl.module
import org.weekendware.basil.data.local.database.DatabaseDriverFactory

/**
 * Desktop implementation of [platformModule].
 *
 * Binds [DatabaseDriverFactory] as a singleton. No platform dependencies
 * are required on desktop — the factory uses [JdbcSqliteDriver] directly.
 */
actual val platformModule: Module = module {
    single { DatabaseDriverFactory() }
}
