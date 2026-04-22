package org.weekendware.basil.domain.usecase

import kotlinx.coroutines.flow.Flow
import org.weekendware.basil.data.repository.LogRepository
import org.weekendware.basil.domain.model.LogEntry

/**
 * Returns a [Flow] of recent log entries from [LogRepository].
 *
 * Acts as the single entry point for all screens that need to observe
 * the log. The limit is fixed at 100 entries — enough to cover any
 * single-day view plus a meaningful history window.
 */
class ObserveRecentLogsUseCase(private val logRepository: LogRepository) {
    operator fun invoke(): Flow<List<LogEntry>> = logRepository.getRecent(100)
}
