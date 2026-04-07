package org.weekendware.basil.data.local.database

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import org.weekendware.basil.database.BasilDatabase

/**
 * Android implementation of [DatabaseDriverFactory].
 *
 * Creates an [AndroidSqliteDriver] backed by a named SQLite file (`basil.db`)
 * in the app's private data directory. Data persists across app sessions and
 * is managed by the Android SQLite framework, including automatic schema
 * migrations when the database version is incremented.
 *
 * @param context The application [Context], used to locate the database file.
 */
actual class DatabaseDriverFactory(private val context: Context) {

    /** Creates and returns the [AndroidSqliteDriver] for [BasilDatabase]. */
    actual fun createDriver(): SqlDriver =
        AndroidSqliteDriver(BasilDatabase.Schema, context, "basil.db")
}
