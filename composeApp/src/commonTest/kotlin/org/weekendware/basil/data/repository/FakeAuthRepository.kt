package org.weekendware.basil.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Test double for [AuthRepository]. Owned by QA — configure the `*Result`
 * properties to drive success/failure paths in ViewModel tests.
 *
 * The social sign-in, password reset, email verification, passkey, and
 * deep-link methods are fully implemented here even though the real
 * [SupabaseAuthRepository] only stubs them — this fake is QA's test
 * tooling and its behavior is asserted directly in
 * [FakeAuthRepositoryTest], which also documents the contract Backend
 * Builder's real implementation must satisfy.
 */
class FakeAuthRepository : AuthRepository {

    private val _sessionFlow = MutableStateFlow(false)
    override val sessionFlow: Flow<Boolean> = _sessionFlow

    var signInResult: Result<Unit> = Result.success(Unit)
    var signUpResult: Result<Unit> = Result.success(Unit)
    var signOutResult: Result<Unit> = Result.success(Unit)
    var resetPasswordResult: Result<Unit> = Result.success(Unit)
    var updatePasswordResult: Result<Unit> = Result.success(Unit)
    var resendVerificationResult: Result<Unit> = Result.success(Unit)
    var registerPasskeyResult: Result<Unit> = Result.success(Unit)
    var signInWithPasskeyResult: Result<Unit> = Result.success(Unit)
    var handleDeepLinkResult: Result<Unit> = Result.success(Unit)

    var emailVerified: Boolean = true
    var daysSinceSignupValue: Long = 0
    var passkeyEnrolled: Boolean = false
    var storedLastUsedEmail: String? = null

    var resetPasswordCallCount: Int = 0
        private set
    var resendVerificationCallCount: Int = 0
        private set
    var lastResetPasswordEmail: String? = null
        private set
    var lastHandledDeepLink: String? = null
        private set
    var updatePasswordCallCount: Int = 0
        private set
    var lastUpdatedPassword: String? = null
        private set

    fun setSignedIn(value: Boolean) {
        _sessionFlow.value = value
    }

    override suspend fun signIn(email: String, password: String): Result<Unit> {
        if (signInResult.isSuccess) {
            _sessionFlow.value = true
            storedLastUsedEmail = email
        }
        return signInResult
    }

    override suspend fun signUp(email: String, password: String): Result<Unit> {
        if (signUpResult.isSuccess) {
            _sessionFlow.value = true
            storedLastUsedEmail = email
        }
        return signUpResult
    }

    override suspend fun signOut(): Result<Unit> {
        if (signOutResult.isSuccess) _sessionFlow.value = false
        return signOutResult
    }

    override fun currentUserId(): String? = if (_sessionFlow.value) "fake-uid" else null
    override fun currentUserEmail(): String? = if (_sessionFlow.value) "fake@example.com" else null
    override fun isSignedIn(): Boolean = _sessionFlow.value

    override fun recordLastUsedEmail(email: String) {
        storedLastUsedEmail = email
    }

    override suspend fun resetPassword(email: String): Result<Unit> {
        resetPasswordCallCount++
        lastResetPasswordEmail = email
        return resetPasswordResult
    }

    override suspend fun resendVerificationEmail(): Result<Unit> {
        resendVerificationCallCount++
        return resendVerificationResult
    }

    override fun isEmailVerified(): Boolean = emailVerified

    override fun daysSinceSignup(): Long = daysSinceSignupValue

    override suspend fun registerPasskey(): Result<Unit> {
        if (registerPasskeyResult.isSuccess) passkeyEnrolled = true
        return registerPasskeyResult
    }

    override suspend fun signInWithPasskey(): Result<Unit> {
        if (signInWithPasskeyResult.isSuccess) _sessionFlow.value = true
        return signInWithPasskeyResult
    }

    override fun hasPasskeyEnrolled(): Boolean = passkeyEnrolled

    override suspend fun handleDeepLink(url: String): Result<Unit> {
        lastHandledDeepLink = url
        return handleDeepLinkResult
    }

    override fun lastUsedEmail(): String? = storedLastUsedEmail

    override suspend fun updatePassword(newPassword: String): Result<Unit> {
        updatePasswordCallCount++
        lastUpdatedPassword = newPassword
        return updatePasswordResult
    }
}
