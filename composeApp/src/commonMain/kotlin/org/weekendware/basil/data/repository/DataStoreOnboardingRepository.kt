package org.weekendware.basil.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.weekendware.basil.domain.model.DiagnosisDuration
import org.weekendware.basil.domain.model.Goal
import org.weekendware.basil.domain.model.ManagementType
import org.weekendware.basil.domain.model.OnboardingPersistedState

class DataStoreOnboardingRepository(
    private val dataStore: DataStore<Preferences>
) : OnboardingLocalRepository {

    private object Keys {
        val NAME = stringPreferencesKey("onboarding_name")
        val MANAGEMENT_TYPE = stringPreferencesKey("onboarding_management_type")
        val DIAGNOSIS_DURATION = stringPreferencesKey("onboarding_diagnosis_duration")
        val GOAL = stringPreferencesKey("onboarding_goal")
        val IS_COMPLETE = booleanPreferencesKey("onboarding_is_complete")
    }

    override val state: Flow<OnboardingPersistedState> = dataStore.data.map { prefs ->
        OnboardingPersistedState(
            name = prefs[Keys.NAME],
            managementType = prefs[Keys.MANAGEMENT_TYPE]?.let {
                runCatching { ManagementType.valueOf(it) }.getOrNull()
            },
            diagnosisDuration = prefs[Keys.DIAGNOSIS_DURATION]?.let {
                runCatching { DiagnosisDuration.valueOf(it) }.getOrNull()
            },
            goal = prefs[Keys.GOAL]?.let {
                runCatching { Goal.valueOf(it) }.getOrNull()
            },
            isComplete = prefs[Keys.IS_COMPLETE] ?: false
        )
    }

    override suspend fun saveName(name: String): Result<Unit> =
        runCatching { dataStore.edit { it[Keys.NAME] = name } }

    override suspend fun saveManagementType(type: ManagementType): Result<Unit> =
        runCatching { dataStore.edit { it[Keys.MANAGEMENT_TYPE] = type.name } }

    override suspend fun saveDiagnosisDuration(duration: DiagnosisDuration): Result<Unit> =
        runCatching { dataStore.edit { it[Keys.DIAGNOSIS_DURATION] = duration.name } }

    override suspend fun saveGoal(goal: Goal): Result<Unit> =
        runCatching { dataStore.edit { it[Keys.GOAL] = goal.name } }

    override suspend fun markComplete(): Result<Unit> =
        runCatching { dataStore.edit { it[Keys.IS_COMPLETE] = true } }

    override suspend fun clear(): Result<Unit> =
        runCatching { dataStore.edit { it.clear() } }
}
