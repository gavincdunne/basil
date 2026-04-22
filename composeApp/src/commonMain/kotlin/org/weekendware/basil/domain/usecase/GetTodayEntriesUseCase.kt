package org.weekendware.basil.domain.usecase

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.weekendware.basil.domain.model.LogEntry

/**
 * Filters a list of log entries to those recorded today in the device's
 * local timezone.
 *
 * Extracted from [DashboardViewModel] so the definition of "today" lives
 * in the domain layer and can be tested independently of any UI class.
 */
class GetTodayEntriesUseCase {

    operator fun invoke(entries: List<LogEntry>): List<LogEntry> {
        val tz = TimeZone.currentSystemDefault()
        val today = Clock.System.now().toLocalDateTime(tz).date
        return entries.filter { entry ->
            Instant.fromEpochMilliseconds(entry.timestamp).toLocalDateTime(tz).date == today
        }
    }
}
