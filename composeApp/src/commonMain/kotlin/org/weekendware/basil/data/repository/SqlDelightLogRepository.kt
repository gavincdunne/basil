package org.weekendware.basil.data.repository

import org.weekendware.basil.database.BasilDatabase
import org.weekendware.basil.domain.model.BgUnit
import org.weekendware.basil.domain.model.LogEntry
import orgweekendwarebasil.database.LogEntry as LogEntryEntity

/**
 * SQLDelight-backed implementation of [LogRepository].
 *
 * Wraps the generated [BasilDatabase.logEntryQueries] and maps between the
 * database entity and the domain [LogEntry] model.
 *
 * @param database The [BasilDatabase] instance injected via Koin.
 */
class SqlDelightLogRepository(private val database: BasilDatabase) : LogRepository {

    override fun getRecent(limit: Long): List<LogEntry> =
        database.logEntryQueries.selectRecent(limit).executeAsList().map { it.toDomain() }

    override fun insert(
        bgValue: Double?,
        bgUnit: BgUnit?,
        insulinUnits: Double?,
        carbsGrams: Double?
    ) {
        database.logEntryQueries.insertEntry(
            timestamp = System.currentTimeMillis(),
            bg_value = bgValue,
            bg_unit = bgUnit?.name,
            insulin_units = insulinUnits,
            carbs_grams = carbsGrams
        )
    }

    override fun delete(id: Long) = database.logEntryQueries.deleteEntry(id)

    // ── Mapping ───────────────────────────────────────────────

    private fun LogEntryEntity.toDomain() = LogEntry(
        id = id,
        bgValue = bg_value,
        bgUnit = bg_unit?.let { runCatching { BgUnit.valueOf(it) }.getOrNull() },
        insulinUnits = insulin_units,
        carbsGrams = carbs_grams,
        timestamp = timestamp
    )
}
