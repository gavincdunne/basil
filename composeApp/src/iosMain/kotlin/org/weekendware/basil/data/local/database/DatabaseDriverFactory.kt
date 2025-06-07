package org.weekendware.basil.data.local.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.native.driver.NativeSqliteDriver
import org.weekendware.basil.database.BasilDatabase

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        return NativeSqliteDriver(BasilDatabase.Schema, "basil.db")
    }
}