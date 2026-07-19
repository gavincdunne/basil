package org.weekendware.basil.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import org.koin.core.module.Module
import org.koin.dsl.module
import org.weekendware.basil.data.local.ONBOARDING_DATASTORE_FILE
import org.weekendware.basil.data.local.createOnboardingDataStore
import org.weekendware.basil.data.local.database.DatabaseDriverFactory
import org.weekendware.basil.data.local.database.DatabaseKeyProvider

actual val platformModule: Module = module {
    single { DatabaseKeyProvider(get<Context>()) }
    single { DatabaseDriverFactory(get<Context>(), get()) }
    single<DataStore<Preferences>> {
        val context = get<Context>()
        createOnboardingDataStore(context.filesDir.resolve(ONBOARDING_DATASTORE_FILE).absolutePath)
    }
}
