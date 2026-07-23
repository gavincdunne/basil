package org.weekendware.basil.data.repository

import android.content.Context

/**
 * Android FIDO2 ceremony execution via `androidx.credentials.CredentialManager`.
 *
 * Registration: `CredentialManager.createCredential(CreatePublicKeyCredentialRequest)`.
 * Authentication: `CredentialManager.getCredential(GetPublicKeyCredentialOption)`.
 * Requires `androidx.credentials:credentials` and
 * `androidx.credentials:credentials-play-services-auth`.
 */
actual class PasskeyManager(private val context: Context) {

    actual suspend fun createCredential(challengeJson: String): Result<String> {
        TODO("Not yet implemented — see tdd-splash-auth-07222026.md, Passkeys on KMP (Android)")
    }

    actual suspend fun getCredential(challengeJson: String): Result<String> {
        TODO("Not yet implemented — see tdd-splash-auth-07222026.md, Passkeys on KMP (Android)")
    }
}
