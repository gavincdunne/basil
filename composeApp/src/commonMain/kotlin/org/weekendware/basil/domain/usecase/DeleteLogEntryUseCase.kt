package org.weekendware.basil.domain.usecase

import org.weekendware.basil.data.repository.LogRepository

/**
 * Deletes a single log entry by its database ID.
 *
 * @param logRepository Source of truth for all log entry data.
 */
class DeleteLogEntryUseCase(private val logRepository: LogRepository) {
    operator fun invoke(id: Long) = logRepository.delete(id)
}
