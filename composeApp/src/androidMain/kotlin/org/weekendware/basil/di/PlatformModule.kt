package org.weekendware.basil.di

import android.content.Context
import org.koin.core.module.Module
import org.koin.dsl.module
import org.weekendware.basil.data.local.database.DatabaseDriverFactory

/**
 * Android implementation of [platformModule].
 *
 * Binds [DatabaseDriverFactory] as a singleton, resolving the Android
 * [Context] from Koin (registered in [MainActivity] via `initKoin`'s
 * `appDeclaration` lambda).
 */
actual val platformModule: Module = module {
    single { DatabaseDriverFactory(get<Context>()) }
}
