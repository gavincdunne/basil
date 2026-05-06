package org.weekendware.basil.domain.usecase

import org.weekendware.basil.data.repository.PreferencesRepository

/**
 * Returns the user's persisted target BG range as a [BgTargets] pair.
 *
 * @param preferencesRepository Source of truth for user preferences.
 */
class GetBgTargetsUseCase(private val preferencesRepository: PreferencesRepository) {
    operator fun invoke(): BgTargets = BgTargets(
        low  = preferencesRepository.getBgTargetLow(),
        high = preferencesRepository.getBgTargetHigh()
    )
}

data class BgTargets(val low: Double, val high: Double)
