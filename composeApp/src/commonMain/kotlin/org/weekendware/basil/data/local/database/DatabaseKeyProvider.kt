package org.weekendware.basil.data.local.database

/**
 * Retrieves or generates the encryption key used to open the SQLCipher database.
 *
 * Each platform stores the key in a secure, OS-managed store:
 * - Android — Android Keystore via [EncryptedSharedPreferences]
 * - iOS     — Keychain via CoreFoundation Security APIs
 * - Desktop — no-op (in-memory database; encryption not applied)
 */
expect class DatabaseKeyProvider {
    fun getOrCreateKey(): String
}
