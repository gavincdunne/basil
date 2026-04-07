package org.weekendware.basil.data.repository

import kotlinx.datetime.Clock
import org.weekendware.basil.database.BasilDatabase
import org.weekendware.basil.domain.model.BgUnit
import org.weekendware.basil.domain.model.LogEntry
import orgweekendwarebasil.database.LogEntry as LogEntryEntity

/**
 * Repository for [LogEntry] persistence.
 *
 * Provides the single source of truth for all log entry data, mapping
 * between the SQLDelight-generated [LogEntryEntity] and the domain [LogEntry]
 * model. All database interactions for log entries should go through this class.
 *
 * @param database The [BasilDatabase] instance injected via Koin.
 */
class LogRepository(private val database: BasilDatabase) {

    /**
     * Returns the most recent log entries, ordered by timestamp descending.
     *
     * @param limit Maximum number of entries to return. Defaults to 20.
     * @return A list of [LogEntry] domain models, newest first.
     */
    fun getRecent(limit: Long = 20): List<LogEntry> =
        database.logEntryQueries.selectRecent(limit).executeAsList().map { it.toDomain() }

    /**
     * Inserts a new log entry with the current timestamp.
     *
     * At least one of the value parameters should be non-null for the entry
     * to be meaningful, but the repository does not enforce this — validation
     * is the responsibility of the calling ViewModel.
     *
     * @param bgValue      Blood glucose reading, or null if not recorded.
     * @param bgUnit       Unit for [bgValue]. Should be non-null whenever [bgValue] is non-null.
     * @param insulinUnits Insulin dose in units, or null if not recorded.
     * @param carbsGrams   Carbohydrate intake in grams, or null if not recorded.
     */
    fun insert(
        bgValue:      Double?,
        bgUnit:       BgUnit?,
        insulinUnits: Double?,
        carbsGrams:   Double?
    ) {
        database.logEntryQueries.insertEntry(
            timestamp     = Clock.System.now().toEpochMilliseconds(),
            bg_value      = bgValue,
            bg_unit       = bgUnit?.name,
            insulin_units = insulinUnits,
            carbs_grams   = carbsGrams
        )
    }

    /**
     * Deletes a single log entry by its database ID.
     *
     * @param id The primary key of the entry to delete.
     */
    fun delete(id: Long) = database.logEntryQueries.deleteEntry(id)

    // ── Mapping ───────────────────────────────────────────────

    private fun LogEntryEntity.toDomain() = LogEntry(
        id           = id,
        timestamp    = timestamp,
        bgValue      = bg_value,
        bgUnit       = bg_unit?.let { runCatching { BgUnit.valueOf(it) }.getOrNull() },
        insulinUnits = insulin_units,
        carbsGrams   = carbs_grams
    )
}
