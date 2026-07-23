package org.weekendware.basil.presentation.auth

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.weekendware.basil.data.repository.FakeAuthRepository
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class VerificationWallViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private lateinit var repo: FakeAuthRepository

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(dispatcher)
        repo = FakeAuthRepository()
    }

    @AfterTest
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state shows the signed-in user's email and allows resend`() {
        repo.setSignedIn(true)
        val viewModel = VerificationWallViewModel(repo)
        val state = viewModel.state.value
        assertEquals("fake@example.com", state.email) // FakeAuthRepository's fixed signed-in email
        assertTrue(state.canResend)
        assertFalse(state.isLoading)
    }

    @Test
    fun `onResend calls the repository and immediately starts the cooldown`() = runTest {
        repo.setSignedIn(true)
        val viewModel = VerificationWallViewModel(repo)

        viewModel.onResend()

        assertEquals(1, repo.resendVerificationCallCount)
        assertFalse(viewModel.state.value.canResend)
    }

    @Test
    fun `cooldown clears after 60 seconds of virtual time`() = runTest {
        repo.setSignedIn(true)
        val viewModel = VerificationWallViewModel(repo)

        viewModel.onResend()
        assertFalse(viewModel.state.value.canResend)

        advanceTimeBy(60_001)

        assertTrue(viewModel.state.value.canResend)
    }

    @Test
    fun `onResend is a no-op while the cooldown is active`() = runTest {
        repo.setSignedIn(true)
        val viewModel = VerificationWallViewModel(repo)

        // Two rapid calls in the same virtual-time instant — the second should
        // see canResend already false from the first call's synchronous update.
        viewModel.onResend()
        val callsAfterFirst = repo.resendVerificationCallCount
        viewModel.onResend()

        assertEquals(callsAfterFirst, repo.resendVerificationCallCount)
    }

    @Test
    fun `onSignOut calls the repository`() = runTest {
        repo.setSignedIn(true)
        val viewModel = VerificationWallViewModel(repo)

        viewModel.onSignOut()

        assertFalse(repo.isSignedIn())
    }
}
