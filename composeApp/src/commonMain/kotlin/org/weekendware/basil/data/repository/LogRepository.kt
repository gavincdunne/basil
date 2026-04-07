package org.weekendware.basil.data.repository

import kotlinx.datetime.Clock
import org.weekendware.basil.database.BasilDatabase
import org.weekendware.basil.domain.model.BgUnit
import org.weekendware.basil.domain.model.LogEntry
import orgweekendwarebasil.database.LogEntry as LogEntryEntity

class LogRepository(private val database: BasilDatabase) {

    fun getRecent(limit: Long = 20): List<LogEntry> =
        database.logEntryQueries.selectRecent(limit).executeAsList().map { it.toDomain() }

    fun insert(
        bgValue: Double?,
        bgUnit: BgUnit?,
        insulinUnits: Double?,
        carbsGrams: Double?
    ) {
        database.logEntryQueries.insertEntry(
            timestamp = Clock.System.now().toEpochMilliseconds(),
            bg_value = bgValue,
            bg_unit = bgUnit?.name,
            insulin_units = insulinUnits,
            carbs_grams = carbsGrams
        )
    }

    fun delete(id: Long) = database.logEntryQueries.deleteEntry(id)

    private fun LogEntryEntity.toDomain() = LogEntry(
        id = id,
        timestamp = timestamp,
        bgValue = bg_value,
        bgUnit = bg_unit?.let { runCatching { BgUnit.valueOf(it) }.getOrNull() },
        insulinUnits = insulin_units,
        carbsGrams = carbs_grams
    )
}
