package org.weekendware.basil.domain.usecase

import org.weekendware.basil.data.repository.PreferencesRepository
import org.weekendware.basil.domain.model.BgUnit

/**
 * Persists the user's preferred [BgUnit].
 *
 * @param preferencesRepository Source of truth for user preferences.
 */
class SetBgUnitPreferenceUseCase(private val preferencesRepository: PreferencesRepository) {
    operator fun invoke(unit: BgUnit) = preferencesRepository.setBgUnit(unit)
}
