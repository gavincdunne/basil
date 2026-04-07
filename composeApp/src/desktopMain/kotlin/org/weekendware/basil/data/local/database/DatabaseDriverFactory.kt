package org.weekendware.basil.data.local.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import org.weekendware.basil.database.BasilDatabase

/**
 * Desktop (JVM) implementation of [DatabaseDriverFactory].
 *
 * Creates a [JdbcSqliteDriver] using an **in-memory** SQLite database.
 * This means all data is lost when the application is closed.
 *
 * **TODO before desktop release:** Switch to a file-backed driver:
 * ```kotlin
 * JdbcSqliteDriver("jdbc:sqlite:basil.db")
 * ```
 * and call `BasilDatabase.Schema.migrate(driver, oldVersion, newVersion)`
 * to handle schema migrations on upgrade.
 */
actual class DatabaseDriverFactory {

    /**
     * Creates and returns an in-memory [JdbcSqliteDriver] for [BasilDatabase].
     * The schema is created immediately after the driver is instantiated.
     */
    actual fun createDriver(): SqlDriver {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        BasilDatabase.Schema.create(driver)
        return driver
    }
}
