package org.weekendware.basil.data.repository

/**
 * Desktop has no platform credential store, so passkeys aren't supported
 * here — both operations always fail. The passkey enrolment prompt and
 * adaptive biometric auth screen are never shown on Desktop; email/password
 * and Google OAuth remain the only sign-in paths.
 */
actual class PasskeyManager {

    actual suspend fun createCredential(challengeJson: String): Result<String> {
        TODO("Not yet implemented — see tdd-splash-auth-07222026.md, Passkeys on KMP (Desktop no-op)")
    }

    actual suspend fun getCredential(challengeJson: String): Result<String> {
        TODO("Not yet implemented — see tdd-splash-auth-07222026.md, Passkeys on KMP (Desktop no-op)")
    }
}
