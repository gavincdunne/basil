package org.weekendware.basil.data.local.database

import android.content.Context
import app.cash.sqldelight.android.AndroidSqliteDriver
import app.cash.sqldelight.db.SqlDriver
import org.weekendware.basil.database.BasilDatabase

actual class DatabaseDriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(BasilDatabase.Schema, context, "basil.db")
    }
}