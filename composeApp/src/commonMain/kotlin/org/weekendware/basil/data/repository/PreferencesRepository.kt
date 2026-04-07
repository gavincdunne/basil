package org.weekendware.basil.data.repository

import org.weekendware.basil.database.BasilDatabase
import org.weekendware.basil.domain.model.BgUnit

/**
 * Repository for lightweight key-value user preferences.
 *
 * Backed by the `Preferences` SQLite table. Each preference is stored as a
 * `TEXT` value under a unique string key. As new preferences are added to
 * the app, define their keys as constants in the companion object and add
 * typed accessor methods below.
 *
 * @param database The [BasilDatabase] instance injected via Koin.
 */
class PreferencesRepository(private val database: BasilDatabase) {

    companion object {
        /** Key for the user's preferred blood glucose unit. */
        private const val BG_UNIT_KEY = "bg_unit"
    }

    /**
     * Returns the user's preferred [BgUnit].
     *
     * Defaults to [BgUnit.MGDL] if no preference has been saved yet or if
     * the stored value cannot be parsed (e.g. after a schema change).
     */
    fun getBgUnit(): BgUnit {
        val value = database.preferencesQueries.get(BG_UNIT_KEY).executeAsOneOrNull()
        return value?.let { runCatching { BgUnit.valueOf(it) }.getOrNull() } ?: BgUnit.MGDL
    }

    /**
     * Persists the user's preferred [BgUnit].
     *
     * Uses `INSERT OR REPLACE` semantics so calling this multiple times with
     * different values always reflects the latest choice.
     *
     * @param unit The [BgUnit] to store.
     */
    fun setBgUnit(unit: BgUnit) {
        database.preferencesQueries.set(BG_UNIT_KEY, unit.name)
    }
}
