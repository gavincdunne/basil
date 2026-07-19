package org.weekendware.basil.data.repository

import org.weekendware.basil.domain.model.OnboardingPersistedState

class FakeProfileRepository : ProfileRepository {

    var fetchResult: Result<OnboardingPersistedState?> = Result.success(null)
    var upsertResult: Result<Unit> = Result.success(Unit)

    var fetchCallCount = 0
    val upsertCalls = mutableListOf<OnboardingPersistedState>()

    override suspend fun fetchProfile(userId: String): Result<OnboardingPersistedState?> {
        fetchCallCount++
        return fetchResult
    }

    override suspend fun upsertStep(userId: String, state: OnboardingPersistedState): Result<Unit> {
        upsertCalls.add(state)
        return upsertResult
    }
}
