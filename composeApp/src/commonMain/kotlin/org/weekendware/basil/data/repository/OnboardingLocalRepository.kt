package org.weekendware.basil.data.repository

import kotlinx.coroutines.flow.Flow
import org.weekendware.basil.domain.model.DiagnosisDuration
import org.weekendware.basil.domain.model.Goal
import org.weekendware.basil.domain.model.ManagementType
import org.weekendware.basil.domain.model.OnboardingPersistedState

interface OnboardingLocalRepository {
    val state: Flow<OnboardingPersistedState>
    suspend fun saveName(name: String): Result<Unit>
    suspend fun saveManagementType(type: ManagementType): Result<Unit>
    suspend fun saveDiagnosisDuration(duration: DiagnosisDuration): Result<Unit>
    suspend fun saveGoal(goal: Goal): Result<Unit>
    suspend fun markComplete(): Result<Unit>
    suspend fun clear(): Result<Unit>
}
