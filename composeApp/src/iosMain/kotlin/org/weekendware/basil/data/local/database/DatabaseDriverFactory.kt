package org.weekendware.basil.data.local.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import co.touchlab.sqliter.DatabaseConfiguration
import org.weekendware.basil.database.BasilDatabase

/**
 * iOS implementation of [DatabaseDriverFactory].
 *
 * Opens `basil.db` via SQLCipher by passing the encryption key through
 * sqliter's [DatabaseConfiguration.Encryption]. The key itself is managed
 * by [DatabaseKeyProvider] (iOS Keychain).
 *
 * **iOS Xcode requirement**: SQLCipher encryption is only active once the
 * SQLCipher SPM package is linked in Xcode and `OTHER_LDFLAGS` in
 * `Config.xcconfig` is changed from `-lsqlite3` to link SQLCipher instead.
 * Until that step is done the driver opens an unencrypted standard SQLite
 * database (the key is accepted but silently ignored by the system sqlite3).
 *
 * @param keyProvider Supplies the stable per-device encryption passphrase.
 */
actual class DatabaseDriverFactory(private val keyProvider: DatabaseKeyProvider) {

    /** Creates and returns the [NativeSqliteDriver] with SQLCipher encryption for [BasilDatabase]. */
    actual fun createDriver(): SqlDriver {
        val key = keyProvider.getOrCreateKey()
        return NativeSqliteDriver(
            schema  = BasilDatabase.Schema,
            name    = "basil.db",
            onConfiguration = { config ->
                config.copy(encryptionConfig = DatabaseConfiguration.Encryption(key = key))
            },
        )
    }
}
