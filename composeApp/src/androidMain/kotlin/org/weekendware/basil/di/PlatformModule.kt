package org.weekendware.basil.di

import android.content.Context
import org.koin.core.module.Module
import org.koin.dsl.module
import org.weekendware.basil.data.local.database.DatabaseDriverFactory

actual val platformModule: Module = module {
    single { DatabaseDriverFactory(get<Context>()) }
}
