package org.weekendware.basil.presentation.session

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.weekendware.basil.data.repository.FakeAuthRepository
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class SessionViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private lateinit var repo: FakeAuthRepository
    private lateinit var viewModel: SessionViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(dispatcher)
        repo = FakeAuthRepository()
        viewModel = SessionViewModel(repo)
    }

    @AfterTest
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Unauthenticated when no session exists`() = runTest {
        assertEquals(SessionState.Unauthenticated, viewModel.state.value)
    }

    @Test
    fun `state becomes Authenticated when session flow emits true`() = runTest {
        repo.setSignedIn(true)
        assertEquals(SessionState.Authenticated, viewModel.state.value)
    }

    @Test
    fun `state returns to Unauthenticated when session is cleared`() = runTest {
        repo.setSignedIn(true)
        repo.setSignedIn(false)
        assertEquals(SessionState.Unauthenticated, viewModel.state.value)
    }

    @Test
    fun `state reflects sign-out after sign-in`() = runTest {
        repo.setSignedIn(true)
        assertEquals(SessionState.Authenticated, viewModel.state.value)
        repo.signOut()
        assertEquals(SessionState.Unauthenticated, viewModel.state.value)
    }
}
