package org.weekendware.basil.data.local.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import org.weekendware.basil.database.BasilDatabase

/**
 * iOS implementation of [DatabaseDriverFactory].
 *
 * Creates a [NativeSqliteDriver] backed by a named SQLite file (`basil.db`)
 * stored in the app's Documents directory. Data persists across app sessions.
 */
actual class DatabaseDriverFactory {

    /** Creates and returns the [NativeSqliteDriver] for [BasilDatabase]. */
    actual fun createDriver(): SqlDriver =
        NativeSqliteDriver(BasilDatabase.Schema, "basil.db")
}
