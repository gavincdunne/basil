package org.weekendware.basil.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import org.koin.core.module.Module
import org.koin.dsl.module
import org.weekendware.basil.data.local.ONBOARDING_DATASTORE_FILE
import org.weekendware.basil.data.local.createOnboardingDataStore
import org.weekendware.basil.data.local.database.DatabaseDriverFactory
import org.weekendware.basil.data.local.database.DatabaseKeyProvider
import platform.Foundation.NSHomeDirectory

actual val platformModule: Module = module {
    single { DatabaseKeyProvider() }
    single { DatabaseDriverFactory(get()) }
    single<DataStore<Preferences>> {
        createOnboardingDataStore("${NSHomeDirectory()}/Documents/$ONBOARDING_DATASTORE_FILE")
    }
}
