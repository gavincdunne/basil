package org.weekendware.basil.domain.usecase

import org.weekendware.basil.domain.model.LogEntry

/**
 * Returns the most recent [LogEntry] that contains a blood glucose value,
 * or null if no such entry exists.
 *
 * Entries are assumed to be ordered newest-first (as returned by
 * [ObserveRecentLogsUseCase]). The first entry with a non-null [LogEntry.bgValue]
 * is returned, regardless of whether it was recorded today.
 */
class GetLastBgReadingUseCase {
    operator fun invoke(entries: List<LogEntry>): LogEntry? =
        entries.firstOrNull { it.bgValue != null }
}
