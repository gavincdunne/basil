package org.weekendware.basil.data.local.database

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import org.weekendware.basil.database.BasilDatabase

/**
 * Android implementation of [DatabaseDriverFactory].
 *
 * Opens `basil.db` via SQLCipher using [AndroidSqliteDriver] with a
 * [SupportOpenHelperFactory]. The encryption passphrase is managed by
 * [DatabaseKeyProvider] (Android Keystore via [EncryptedSharedPreferences]).
 *
 * @param context     The application [Context], used to locate the database file.
 * @param keyProvider Supplies the stable per-device encryption passphrase.
 */
actual class DatabaseDriverFactory(
    private val context: Context,
    private val keyProvider: DatabaseKeyProvider,
) {

    /** Creates and returns the encrypted [AndroidSqliteDriver] for [BasilDatabase]. */
    actual fun createDriver(): SqlDriver {
        val passphrase = keyProvider.getOrCreateKey().toByteArray(Charsets.UTF_8)
        return AndroidSqliteDriver(
            schema  = BasilDatabase.Schema,
            context = context,
            name    = "basil.db",
            factory = SupportOpenHelperFactory(passphrase),
        )
    }
}
