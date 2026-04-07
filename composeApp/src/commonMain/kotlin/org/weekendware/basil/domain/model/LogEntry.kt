package org.weekendware.basil.domain.model

/**
 * A single recorded log entry in the user's health journal.
 *
 * Each field is nullable because an entry can record any combination of
 * readings — for example, a standalone blood glucose check with no insulin
 * or carb data, or a meal log with carbs only.
 *
 * @property id           Auto-generated database primary key.
 * @property timestamp    Unix epoch time in milliseconds when the entry was recorded.
 * @property bgValue      Blood glucose reading. Null if not recorded for this entry.
 * @property bgUnit       Unit for [bgValue]. Always non-null when [bgValue] is non-null.
 * @property insulinUnits Insulin dose in units. Null if not recorded for this entry.
 * @property carbsGrams   Carbohydrate intake in grams. Null if not recorded for this entry.
 */
data class LogEntry(
    val id:           Long,
    val timestamp:    Long,
    val bgValue:      Double?,
    val bgUnit:       BgUnit?,
    val insulinUnits: Double?,
    val carbsGrams:   Double?
)
