package org.weekendware.basil.data.local.database

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.util.UUID

actual class DatabaseKeyProvider(private val context: Context) {

    actual fun getOrCreateKey(): String {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        val prefs = EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
        return prefs.getString(KEY_ALIAS, null) ?: UUID.randomUUID().toString().also { key ->
            prefs.edit().putString(KEY_ALIAS, key).apply()
        }
    }

    private companion object {
        const val PREFS_NAME = "basil_db_key"
        const val KEY_ALIAS  = "db_encryption_key"
    }
}
