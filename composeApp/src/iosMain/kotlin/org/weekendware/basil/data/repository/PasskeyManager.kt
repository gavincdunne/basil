package org.weekendware.basil.data.repository

/**
 * iOS FIDO2 ceremony execution via
 * `ASAuthorizationPlatformPublicKeyCredentialProvider` (AuthenticationServices,
 * iOS 16+).
 *
 * Registration: `ASAuthorizationPlatformPublicKeyCredentialRegistrationRequest`.
 * Authentication: `ASAuthorizationPlatformPublicKeyCredentialAssertionRequest`.
 * `AuthenticationServices` isn't wrapped by any KMP library at the
 * supabase-kt version this project uses — the real implementation needs a
 * `kotlinx-cinterop` binding and `@ObjCAction` delegate callbacks wrapped in
 * `suspendCoroutine`. Highest-risk implementation task in the passkey
 * feature; allocate extra time.
 */
actual class PasskeyManager {

    actual suspend fun createCredential(challengeJson: String): Result<String> {
        TODO("Not yet implemented — see tdd-splash-auth-07222026.md, Passkeys on KMP (iOS)")
    }

    actual suspend fun getCredential(challengeJson: String): Result<String> {
        TODO("Not yet implemented — see tdd-splash-auth-07222026.md, Passkeys on KMP (iOS)")
    }
}
