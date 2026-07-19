package org.weekendware.basil.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeAuthRepository : AuthRepository {

    private val _sessionFlow = MutableStateFlow(false)
    override val sessionFlow: Flow<Boolean> = _sessionFlow

    var signInResult: Result<Unit> = Result.success(Unit)
    var signUpResult: Result<Unit> = Result.success(Unit)

    fun setSignedIn(value: Boolean) {
        _sessionFlow.value = value
    }

    override suspend fun signIn(email: String, password: String): Result<Unit> {
        if (signInResult.isSuccess) _sessionFlow.value = true
        return signInResult
    }

    override suspend fun signUp(email: String, password: String): Result<Unit> {
        if (signUpResult.isSuccess) _sessionFlow.value = true
        return signUpResult
    }

    override suspend fun signOut(): Result<Unit> {
        _sessionFlow.value = false
        return Result.success(Unit)
    }

    override fun currentUserId(): String? = if (_sessionFlow.value) "fake-uid" else null
    override fun currentUserEmail(): String? = if (_sessionFlow.value) "fake@example.com" else null
    override fun isSignedIn(): Boolean = _sessionFlow.value
}
