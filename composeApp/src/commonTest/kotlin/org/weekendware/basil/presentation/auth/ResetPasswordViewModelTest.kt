package org.weekendware.basil.presentation.auth

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import basil.composeapp.generated.resources.Res
import basil.composeapp.generated.resources.error_auth_failed
import org.weekendware.basil.data.repository.FakeAuthRepository
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ResetPasswordViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private lateinit var repo: FakeAuthRepository
    private lateinit var viewModel: ResetPasswordViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(dispatcher)
        repo = FakeAuthRepository()
        viewModel = ResetPasswordViewModel(repo)
    }

    @AfterTest
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is not sent and nothing entered`() {
        val state = viewModel.state.value
        assertEquals("", state.email)
        assertFalse(state.isSent)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `canSend is false when email is blank`() {
        assertFalse(viewModel.state.value.canSend)
    }

    @Test
    fun `sendResetLink does nothing when the email is blank`() = runTest {
        viewModel.sendResetLink()
        assertEquals(0, repo.resetPasswordCallCount)
    }

    @Test
    fun `successful send sets isSent and records the email`() = runTest {
        viewModel.onEmailChange("user@test.com")
        viewModel.sendResetLink()

        val state = viewModel.state.value
        assertTrue(state.isSent)
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertEquals("user@test.com", repo.lastResetPasswordEmail)
    }

    @Test
    fun `failed send surfaces an error and does not set isSent`() = runTest {
        repo.resetPasswordResult = Result.failure(Exception("rate limited"))
        viewModel.onEmailChange("user@test.com")
        viewModel.sendResetLink()

        val state = viewModel.state.value
        assertFalse(state.isSent)
        assertEquals(Res.string.error_auth_failed, state.error)
        assertFalse(state.isLoading)
    }

    @Test
    fun `resend calls sendResetLink again and increments the call count`() = runTest {
        viewModel.onEmailChange("user@test.com")
        viewModel.sendResetLink()
        assertEquals(1, repo.resetPasswordCallCount)

        viewModel.sendResetLink() // "Resend email" reuses the same action
        assertEquals(2, repo.resetPasswordCallCount)
    }

    @Test
    fun `onEmailChange clears an existing error`() = runTest {
        repo.resetPasswordResult = Result.failure(Exception("error"))
        viewModel.onEmailChange("user@test.com")
        viewModel.sendResetLink()
        assertEquals(Res.string.error_auth_failed, viewModel.state.value.error)

        viewModel.onEmailChange("new@test.com")
        assertNull(viewModel.state.value.error)
    }
}
