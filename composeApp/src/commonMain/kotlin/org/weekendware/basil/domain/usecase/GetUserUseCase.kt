package org.weekendware.basil.domain.usecase

import org.weekendware.basil.data.repository.UserRepository
import org.weekendware.basil.domain.model.User

/**
 * Returns the current user, or null if no user has been stored locally yet.
 *
 * @param userRepository Source of truth for user data.
 */
class GetUserUseCase(private val userRepository: UserRepository) {
    operator fun invoke(): User? = userRepository.getAll().firstOrNull()
}
