package org.weekendware.basil.presentation.onboarding

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import basil.composeapp.generated.resources.Res
import basil.composeapp.generated.resources.onboarding_error_network
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.weekendware.basil.data.repository.AuthRepository
import org.weekendware.basil.data.repository.OnboardingLocalRepository
import org.weekendware.basil.data.repository.ProfileRepository
import org.weekendware.basil.data.repository.UserRepository
import org.weekendware.basil.domain.model.DiagnosisDuration
import org.weekendware.basil.domain.model.Goal
import org.weekendware.basil.domain.model.ManagementType
import org.weekendware.basil.domain.model.OnboardingPersistedState

enum class OnboardingStep { NAME, MANAGEMENT_TYPE, DIAGNOSIS_DURATION, GOAL, COMPLETE }

@Immutable
data class OnboardingUiState(
    val name: String? = null,
    val managementType: ManagementType? = null,
    val diagnosisDuration: DiagnosisDuration? = null,
    val goal: Goal? = null,
    val isComplete: Boolean = false,
    val isLoading: Boolean = false,
    val isResuming: Boolean = false,
    val error: StringResource? = null
) {
    val currentStep: OnboardingStep
        get() = when {
            name == null              -> OnboardingStep.NAME
            managementType == null    -> OnboardingStep.MANAGEMENT_TYPE
            diagnosisDuration == null -> OnboardingStep.DIAGNOSIS_DURATION
            goal == null              -> OnboardingStep.GOAL
            else                      -> OnboardingStep.COMPLETE
        }
}

class OnboardingViewModel(
    private val localRepo: OnboardingLocalRepository,
    private val profileRepo: ProfileRepository,
    private val authRepo: AuthRepository,
    private val userRepo: UserRepository,
    coroutineScope: CoroutineScope? = null
) : ViewModel() {

    private val scope = coroutineScope ?: viewModelScope

    private val _state = MutableStateFlow(OnboardingUiState())
    val state: StateFlow<OnboardingUiState> = _state

    init {
        scope.launch { loadStartupState() }
    }

    private suspend fun loadStartupState() {
        val localState = localRepo.state.first()

        if (localState.isComplete) {
            _state.update { it.copy(isComplete = true) }
            return
        }

        val hasLocalProgress = localState.name != null

        val userId = authRepo.currentUserId() ?: return

        profileRepo.fetchProfile(userId).fold(
            onSuccess = { remote ->
                if (remote != null) {
                    seedLocalFromRemote(remote)
                    val seeded = localRepo.state.first()
                    if (seeded.isComplete) {
                        _state.update { it.copy(isComplete = true) }
                    } else if (seeded.name != null) {
                        _state.update {
                            it.copy(
                                name = seeded.name,
                                managementType = seeded.managementType,
                                diagnosisDuration = seeded.diagnosisDuration,
                                goal = seeded.goal,
                                isResuming = true
                            )
                        }
                    }
                } else if (hasLocalProgress) {
                    _state.update {
                        it.copy(
                            name = localState.name,
                            managementType = localState.managementType,
                            diagnosisDuration = localState.diagnosisDuration,
                            goal = localState.goal,
                            isResuming = true
                        )
                    }
                }
            },
            onFailure = {
                if (hasLocalProgress) {
                    _state.update {
                        it.copy(
                            name = localState.name,
                            managementType = localState.managementType,
                            diagnosisDuration = localState.diagnosisDuration,
                            goal = localState.goal,
                            isResuming = true
                        )
                    }
                }
            }
        )
    }

    private suspend fun seedLocalFromRemote(remote: OnboardingPersistedState) {
        remote.name?.let { localRepo.saveName(it) }
        remote.managementType?.let { localRepo.saveManagementType(it) }
        remote.diagnosisDuration?.let { localRepo.saveDiagnosisDuration(it) }
        remote.goal?.let { localRepo.saveGoal(it) }
        if (remote.isComplete) localRepo.markComplete()
    }

    fun onNameSubmitted(name: String) {
        if (name.isBlank()) return
        val userId = authRepo.currentUserId() ?: return
        scope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val pending = OnboardingPersistedState(name = name)
            profileRepo.upsertStep(userId, pending).fold(
                onSuccess = {
                    localRepo.saveName(name)
                    _state.update { it.copy(name = name, isLoading = false) }
                },
                onFailure = {
                    _state.update { it.copy(isLoading = false, error = Res.string.onboarding_error_network) }
                }
            )
        }
    }

    fun onManagementTypeSelected(type: ManagementType) {
        val userId = authRepo.currentUserId() ?: return
        scope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val current = _state.value
            val pending = OnboardingPersistedState(
                name = current.name,
                managementType = type
            )
            profileRepo.upsertStep(userId, pending).fold(
                onSuccess = {
                    localRepo.saveManagementType(type)
                    _state.update { it.copy(managementType = type, isLoading = false) }
                },
                onFailure = {
                    _state.update { it.copy(isLoading = false, error = Res.string.onboarding_error_network) }
                }
            )
        }
    }

    fun onDiagnosisDurationSelected(duration: DiagnosisDuration) {
        val userId = authRepo.currentUserId() ?: return
        scope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val current = _state.value
            val pending = OnboardingPersistedState(
                name = current.name,
                managementType = current.managementType,
                diagnosisDuration = duration
            )
            profileRepo.upsertStep(userId, pending).fold(
                onSuccess = {
                    localRepo.saveDiagnosisDuration(duration)
                    _state.update { it.copy(diagnosisDuration = duration, isLoading = false) }
                },
                onFailure = {
                    _state.update { it.copy(isLoading = false, error = Res.string.onboarding_error_network) }
                }
            )
        }
    }

    fun onGoalSelected(goal: Goal) {
        val userId = authRepo.currentUserId() ?: return
        scope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val current = _state.value
            val finalState = OnboardingPersistedState(
                name = current.name,
                managementType = current.managementType,
                diagnosisDuration = current.diagnosisDuration,
                goal = goal,
                isComplete = true
            )
            profileRepo.upsertStep(userId, finalState).fold(
                onSuccess = {
                    localRepo.saveGoal(goal)
                    localRepo.markComplete()
                    userRepo.insert(
                        id = userId,
                        name = current.name.orEmpty(),
                        email = authRepo.currentUserEmail().orEmpty()
                    )
                    _state.update { it.copy(goal = goal, isComplete = true, isLoading = false) }
                },
                onFailure = {
                    _state.update { it.copy(isLoading = false, error = Res.string.onboarding_error_network) }
                }
            )
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}
