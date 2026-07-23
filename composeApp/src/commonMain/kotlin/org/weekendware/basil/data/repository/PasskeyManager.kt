package org.weekendware.basil.data.repository

/**
 * Executes FIDO2 passkey registration and authentication ceremonies using
 * each platform's native credential store. Given a WebAuthn challenge from
 * Supabase, produces the attestation (registration) or assertion
 * (authentication) needed to complete the round trip.
 *
 * Fetching the challenge and posting the result back to Supabase is
 * [AuthRepository]'s job, not this class's — [PasskeyManager] only executes
 * the platform credential ceremony in between. The credential's private key
 * never leaves the platform secure store (iCloud Keychain / Google Password
 * Manager); this class only ever handles the public attestation/assertion.
 *
 * **Not implemented here.** QA scaffolding only — see [PasskeyManagerTest]
 * for the Desktop no-op contract, the only platform behavior specified
 * precisely enough to verify without a real device, simulator, or platform
 * credential UI.
 */
expect class PasskeyManager {

    /**
     * Creates a new passkey credential from [challengeJson] (a FIDO2
     * `PublicKeyCredentialCreationOptions` challenge) and returns the
     * resulting attestation as JSON.
     */
    suspend fun createCredential(challengeJson: String): Result<String>

    /**
     * Signs [challengeJson] (a FIDO2 `PublicKeyCredentialRequestOptions`
     * challenge) with the platform-stored private key and returns the
     * resulting assertion as JSON.
     */
    suspend fun getCredential(challengeJson: String): Result<String>
}
