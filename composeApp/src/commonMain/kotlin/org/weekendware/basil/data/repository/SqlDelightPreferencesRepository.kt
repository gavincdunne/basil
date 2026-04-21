package org.weekendware.basil.data.repository

import org.weekendware.basil.database.BasilDatabase
import org.weekendware.basil.domain.model.BgUnit

/**
 * SQLDelight-backed implementation of [PreferencesRepository].
 *
 * Backed by the `Preferences` SQLite table. Each preference is stored as a
 * `TEXT` value under a unique string key.
 *
 * @param database The [BasilDatabase] instance injected via Koin.
 */
class SqlDelightPreferencesRepository(private val database: BasilDatabase) : PreferencesRepository {

    companion object {
        /** Key for the user's preferred blood glucose unit. */
        private const val BG_UNIT_KEY = "bg_unit"
    }

    override fun getBgUnit(): BgUnit {
        val value = database.preferencesQueries.get(BG_UNIT_KEY).executeAsOneOrNull()
        return value?.let { runCatching { BgUnit.valueOf(it) }.getOrNull() } ?: BgUnit.MGDL
    }

    override fun setBgUnit(unit: BgUnit) {
        database.preferencesQueries.set(BG_UNIT_KEY, unit.name)
    }
}
