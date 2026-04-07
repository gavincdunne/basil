package org.weekendware.basil.data.local.database

import app.cash.sqldelight.db.SqlDriver

/**
 * Platform-specific factory for creating the SQLDelight [SqlDriver].
 *
 * Each platform provides its own `actual` implementation:
 * - **Android** — uses `AndroidSqliteDriver` backed by a named file (`basil.db`).
 * - **iOS** — uses `NativeSqliteDriver` backed by a named file (`basil.db`).
 * - **Desktop** — uses `JdbcSqliteDriver` with an in-memory database
 *   (data does not persist across desktop sessions; switch to a file-backed
 *   driver before shipping a desktop release).
 *
 * The factory is registered in Koin's `platformModule` and consumed by
 * [DatabaseProvider] to obtain the shared [BasilDatabase] instance.
 */
expect class DatabaseDriverFactory {
    /**
     * Creates and returns the platform-appropriate [SqlDriver].
     *
     * Called once during app startup via [DatabaseProvider.getDatabase].
     */
    fun createDriver(): SqlDriver
}
