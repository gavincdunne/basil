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
        private const val BG_UNIT_KEY        = "bg_unit"
        private const val BG_TARGET_LOW_KEY  = "bg_target_low"
        private const val BG_TARGET_HIGH_KEY = "bg_target_high"

        private const val DEFAULT_TARGET_LOW  = 3.9
        private const val DEFAULT_TARGET_HIGH = 10.0
    }

    override fun getBgUnit(): BgUnit {
        val value = database.preferencesQueries.get(BG_UNIT_KEY).executeAsOneOrNull()
        return value?.let { runCatching { BgUnit.valueOf(it) }.getOrNull() } ?: BgUnit.MGDL
    }

    override fun setBgUnit(unit: BgUnit) {
        database.preferencesQueries.set(BG_UNIT_KEY, unit.name)
    }

    override fun getBgTargetLow(): Double =
        database.preferencesQueries.get(BG_TARGET_LOW_KEY).executeAsOneOrNull()
            ?.toDoubleOrNull() ?: DEFAULT_TARGET_LOW

    override fun setBgTargetLow(value: Double) {
        database.preferencesQueries.set(BG_TARGET_LOW_KEY, value.toString())
    }

    override fun getBgTargetHigh(): Double =
        database.preferencesQueries.get(BG_TARGET_HIGH_KEY).executeAsOneOrNull()
            ?.toDoubleOrNull() ?: DEFAULT_TARGET_HIGH

    override fun setBgTargetHigh(value: Double) {
        database.preferencesQueries.set(BG_TARGET_HIGH_KEY, value.toString())
    }
}
