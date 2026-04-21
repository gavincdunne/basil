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
}
