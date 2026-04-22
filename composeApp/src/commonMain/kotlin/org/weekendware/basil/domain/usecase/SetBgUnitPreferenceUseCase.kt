package org.weekendware.basil.domain.usecase

import org.weekendware.basil.data.repository.PreferencesRepository
import org.weekendware.basil.domain.model.BgUnit

/**
 * Persists the user's preferred [BgUnit].
 *
 * Returns [Result.success] on completion or [Result.failure] if the
 * database operation throws.
 *
 * @param preferencesRepository Source of truth for user preferences.
 */
class SetBgUnitPreferenceUseCase(private val preferencesRepository: PreferencesRepository) {
    operator fun invoke(unit: BgUnit): Result<Unit> = runCatching {
        preferencesRepository.setBgUnit(unit)
    }
}
