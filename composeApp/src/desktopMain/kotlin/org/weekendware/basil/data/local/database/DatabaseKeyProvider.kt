package org.weekendware.basil.data.local.database

// Desktop uses an in-memory JdbcSqliteDriver; encryption is not applied.
actual class DatabaseKeyProvider {
    actual fun getOrCreateKey(): String = "desktop-dev-only"
}
