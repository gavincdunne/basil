package org.weekendware.basil.domain.usecase

import org.weekendware.basil.data.repository.PreferencesRepository
import org.weekendware.basil.domain.model.BgUnit

/**
 * Returns the user's preferred [BgUnit].
 *
 * Defaults to [BgUnit.MGDL] if no preference has been saved yet.
 *
 * @param preferencesRepository Source of truth for user preferences.
 */
class GetBgUnitPreferenceUseCase(private val preferencesRepository: PreferencesRepository) {
    operator fun invoke(): BgUnit = preferencesRepository.getBgUnit()
}
