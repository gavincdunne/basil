package org.weekendware.basil.domain.usecase

import org.weekendware.basil.data.repository.PreferencesRepository

/**
 * Persists the user's target BG range.
 *
 * @param preferencesRepository Destination for user preferences.
 */
class SetBgTargetsUseCase(private val preferencesRepository: PreferencesRepository) {
    operator fun invoke(low: Double, high: Double) {
        preferencesRepository.setBgTargetLow(low)
        preferencesRepository.setBgTargetHigh(high)
    }
}
