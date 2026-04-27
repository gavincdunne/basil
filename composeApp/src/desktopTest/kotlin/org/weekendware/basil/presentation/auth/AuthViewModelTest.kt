package org.weekendware.basil.presentation.auth

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
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private lateinit var repo: FakeAuthRepository
    private lateinit var viewModel: AuthViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(dispatcher)
        repo = FakeAuthRepository()
        viewModel = AuthViewModel(repo)
    }

    @AfterTest
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is empty sign-in form`() {
        val state = viewModel.state.value
        assertEquals("", state.email)
        assertEquals("", state.password)
        assertFalse(state.isSignUp)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `canSubmit is false when email is blank`() {
        viewModel.onPasswordChange("password123")
        assertFalse(viewModel.state.value.canSubmit)
    }

    @Test
    fun `canSubmit is false when password is shorter than 6 characters`() {
        viewModel.onEmailChange("user@test.com")
        viewModel.onPasswordChange("abc")
        assertFalse(viewModel.state.value.canSubmit)
    }

    @Test
    fun `canSubmit is true when email and password meet requirements`() {
        viewModel.onEmailChange("user@test.com")
        viewModel.onPasswordChange("password123")
        assertTrue(viewModel.state.value.canSubmit)
    }

    @Test
    fun `toggleMode switches between sign-in and sign-up`() {
        assertFalse(viewModel.state.value.isSignUp)
        viewModel.toggleMode()
        assertTrue(viewModel.state.value.isSignUp)
        viewModel.toggleMode()
        assertFalse(viewModel.state.value.isSignUp)
    }

    @Test
    fun `successful sign-in clears loading and error`() = runTest {
        viewModel.onEmailChange("user@test.com")
        viewModel.onPasswordChange("password123")
        viewModel.submit()
        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `failed sign-in surfaces error message`() = runTest {
        repo.signInResult = Result.failure(Exception("Invalid credentials"))
        viewModel.onEmailChange("user@test.com")
        viewModel.onPasswordChange("password123")
        viewModel.submit()
        assertNotNull(viewModel.state.value.error)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `onEmailChange clears existing error`() = runTest {
        repo.signInResult = Result.failure(Exception("error"))
        viewModel.onEmailChange("user@test.com")
        viewModel.onPasswordChange("password123")
        viewModel.submit()
        viewModel.onEmailChange("new@test.com")
        assertNull(viewModel.state.value.error)
    }
}
