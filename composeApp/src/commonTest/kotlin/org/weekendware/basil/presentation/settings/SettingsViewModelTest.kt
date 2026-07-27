package org.weekendware.basil.presentation.settings

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.weekendware.basil.data.repository.FakeAuthRepository
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * QA test suite for [SettingsViewModel], written ahead of implementation
 * per the team's test-first process. Spec: `spec-splash-auth-07222026.md`,
 * AC24.
 *
 * All cases touching [SettingsViewModel.onSignOut] are expected to fail
 * with `NotImplementedError` until Backend Builder implements it.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private lateinit var repo: FakeAuthRepository
    private lateinit var viewModel: SettingsViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(dispatcher)
        repo = FakeAuthRepository().apply { setSignedIn(true) }
        viewModel = SettingsViewModel(repo)
    }

    @AfterTest
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is not signing out`() {
        assertFalse(viewModel.state.value.isSigningOut)
    }

    @Test
    fun `onSignOut calls the repository and signs the user out`() = runTest {
        viewModel.onSignOut()
        advanceUntilIdle()

        assertFalse(repo.isSignedIn())
    }

    @Test
    fun `onSignOut clears isSigningOut after a successful call`() = runTest {
        viewModel.onSignOut()
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isSigningOut)
    }

    @Test
    fun `onSignOut clears isSigningOut even when the repository call fails`() = runTest {
        repo.signOutResult = Result.failure(Exception("network error"))

        viewModel.onSignOut()
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isSigningOut)
        assertTrue(repo.isSignedIn())
    }
}
