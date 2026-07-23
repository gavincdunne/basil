package org.weekendware.basil.presentation.onboarding

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

/**
 * QA test suite for [SaveProgressViewModel]. Every case touching
 * [SaveProgressViewModel.onPasswordChange] is expected to fail with
 * `NotImplementedError` until Backend implements
 * [org.weekendware.basil.presentation.auth.PasswordStrengthValidator] —
 * same situation as [org.weekendware.basil.presentation.auth.NewPasswordViewModelTest].
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SaveProgressViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private lateinit var repo: FakeAuthRepository
    private lateinit var viewModel: SaveProgressViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(dispatcher)
        repo = FakeAuthRepository()
        viewModel = SaveProgressViewModel(repo)
    }

    @AfterTest
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is the choose-method step, nothing entered`() {
        val state = viewModel.state.value
        assertEquals(SaveProgressStep.ChooseMethod, state.step)
        assertEquals("", state.email)
        assertEquals("", state.password)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `onContinueWithEmail advances to the email-entry step`() {
        viewModel.onContinueWithEmail()
        assertEquals(SaveProgressStep.EmailEntry, viewModel.state.value.step)
    }

    @Test
    fun `onBackToChooseMethod returns to the choose-method step`() {
        viewModel.onContinueWithEmail()
        viewModel.onBackToChooseMethod()
        assertEquals(SaveProgressStep.ChooseMethod, viewModel.state.value.step)
    }

    @Test
    fun `onTogglePasswordVisibility flips the flag`() {
        assertFalse(viewModel.state.value.isPasswordVisible)
        viewModel.onTogglePasswordVisibility()
        assertTrue(viewModel.state.value.isPasswordVisible)
    }

    @Test
    fun `canCreateAccount is false until password strength is Strong`() {
        viewModel.onEmailChange("user@test.com")
        viewModel.onPasswordChange("weak")
        assertFalse(viewModel.state.value.canCreateAccount)
    }

    @Test
    fun `canCreateAccount is true with a strong password and a non-blank email`() {
        viewModel.onEmailChange("user@test.com")
        viewModel.onPasswordChange("Abcdefg1!")
        assertTrue(viewModel.state.value.canCreateAccount)
    }

    @Test
    fun `onCreateAccount does nothing when canCreateAccount is false`() = runTest {
        viewModel.onCreateAccount()
        assertFalse(repo.isSignedIn())
    }

    @Test
    fun `successful onCreateAccount signs up and clears loading`() = runTest {
        viewModel.onEmailChange("user@test.com")
        viewModel.onPasswordChange("Abcdefg1!")
        viewModel.onCreateAccount()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertTrue(repo.isSignedIn())
    }

    @Test
    fun `failed onCreateAccount surfaces an error`() = runTest {
        repo.signUpResult = Result.failure(Exception("email already registered"))
        viewModel.onEmailChange("user@test.com")
        viewModel.onPasswordChange("Abcdefg1!")
        viewModel.onCreateAccount()

        assertEquals(Res.string.error_auth_failed, viewModel.state.value.error)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `successful onGoogleSignUp signs in and clears loading`() = runTest {
        viewModel.onGoogleSignUp()
        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertTrue(repo.isSignedIn())
    }

    @Test
    fun `failed onAppleSignUp surfaces an error`() = runTest {
        repo.appleSignInResult = Result.failure(Exception("cancelled"))
        viewModel.onAppleSignUp()
        val state = viewModel.state.value
        assertEquals(Res.string.error_auth_failed, state.error)
        assertFalse(state.isLoading)
    }
}
