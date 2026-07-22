package org.weekendware.basil.data.repository

import org.weekendware.basil.domain.model.OnboardingPersistedState

interface ProfileRepository {
    suspend fun fetchProfile(userId: String): Result<OnboardingPersistedState?>
    suspend fun upsertStep(userId: String, state: OnboardingPersistedState): Result<Unit>
}
