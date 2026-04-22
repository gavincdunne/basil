package org.weekendware.basil.data.repository

import kotlinx.coroutines.flow.Flow
import org.weekendware.basil.domain.model.BgUnit
import org.weekendware.basil.domain.model.LogEntry

/**
 * Contract for [LogEntry] persistence.
 *
 * Abstracts the storage mechanism so ViewModels and use cases depend only
 * on this interface, not on any specific database implementation.
 */
interface LogRepository {

    /**
     * Returns a [Flow] of the most recent log entries, ordered by timestamp
     * descending. Re-emits automatically whenever the underlying table changes,
     * so observers always see up-to-date data without polling.
     *
     * @param limit Maximum number of entries to return. Defaults to 20.
     */
    fun getRecent(limit: Long = 20): Flow<List<LogEntry>>

    /**
     * Inserts a new log entry with the current timestamp.
     *
     * @param bgValue      Blood glucose reading, or null if not recorded.
     * @param bgUnit       Unit for [bgValue]. Should be non-null when [bgValue] is non-null.
     * @param insulinUnits Insulin dose in units, or null if not recorded.
     * @param carbsGrams   Carbohydrate intake in grams, or null if not recorded.
     */
    fun insert(
        bgValue:      Double?,
        bgUnit:       BgUnit?,
        insulinUnits: Double?,
        carbsGrams:   Double?
    )

    /**
     * Deletes a single log entry by its database ID.
     *
     * @param id The primary key of the entry to delete.
     */
    fun delete(id: Long)
}
