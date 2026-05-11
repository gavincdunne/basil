package org.weekendware.basil.data.repository

import org.weekendware.basil.domain.model.BgUnit

/**
 * Contract for user preference persistence.
 *
 * Abstracts the storage mechanism so ViewModels and use cases depend only
 * on this interface, not on any specific database implementation.
 */
interface PreferencesRepository {

    /**
     * Returns the user's preferred [BgUnit].
     *
     * Defaults to [BgUnit.MGDL] if no preference has been saved yet.
     */
    fun getBgUnit(): BgUnit

    /**
     * Persists the user's preferred [BgUnit].
     *
     * @param unit The [BgUnit] to store.
     */
    fun setBgUnit(unit: BgUnit)

    /**
     * Returns the user's low end of their target BG range, in their preferred unit.
     * Defaults to 3.9 mmol/L equivalent; callers should interpret in context of the current unit.
     */
    fun getBgTargetLow(): Double

    /**
     * Persists the user's low BG target.
     */
    fun setBgTargetLow(value: Double)

    /**
     * Returns the user's high end of their target BG range.
     * Defaults to 10.0 mmol/L equivalent.
     */
    fun getBgTargetHigh(): Double

    /**
     * Persists the user's high BG target.
     */
    fun setBgTargetHigh(value: Double)
}
