package org.weekendware.basil.domain.usecase

import org.weekendware.basil.data.repository.LogRepository

/**
 * Deletes a single log entry by its database ID.
 *
 * Returns [Result.success] on completion or [Result.failure] if the
 * database operation throws.
 *
 * @param logRepository Source of truth for all log entry data.
 */
class DeleteLogEntryUseCase(private val logRepository: LogRepository) {
    operator fun invoke(id: Long): Result<Unit> = runCatching {
        logRepository.delete(id)
    }
}
