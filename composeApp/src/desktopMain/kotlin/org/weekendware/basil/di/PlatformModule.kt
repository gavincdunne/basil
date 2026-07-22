package org.weekendware.basil.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import org.koin.core.module.Module
import org.koin.dsl.module
import org.weekendware.basil.data.local.ONBOARDING_DATASTORE_FILE
import org.weekendware.basil.data.local.createOnboardingDataStore
import org.weekendware.basil.data.local.database.DatabaseDriverFactory

actual val platformModule: Module = module {
    single { DatabaseDriverFactory() }
    single<DataStore<Preferences>> {
        val dir = System.getProperty("user.home") + "/.basil"
        java.io.File(dir).mkdirs()
        createOnboardingDataStore("$dir/$ONBOARDING_DATASTORE_FILE")
    }
}
