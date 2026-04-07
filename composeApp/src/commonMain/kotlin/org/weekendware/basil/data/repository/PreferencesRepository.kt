package org.weekendware.basil.data.repository

import org.weekendware.basil.database.BasilDatabase
import org.weekendware.basil.domain.model.BgUnit

class PreferencesRepository(private val database: BasilDatabase) {

    companion object {
        private const val BG_UNIT_KEY = "bg_unit"
    }

    fun getBgUnit(): BgUnit {
        val value = database.preferencesQueries.get(BG_UNIT_KEY).executeAsOneOrNull()
        return value?.let { runCatching { BgUnit.valueOf(it) }.getOrNull() } ?: BgUnit.MGDL
    }

    fun setBgUnit(unit: BgUnit) {
        database.preferencesQueries.set(BG_UNIT_KEY, unit.name)
    }
}
