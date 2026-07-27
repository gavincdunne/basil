package org.weekendware.basil.presentation.auth

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import basil.composeapp.generated.resources.Res
import basil.composeapp.generated.resources.auth_passkey_second_failure
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
    fun `initial state is the email step in sign-in mode with nothing entered`() {
        val state = viewModel.state.value
        assertTrue(state.emailStep)
        assertEquals(AuthMode.SignIn, state.mode)
        assertEquals("", state.email)
        assertEquals("", state.password)
        assertFalse(state.isLoading)
        assertFalse(state.isPasswordVisible)
        assertNull(state.error)
    }

    @Test
    fun `canContinueEmail is false when email is blank`() {
        assertFalse(viewModel.state.value.canContinueEmail)
    }

    @Test
    fun `canContinueEmail is true once an email is entered`() {
        viewModel.onEmailChange("user@test.com")
        assertTrue(viewModel.state.value.canContinueEmail)
    }

    @Test
    fun `onContinueEmail does nothing when the email is blank`() {
        viewModel.onContinueEmail()
        assertTrue(viewModel.state.value.emailStep)
    }

    @Test
    fun `onContinueEmail with an unrecognized email advances to SignUp mode`() {
        viewModel.onEmailChange("new@test.com")
        viewModel.onContinueEmail()
        val state = viewModel.state.value
        assertFalse(state.emailStep)
        assertEquals(AuthMode.SignUp, state.mode)
    }

    @Test
    fun `onContinueEmail with a recognized email advances to SignIn mode`() = runTest {
        repo.signIn("returning@test.com", "password123")
        viewModel.onEmailChange("returning@test.com")
        viewModel.onContinueEmail()
        val state = viewModel.state.value
        assertFalse(state.emailStep)
        assertEquals(AuthMode.SignIn, state.mode)
    }

    @Test
    fun `onUseDifferentAccount resets to a fresh email step`() {
        viewModel.onEmailChange("user@test.com")
        viewModel.onPasswordChange("password123")
        viewModel.onContinueEmail()

        viewModel.onUseDifferentAccount()

        val state = viewModel.state.value
        assertTrue(state.emailStep)
        assertEquals("", state.email)
        assertEquals("", state.password)
    }

    @Test
    fun `onTogglePasswordVisibility flips the flag`() {
        assertFalse(viewModel.state.value.isPasswordVisible)
        viewModel.onTogglePasswordVisibility()
        assertTrue(viewModel.state.value.isPasswordVisible)
        viewModel.onTogglePasswordVisibility()
        assertFalse(viewModel.state.value.isPasswordVisible)
    }

    @Test
    fun `canSubmit is false when password is blank`() {
        assertFalse(viewModel.state.value.canSubmit)
    }

    @Test
    fun `canSubmit is true once a password is entered`() {
        viewModel.onPasswordChange("password123")
        assertTrue(viewModel.state.value.canSubmit)
    }

    /** Primes the fake so [email] is "recognized" by the detect-by-email heuristic, then signs back out. */
    private suspend fun primeRecognizedEmail(email: String) {
        repo.signIn(email, "priming-password")
        repo.signOut()
    }

    @Test
    fun `successful sign-in clears loading and error`() = runTest {
        primeRecognizedEmail("user@test.com")
        viewModel.onEmailChange("user@test.com")
        viewModel.onContinueEmail()
        assertEquals(AuthMode.SignIn, viewModel.state.value.mode)
        viewModel.onPasswordChange("password123")
        viewModel.submit()
        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertTrue(repo.isSignedIn())
    }

    @Test
    fun `failed sign-in surfaces error message`() = runTest {
        primeRecognizedEmail("user@test.com")
        repo.signInResult = Result.failure(Exception("Invalid credentials"))
        viewModel.onEmailChange("user@test.com")
        viewModel.onContinueEmail()
        assertEquals(AuthMode.SignIn, viewModel.state.value.mode)
        viewModel.onPasswordChange("password123")
        viewModel.submit()
        assertEquals(Res.string.error_auth_failed, viewModel.state.value.error)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `submit is a no-op in SignUp mode`() = runTest {
        viewModel.onEmailChange("new@test.com")
        viewModel.onContinueEmail() // unrecognized email -> SignUp mode
        assertEquals(AuthMode.SignUp, viewModel.state.value.mode)

        viewModel.submit()

        assertFalse(repo.isSignedIn())
    }

    @Test
    fun `onEmailChange clears existing error`() = runTest {
        primeRecognizedEmail("user@test.com")
        repo.signInResult = Result.failure(Exception("error"))
        viewModel.onEmailChange("user@test.com")
        viewModel.onContinueEmail()
        viewModel.onPasswordChange("password123")
        viewModel.submit()
        assertEquals(Res.string.error_auth_failed, viewModel.state.value.error) // sanity: error was actually set
        viewModel.onEmailChange("new@test.com")
        assertNull(viewModel.state.value.error)
    }

    @Test
    fun `successful Google sign-in clears loading and error`() = runTest {
        viewModel.onGoogleSignIn()
        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertTrue(repo.isSignedIn())
    }

    @Test
    fun `failed Apple sign-in surfaces error message`() = runTest {
        repo.appleSignInResult = Result.failure(Exception("cancelled"))
        viewModel.onAppleSignIn()
        val state = viewModel.state.value
        assertEquals(Res.string.error_auth_failed, state.error)
        assertFalse(state.isLoading)
    }

    // ── passkey prompt ────────────────────────────────────────
    // repo.passkeyEnrolled must be set before constructing the ViewModel —
    // isPasskeyAvailable is read once, on init, not observed reactively.

    @Test
    fun `isPasskeyAvailable is false when no passkey is enrolled on this device`() {
        assertFalse(viewModel.state.value.isPasskeyAvailable)
        assertFalse(viewModel.state.value.showPasskeyPrompt)
    }

    @Test
    fun `isPasskeyAvailable is true and showPasskeyPrompt is true when a passkey is enrolled`() {
        repo.passkeyEnrolled = true
        val vm = AuthViewModel(repo)
        assertTrue(vm.state.value.isPasskeyAvailable)
        assertTrue(vm.state.value.showPasskeyPrompt)
    }

    @Test
    fun `first biometric failure increments the attempt count without dropping to the form`() = runTest {
        repo.passkeyEnrolled = true
        repo.signInWithPasskeyResult = Result.failure(Exception("not recognized"))
        val vm = AuthViewModel(repo)

        vm.onPasskeyScreenEntered()

        val state = vm.state.value
        assertEquals(1, state.biometricAttemptCount)
        assertTrue(state.isPasskeyAvailable)
        assertTrue(state.showPasskeyPrompt)
        assertNull(state.error)
    }

    @Test
    fun `second biometric failure drops to the standard form with a contextual message`() = runTest {
        repo.passkeyEnrolled = true
        repo.signInWithPasskeyResult = Result.failure(Exception("not recognized"))
        val vm = AuthViewModel(repo)

        vm.onPasskeyScreenEntered()
        vm.onPasskeyRetry()

        val state = vm.state.value
        assertEquals(2, state.biometricAttemptCount)
        assertFalse(state.isPasskeyAvailable)
        assertFalse(state.showPasskeyPrompt)
        assertEquals(Res.string.auth_passkey_second_failure, state.error)
    }

    @Test
    fun `successful biometric auth establishes a session without changing attempt count`() = runTest {
        repo.passkeyEnrolled = true
        val vm = AuthViewModel(repo)

        vm.onPasskeyScreenEntered()

        assertTrue(repo.isSignedIn())
        assertEquals(0, vm.state.value.biometricAttemptCount)
    }

    @Test
    fun `onUseDifferentAccountFromPasskey hides the prompt without counting as a failure`() {
        repo.passkeyEnrolled = true
        val vm = AuthViewModel(repo)

        vm.onUseDifferentAccountFromPasskey()

        val state = vm.state.value
        assertFalse(state.isPasskeyAvailable)
        assertEquals(0, state.biometricAttemptCount)
    }
}
