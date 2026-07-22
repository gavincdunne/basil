package org.weekendware.basil.presentation.onboarding

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import basil.composeapp.generated.resources.Res
import basil.composeapp.generated.resources.onboarding_error_network
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
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

private const val TYPING_DELAY_MS = 1200L

@Immutable
data class OnboardingUiState(
    val name: String? = null,
    val managementType: ManagementType? = null,
    val diagnosisDuration: DiagnosisDuration? = null,
    val goal: Goal? = null,
    val isComplete: Boolean = false,
    val completedThisSession: Boolean = false,
    val isLoading: Boolean = false,
    val isTyping: Boolean = false,
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

    // Cached at startup — avoids repeated currentUserOrNull() calls that can
    // return null during brief supabase-kt session-restoration windows.
    private var storedUserId: String? = null

    // Start loading so the UI shows a spinner while we determine the user's path.
    private val _state = MutableStateFlow(OnboardingUiState(isLoading = true))
    val state: StateFlow<OnboardingUiState> = _state

    init {
        scope.launch { loadStartupState() }
    }

    private suspend fun loadStartupState() {
        storedUserId = authRepo.currentUserId()
        val userId = storedUserId ?: run {
            _state.update { it.copy(isLoading = false) }
            return
        }

        profileRepo.fetchProfile(userId).fold(
            onSuccess = { remote ->
                if (remote != null) {
                    seedLocalFromRemote(remote)
                    val seeded = localRepo.state.first()
                    // Repopulate the local users table for returning users.
                    // Desktop uses in-memory SQLite which clears on every launch,
                    // so ProfileViewModel.getUser() would otherwise return null.
                    if (seeded.isComplete) {
                        userRepo.insert(
                            id = userId,
                            name = seeded.name.orEmpty(),
                            email = authRepo.currentUserEmail().orEmpty()
                        )
                    }
                    _state.update {
                        when {
                            seeded.isComplete -> it.copy(
                                name = seeded.name,
                                managementType = seeded.managementType,
                                diagnosisDuration = seeded.diagnosisDuration,
                                goal = seeded.goal,
                                isComplete = true,
                                isLoading = false,
                            )
                            seeded.name != null -> it.copy(
                                name = seeded.name,
                                managementType = seeded.managementType,
                                diagnosisDuration = seeded.diagnosisDuration,
                                goal = seeded.goal,
                                isResuming = true,
                                isLoading = false,
                            )
                            else -> it.copy(isLoading = false)
                        }
                    }
                } else {
                    // No Supabase row = new user.
                    localRepo.clear()
                    _state.update { it.copy(isLoading = false) }
                }
            },
            onFailure = {
                // Network error — fall back to local state for offline returning users.
                val localState = localRepo.state.first()
                if (localState.isComplete) {
                    userRepo.insert(
                        id = userId,
                        name = localState.name.orEmpty(),
                        email = authRepo.currentUserEmail().orEmpty()
                    )
                }
                _state.update {
                    when {
                        localState.isComplete -> it.copy(
                            name = localState.name,
                            managementType = localState.managementType,
                            diagnosisDuration = localState.diagnosisDuration,
                            goal = localState.goal,
                            isComplete = true,
                            isLoading = false,
                        )
                        localState.name != null -> it.copy(
                            name = localState.name,
                            managementType = localState.managementType,
                            diagnosisDuration = localState.diagnosisDuration,
                            goal = localState.goal,
                            isResuming = true,
                            isLoading = false,
                        )
                        else -> it.copy(isLoading = false)
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
        scope.launch {
            _state.update { it.copy(name = name, isTyping = true, error = null) }
            localRepo.saveName(name)
            val userId = storedUserId ?: run {
                _state.update { it.copy(isTyping = false) }
                return@launch
            }
            profileRepo.upsertStep(userId, OnboardingPersistedState(name = name)).fold(
                onSuccess = {
                    delay(TYPING_DELAY_MS)
                    _state.update { it.copy(isTyping = false) }
                },
                onFailure = {
                    _state.update { it.copy(isTyping = false, error = Res.string.onboarding_error_network) }
                }
            )
        }
    }

    fun onManagementTypeSelected(type: ManagementType) {
        scope.launch {
            val current = _state.value
            _state.update { it.copy(managementType = type, isTyping = true, error = null) }
            localRepo.saveManagementType(type)
            val userId = storedUserId ?: run {
                _state.update { it.copy(isTyping = false) }
                return@launch
            }
            profileRepo.upsertStep(userId, OnboardingPersistedState(
                name = current.name,
                managementType = type
            )).fold(
                onSuccess = {
                    delay(TYPING_DELAY_MS)
                    _state.update { it.copy(isTyping = false) }
                },
                onFailure = {
                    _state.update { it.copy(isTyping = false, error = Res.string.onboarding_error_network) }
                }
            )
        }
    }

    fun onDiagnosisDurationSelected(duration: DiagnosisDuration) {
        scope.launch {
            val current = _state.value
            _state.update { it.copy(diagnosisDuration = duration, isTyping = true, error = null) }
            localRepo.saveDiagnosisDuration(duration)
            val userId = storedUserId ?: run {
                _state.update { it.copy(isTyping = false) }
                return@launch
            }
            profileRepo.upsertStep(userId, OnboardingPersistedState(
                name = current.name,
                managementType = current.managementType,
                diagnosisDuration = duration
            )).fold(
                onSuccess = {
                    delay(TYPING_DELAY_MS)
                    _state.update { it.copy(isTyping = false) }
                },
                onFailure = {
                    _state.update { it.copy(isTyping = false, error = Res.string.onboarding_error_network) }
                }
            )
        }
    }

    fun onGoalSelected(goal: Goal) {
        scope.launch {
            val current = _state.value
            _state.update { it.copy(goal = goal, isTyping = true, error = null) }
            localRepo.saveGoal(goal)
            localRepo.markComplete()
            val userId = storedUserId
            val finalState = OnboardingPersistedState(
                name = current.name,
                managementType = current.managementType,
                diagnosisDuration = current.diagnosisDuration,
                goal = goal,
                isComplete = true
            )
            if (userId != null) {
                profileRepo.upsertStep(userId, finalState).fold(
                    onSuccess = {
                        userRepo.insert(
                            id = userId,
                            name = current.name.orEmpty(),
                            email = authRepo.currentUserEmail().orEmpty()
                        )
                    },
                    onFailure = {
                        _state.update { it.copy(error = Res.string.onboarding_error_network) }
                    }
                )
            }
            delay(TYPING_DELAY_MS)
            _state.update { it.copy(isTyping = false, isComplete = true, completedThisSession = true) }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}
