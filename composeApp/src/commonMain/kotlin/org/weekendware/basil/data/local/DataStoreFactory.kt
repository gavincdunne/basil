package org.weekendware.basil.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import okio.Path.Companion.toPath

internal const val ONBOARDING_DATASTORE_FILE = "basil_onboarding.preferences_pb"

fun createOnboardingDataStore(filePath: String): DataStore<Preferences> =
    PreferenceDataStoreFactory.createWithPath(
        produceFile = { filePath.toPath() }
    )
