package org.weekendware.basil.domain.model

data class OnboardingPersistedState(
    val name: String? = null,
    val managementType: ManagementType? = null,
    val diagnosisDuration: DiagnosisDuration? = null,
    val goal: Goal? = null,
    val isComplete: Boolean = false
)
