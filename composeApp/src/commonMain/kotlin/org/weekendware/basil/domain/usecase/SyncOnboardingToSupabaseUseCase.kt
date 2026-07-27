package org.weekendware.basil.domain.usecase

import kotlinx.coroutines.flow.first
import org.weekendware.basil.data.repository.OnboardingLocalRepository
import org.weekendware.basil.data.repository.ProfileRepository
import org.weekendware.basil.data.repository.UserRepository

/**
 * Pushes the locally-completed onboarding answers to Supabase, once — called
 * exactly once, right after a successful sign-up on [org.weekendware.basil.presentation.onboarding.SaveProgressScreen].
 *
 * Under full silent account provisioning, the entire onboarding conversation
 * runs pre-auth with no `userId`, so [OnboardingViewModel][org.weekendware.basil.presentation.onboarding.OnboardingViewModel]
 * never had one to push to [ProfileRepository] with. This is that deferred
 * sync, run the moment an account — and therefore a `userId` — first exists.
 */
class SyncOnboardingToSupabaseUseCase(
    private val onboardingLocalRepository: OnboardingLocalRepository,
    private val profileRepository: ProfileRepository,
    private val userRepository: UserRepository,
) {
    suspend operator fun invoke(userId: String, email: String): Result<Unit> {
        val local = onboardingLocalRepository.state.first()
        return profileRepository.upsertStep(userId, local).onSuccess {
            userRepository.insert(id = userId, name = local.name.orEmpty(), email = email)
        }
    }
}
