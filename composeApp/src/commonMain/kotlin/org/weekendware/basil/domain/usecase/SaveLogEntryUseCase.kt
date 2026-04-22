package org.weekendware.basil.domain.usecase

import org.weekendware.basil.data.repository.LogRepository
import org.weekendware.basil.domain.model.BgUnit

/**
 * Validates and saves a log entry.
 *
 * Encapsulates the mapping from raw form strings to typed domain values
 * and the rule that a BG unit is only written when a BG value is present.
 * Returns [Result.success] with `true` if the entry was saved, or
 * [Result.success] with `false` if the form was empty (no-op). A
 * [Result.failure] is returned if an unexpected error occurs during save.
 *
 * @param logRepository Persists the entry to the local database.
 */
class SaveLogEntryUseCase(private val logRepository: LogRepository) {

    operator fun invoke(
        bgValue: String,
        bgUnit: BgUnit,
        insulinUnits: String,
        carbsGrams: String
    ): Result<Boolean> {
        val bg = bgValue.toDoubleOrNull()
        val insulin = insulinUnits.toDoubleOrNull()
        val carbs = carbsGrams.toDoubleOrNull()

        if (bg == null && insulin == null && carbs == null) return Result.success(false)

        return runCatching {
            logRepository.insert(
                bgValue = bg,
                bgUnit = if (bg != null) bgUnit else null,
                insulinUnits = insulin,
                carbsGrams = carbs
            )
            true
        }
    }
}
