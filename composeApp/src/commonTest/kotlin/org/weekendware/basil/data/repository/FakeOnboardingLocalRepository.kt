package org.weekendware.basil.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import org.weekendware.basil.domain.model.DiagnosisDuration
import org.weekendware.basil.domain.model.Goal
import org.weekendware.basil.domain.model.ManagementType
import org.weekendware.basil.domain.model.OnboardingPersistedState

class FakeOnboardingLocalRepository : OnboardingLocalRepository {

    private val _state = MutableStateFlow(OnboardingPersistedState())
    override val state: Flow<OnboardingPersistedState> = _state

    var saveNameResult: Result<Unit> = Result.success(Unit)
    var saveManagementTypeResult: Result<Unit> = Result.success(Unit)
    var saveDiagnosisDurationResult: Result<Unit> = Result.success(Unit)
    var saveGoalResult: Result<Unit> = Result.success(Unit)
    var markCompleteResult: Result<Unit> = Result.success(Unit)

    val saveNameCalls = mutableListOf<String>()
    val saveManagementTypeCalls = mutableListOf<ManagementType>()
    val saveDiagnosisDurationCalls = mutableListOf<DiagnosisDuration>()
    val saveGoalCalls = mutableListOf<Goal>()
    var markCompleteCalled = false
    var clearCalled = false

    fun setState(state: OnboardingPersistedState) {
        _state.value = state
    }

    override suspend fun saveName(name: String): Result<Unit> {
        saveNameCalls.add(name)
        if (saveNameResult.isSuccess) _state.update { it.copy(name = name) }
        return saveNameResult
    }

    override suspend fun saveManagementType(type: ManagementType): Result<Unit> {
        saveManagementTypeCalls.add(type)
        if (saveManagementTypeResult.isSuccess) _state.update { it.copy(managementType = type) }
        return saveManagementTypeResult
    }

    override suspend fun saveDiagnosisDuration(duration: DiagnosisDuration): Result<Unit> {
        saveDiagnosisDurationCalls.add(duration)
        if (saveDiagnosisDurationResult.isSuccess) _state.update { it.copy(diagnosisDuration = duration) }
        return saveDiagnosisDurationResult
    }

    override suspend fun saveGoal(goal: Goal): Result<Unit> {
        saveGoalCalls.add(goal)
        if (saveGoalResult.isSuccess) _state.update { it.copy(goal = goal) }
        return saveGoalResult
    }

    override suspend fun markComplete(): Result<Unit> {
        markCompleteCalled = true
        if (markCompleteResult.isSuccess) _state.update { it.copy(isComplete = true) }
        return markCompleteResult
    }

    override suspend fun clear(): Result<Unit> {
        clearCalled = true
        _state.value = OnboardingPersistedState()
        return Result.success(Unit)
    }
}
