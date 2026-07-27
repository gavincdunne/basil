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

/**
 * QA test suite for [NewPasswordViewModel]. Every case touching
 * [NewPasswordViewModel.onPasswordChange] is expected to fail with
 * `NotImplementedError` until Backend implements
 * [PasswordStrengthValidator.validate] — that dependency is scaffolded
 * but not yet implemented, and this ViewModel calls it on every
 * keystroke by design (live strength feedback).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class NewPasswordViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private lateinit var repo: FakeAuthRepository
    private lateinit var viewModel: NewPasswordViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(dispatcher)
        repo = FakeAuthRepository()
        viewModel = NewPasswordViewModel(repo)
    }

    @AfterTest
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has no strength and empty fields and cannot submit`() {
        val state = viewModel.state.value
        assertEquals("", state.password)
        assertEquals("", state.confirmPassword)
        assertEquals(PasswordStrength.None, state.passwordStrength)
        assertFalse(state.isPasswordVisible)
        assertFalse(state.passwordsMatch)
        assertFalse(state.canSubmit)
        assertNull(state.error)
    }

    @Test
    fun `passwordsMatch is false when confirm field is empty`() {
        // Does not touch onPasswordChange — safe to assert without the validator.
        viewModel.onConfirmPasswordChange("")
        assertFalse(viewModel.state.value.passwordsMatch)
    }

    @Test
    fun `onTogglePasswordVisibility flips the shared flag`() {
        assertFalse(viewModel.state.value.isPasswordVisible)
        viewModel.onTogglePasswordVisibility()
        assertTrue(viewModel.state.value.isPasswordVisible)
    }

    @Test
    fun `onPasswordChange updates strength and requirements from the validator`() {
        viewModel.onPasswordChange("Abcdefg1!")
        val state = viewModel.state.value
        assertEquals(PasswordStrength.Strong, state.passwordStrength)
        assertTrue(state.passwordRequirements.all { it.met })
    }

    @Test
    fun `canSubmit requires Strong strength even when the fields match`() {
        viewModel.onPasswordChange("weak")
        viewModel.onConfirmPasswordChange("weak")
        assertFalse(viewModel.state.value.canSubmit)
    }

    @Test
    fun `canSubmit is false when strong but the fields do not match`() {
        viewModel.onPasswordChange("Abcdefg1!")
        viewModel.onConfirmPasswordChange("Different1!")
        assertFalse(viewModel.state.value.canSubmit)
    }

    @Test
    fun `canSubmit is true when strong and the fields match`() {
        viewModel.onPasswordChange("Abcdefg1!")
        viewModel.onConfirmPasswordChange("Abcdefg1!")
        assertTrue(viewModel.state.value.canSubmit)
    }

    @Test
    fun `submit does nothing when canSubmit is false`() = runTest {
        viewModel.submit()
        assertEquals(0, repo.updatePasswordCallCount)
    }

    @Test
    fun `successful submit clears loading and records the new password`() = runTest {
        viewModel.onPasswordChange("Abcdefg1!")
        viewModel.onConfirmPasswordChange("Abcdefg1!")
        viewModel.submit()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertEquals("Abcdefg1!", repo.lastUpdatedPassword)
    }

    @Test
    fun `failed submit surfaces an error`() = runTest {
        repo.updatePasswordResult = Result.failure(Exception("session expired"))
        viewModel.onPasswordChange("Abcdefg1!")
        viewModel.onConfirmPasswordChange("Abcdefg1!")
        viewModel.submit()

        assertEquals(Res.string.error_auth_failed, viewModel.state.value.error)
        assertFalse(viewModel.state.value.isLoading)
    }
}
