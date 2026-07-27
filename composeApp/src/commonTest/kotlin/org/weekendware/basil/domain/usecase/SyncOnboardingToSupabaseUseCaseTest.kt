package org.weekendware.basil.domain.usecase

import kotlinx.coroutines.test.runTest
import org.weekendware.basil.data.repository.FakeOnboardingLocalRepository
import org.weekendware.basil.data.repository.FakeProfileRepository
import org.weekendware.basil.data.repository.FakeUserRepository
import org.weekendware.basil.domain.model.DiagnosisDuration
import org.weekendware.basil.domain.model.Goal
import org.weekendware.basil.domain.model.ManagementType
import org.weekendware.basil.domain.model.OnboardingPersistedState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SyncOnboardingToSupabaseUseCaseTest {

    private val onboardingLocalRepo = FakeOnboardingLocalRepository()
    private val profileRepo = FakeProfileRepository()
    private val userRepo = FakeUserRepository()
    private val useCase = SyncOnboardingToSupabaseUseCase(onboardingLocalRepo, profileRepo, userRepo)

    @Test
    fun `pushes the completed local onboarding state to the profile repository`() = runTest {
        onboardingLocalRepo.setState(
            OnboardingPersistedState(
                name = "Gavin",
                managementType = ManagementType.PUMP,
                diagnosisDuration = DiagnosisDuration.ONE_TO_FIVE,
                goal = Goal.PATTERNS,
                isComplete = true,
            )
        )

        val result = useCase("uid-123", "gavin@example.com")

        assertTrue(result.isSuccess)
        assertEquals(1, profileRepo.upsertCalls.size)
        assertEquals("Gavin", profileRepo.upsertCalls.single().name)
    }

    @Test
    fun `inserts the local user row on successful sync`() = runTest {
        onboardingLocalRepo.setState(OnboardingPersistedState(name = "Gavin", isComplete = true))

        useCase("uid-123", "gavin@example.com")

        val insert = userRepo.insertCalls.single()
        assertEquals("uid-123", insert.id)
        assertEquals("Gavin", insert.name)
        assertEquals("gavin@example.com", insert.email)
    }

    @Test
    fun `does not insert a user row when the profile upsert fails`() = runTest {
        onboardingLocalRepo.setState(OnboardingPersistedState(name = "Gavin", isComplete = true))
        profileRepo.upsertResult = Result.failure(Exception("network error"))

        val result = useCase("uid-123", "gavin@example.com")

        assertTrue(result.isFailure)
        assertTrue(userRepo.insertCalls.isEmpty())
    }

    @Test
    fun `blank name falls back to an empty string rather than the literal null`() = runTest {
        onboardingLocalRepo.setState(OnboardingPersistedState(name = null, isComplete = true))

        useCase("uid-123", "gavin@example.com")

        assertEquals("", userRepo.insertCalls.single().name)
    }
}
