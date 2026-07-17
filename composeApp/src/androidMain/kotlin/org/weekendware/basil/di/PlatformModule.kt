package org.weekendware.basil.di

import android.content.Context
import org.koin.core.module.Module
import org.koin.dsl.module
import org.weekendware.basil.data.local.database.DatabaseDriverFactory
import org.weekendware.basil.data.local.database.DatabaseKeyProvider

/**
 * Android implementation of [platformModule].
 *
 * Registers [DatabaseKeyProvider] (Android Keystore) and [DatabaseDriverFactory]
 * (SQLCipher-backed), both as singletons. Context is resolved from Koin (registered
 * in [BasilApplication] via [initKoin]'s appDeclaration lambda).
 */
actual val platformModule: Module = module {
    single { DatabaseKeyProvider(get<Context>()) }
    single { DatabaseDriverFactory(get<Context>(), get()) }
}
