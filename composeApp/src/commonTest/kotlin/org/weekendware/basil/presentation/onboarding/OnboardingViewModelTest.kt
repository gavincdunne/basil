@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package org.weekendware.basil.presentation.onboarding

import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.weekendware.basil.data.repository.FakeAuthRepository
import org.weekendware.basil.data.repository.FakeOnboardingLocalRepository
import org.weekendware.basil.data.repository.FakeProfileRepository
import org.weekendware.basil.data.repository.FakeUserRepository
import org.weekendware.basil.domain.model.DiagnosisDuration
import org.weekendware.basil.domain.model.Goal
import org.weekendware.basil.domain.model.ManagementType
import org.weekendware.basil.domain.model.OnboardingPersistedState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class OnboardingViewModelTest {

    // ── helpers ───────────────────────────────────────────────────────────────

    private fun makeVm(
        localRepo: FakeOnboardingLocalRepository = FakeOnboardingLocalRepository(),
        profileRepo: FakeProfileRepository = FakeProfileRepository(),
        authRepo: FakeAuthRepository = FakeAuthRepository().also { it.setSignedIn(true) },
        userRepo: FakeUserRepository = FakeUserRepository(),
        scope: kotlinx.coroutines.CoroutineScope
    ) = OnboardingViewModel(localRepo, profileRepo, authRepo, userRepo, coroutineScope = scope)

    // ── ONB-001: fresh user ───────────────────────────────────────────────────

    @Test
    fun `ONB-001 fresh user with no prior state starts at NAME step`() = runTest {
        val vm = makeVm(scope = this)
        advanceUntilIdle()

        assertEquals(OnboardingStep.NAME, vm.state.value.currentStep)
        assertFalse(vm.state.value.isComplete)
        assertFalse(vm.state.value.isResuming)
    }

    // ── ONB-002: returning user, DataStore complete ───────────────────────────

    @Test
    fun `ONB-002 DataStore isComplete true sets isComplete in state and skips Supabase fetch`() = runTest {
        val localRepo = FakeOnboardingLocalRepository()
        localRepo.setState(
            OnboardingPersistedState(
                name = "Alice",
                managementType = ManagementType.PUMP,
                diagnosisDuration = DiagnosisDuration.ONE_TO_FIVE,
                goal = Goal.SOMEONE_WHO_GETS_IT,
                isComplete = true
            )
        )
        val profileRepo = FakeProfileRepository()
        val vm = makeVm(localRepo = localRepo, profileRepo = profileRepo, scope = this)
        advanceUntilIdle()

        assertTrue(vm.state.value.isComplete)
        assertEquals(0, profileRepo.fetchCallCount)
    }

    // ── ONB-003: new device, Supabase complete row ────────────────────────────

    @Test
    fun `ONB-003 Supabase complete row on new device seeds DataStore and sets isComplete`() = runTest {
        val localRepo = FakeOnboardingLocalRepository()
        val profileRepo = FakeProfileRepository()
        profileRepo.fetchResult = Result.success(
            OnboardingPersistedState(
                name = "Alice",
                managementType = ManagementType.CLOSED_LOOP,
                diagnosisDuration = DiagnosisDuration.TEN_PLUS,
                goal = Goal.PATTERNS,
                isComplete = true
            )
        )
        val vm = makeVm(localRepo = localRepo, profileRepo = profileRepo, scope = this)
        advanceUntilIdle()

        assertTrue(vm.state.value.isComplete)
        assertTrue(localRepo.markCompleteCalled)
    }

    // ── ONB-004: Supabase failure on name step ────────────────────────────────

    @Test
    fun `ONB-004 Supabase upsert failure on name blocks advancement and does not write DataStore`() = runTest {
        val localRepo = FakeOnboardingLocalRepository()
        val profileRepo = FakeProfileRepository()
        profileRepo.upsertResult = Result.failure(RuntimeException("network error"))
        val vm = makeVm(localRepo = localRepo, profileRepo = profileRepo, scope = this)
        advanceUntilIdle()

        vm.onNameSubmitted("Alice")
        advanceUntilIdle()

        assertEquals(OnboardingStep.NAME, vm.state.value.currentStep)
        assertNotNull(vm.state.value.error)
        assertFalse(vm.state.value.isLoading)
        assertTrue(localRepo.saveNameCalls.isEmpty())
    }

    // ── ONB-005: Supabase failure on chip step ────────────────────────────────

    @Test
    fun `ONB-005 Supabase upsert failure on management type chip blocks advancement`() = runTest {
        val localRepo = FakeOnboardingLocalRepository()
        localRepo.setState(OnboardingPersistedState(name = "Alice"))
        val profileRepo = FakeProfileRepository()
        profileRepo.upsertResult = Result.failure(RuntimeException("network error"))
        val vm = makeVm(localRepo = localRepo, profileRepo = profileRepo, scope = this)
        advanceUntilIdle()

        vm.onManagementTypeSelected(ManagementType.PUMP)
        advanceUntilIdle()

        assertEquals(OnboardingStep.MANAGEMENT_TYPE, vm.state.value.currentStep)
        assertNotNull(vm.state.value.error)
        assertFalse(vm.state.value.isLoading)
        assertTrue(localRepo.saveManagementTypeCalls.isEmpty())
    }

    // ── ONB-006: Supabase failure on final step ───────────────────────────────

    @Test
    fun `ONB-006 Supabase failure on goal step does not call markComplete and keeps isComplete false`() = runTest {
        val localRepo = FakeOnboardingLocalRepository()
        localRepo.setState(
            OnboardingPersistedState(
                name = "Alice",
                managementType = ManagementType.INJECTIONS,
                diagnosisDuration = DiagnosisDuration.LESS_THAN_ONE
            )
        )
        val profileRepo = FakeProfileRepository()
        profileRepo.upsertResult = Result.failure(RuntimeException("server error"))
        val vm = makeVm(localRepo = localRepo, profileRepo = profileRepo, scope = this)
        advanceUntilIdle()

        vm.onGoalSelected(Goal.VENT)
        advanceUntilIdle()

        assertFalse(localRepo.markCompleteCalled)
        assertFalse(vm.state.value.isComplete)
        assertNotNull(vm.state.value.error)
    }

    // ── ONB-007: final step success ───────────────────────────────────────────

    @Test
    fun `ONB-007 final step success upserts with isComplete true then calls markComplete`() = runTest {
        val localRepo = FakeOnboardingLocalRepository()
        localRepo.setState(
            OnboardingPersistedState(
                name = "Alice",
                managementType = ManagementType.PUMP,
                diagnosisDuration = DiagnosisDuration.FIVE_TO_TEN
            )
        )
        val profileRepo = FakeProfileRepository()
        val vm = makeVm(localRepo = localRepo, profileRepo = profileRepo, scope = this)
        advanceUntilIdle()

        vm.onGoalSelected(Goal.NOT_SURE)
        advanceUntilIdle()

        val lastUpsert = profileRepo.upsertCalls.last()
        assertTrue(lastUpsert.isComplete)
        assertTrue(localRepo.markCompleteCalled)
        assertTrue(vm.state.value.isComplete)
    }

    // ── ONB-023: NOT_SURE goal is valid ───────────────────────────────────────

    @Test
    fun `ONB-023 Goal NOT_SURE is a valid answer and advances to COMPLETE step`() = runTest {
        val localRepo = FakeOnboardingLocalRepository()
        localRepo.setState(
            OnboardingPersistedState(
                name = "Alice",
                managementType = ManagementType.CLOSED_LOOP,
                diagnosisDuration = DiagnosisDuration.TEN_PLUS
            )
        )
        val vm = makeVm(localRepo = localRepo, scope = this)
        advanceUntilIdle()

        vm.onGoalSelected(Goal.NOT_SURE)
        advanceUntilIdle()

        assertEquals(OnboardingStep.COMPLETE, vm.state.value.currentStep)
        assertTrue(vm.state.value.isComplete)
    }

    // ── ONB-024: DataStore only written after Supabase confirms ──────────────

    @Test
    fun `ONB-024 DataStore not written when Supabase upsert fails`() = runTest {
        val localRepo = FakeOnboardingLocalRepository()
        localRepo.setState(OnboardingPersistedState(name = "Alice"))
        val profileRepo = FakeProfileRepository()
        profileRepo.upsertResult = Result.failure(RuntimeException("network"))
        val vm = makeVm(localRepo = localRepo, profileRepo = profileRepo, scope = this)
        advanceUntilIdle()

        vm.onManagementTypeSelected(ManagementType.INJECTIONS)
        advanceUntilIdle()

        assertTrue(localRepo.saveManagementTypeCalls.isEmpty())
    }

    @Test
    fun `ONB-024 DataStore written only after Supabase upsert succeeds`() = runTest {
        val localRepo = FakeOnboardingLocalRepository()
        localRepo.setState(OnboardingPersistedState(name = "Alice"))
        val profileRepo = FakeProfileRepository()
        profileRepo.upsertResult = Result.success(Unit)
        val vm = makeVm(localRepo = localRepo, profileRepo = profileRepo, scope = this)
        advanceUntilIdle()

        vm.onManagementTypeSelected(ManagementType.PUMP)
        advanceUntilIdle()

        assertEquals(1, profileRepo.upsertCalls.size)
        assertEquals(listOf(ManagementType.PUMP), localRepo.saveManagementTypeCalls)
    }

    // ── ONB-025: retry after failure ──────────────────────────────────────────

    @Test
    fun `ONB-025 retry after failure advances when second attempt succeeds`() = runTest {
        val localRepo = FakeOnboardingLocalRepository()
        localRepo.setState(OnboardingPersistedState(name = "Alice"))
        val profileRepo = FakeProfileRepository()
        profileRepo.upsertResult = Result.failure(RuntimeException("network"))
        val vm = makeVm(localRepo = localRepo, profileRepo = profileRepo, scope = this)
        advanceUntilIdle()

        vm.onManagementTypeSelected(ManagementType.PUMP)
        advanceUntilIdle()
        assertEquals(OnboardingStep.MANAGEMENT_TYPE, vm.state.value.currentStep)

        profileRepo.upsertResult = Result.success(Unit)
        vm.onManagementTypeSelected(ManagementType.PUMP)
        advanceUntilIdle()

        assertEquals(OnboardingStep.DIAGNOSIS_DURATION, vm.state.value.currentStep)
        assertNull(vm.state.value.error)
    }

    // ── ONB-026: DataStore partial state → resume ─────────────────────────────

    @Test
    fun `ONB-026 DataStore with name only resumes at MANAGEMENT_TYPE and sets isResuming true`() = runTest {
        val localRepo = FakeOnboardingLocalRepository()
        localRepo.setState(OnboardingPersistedState(name = "Alice"))
        val profileRepo = FakeProfileRepository()
        profileRepo.fetchResult = Result.success(null)
        val vm = makeVm(localRepo = localRepo, profileRepo = profileRepo, scope = this)
        advanceUntilIdle()

        assertEquals(OnboardingStep.MANAGEMENT_TYPE, vm.state.value.currentStep)
        assertTrue(vm.state.value.isResuming)
        assertEquals("Alice", vm.state.value.name)
    }

    // ── ONB-027: Supabase partial row → resume ────────────────────────────────

    @Test
    fun `ONB-027 Supabase partial row on new device seeds DataStore and sets isResuming true`() = runTest {
        val localRepo = FakeOnboardingLocalRepository()
        val profileRepo = FakeProfileRepository()
        profileRepo.fetchResult = Result.success(
            OnboardingPersistedState(name = "Alice")
        )
        val vm = makeVm(localRepo = localRepo, profileRepo = profileRepo, scope = this)
        advanceUntilIdle()

        assertEquals(OnboardingStep.MANAGEMENT_TYPE, vm.state.value.currentStep)
        assertTrue(vm.state.value.isResuming)
        assertEquals("Alice", vm.state.value.name)
        assertEquals(listOf("Alice"), localRepo.saveNameCalls)
    }

    // ── ONB-028: no prior state → fresh start ─────────────────────────────────

    @Test
    fun `ONB-028 no DataStore state and no Supabase row starts fresh with isResuming false`() = runTest {
        val vm = makeVm(scope = this)
        advanceUntilIdle()

        assertEquals(OnboardingStep.NAME, vm.state.value.currentStep)
        assertFalse(vm.state.value.isResuming)
        assertNull(vm.state.value.name)
    }

    // ── ONB-038: whitespace name rejected ────────────────────────────────────

    @Test
    fun `ONB-038 whitespace-only name is rejected and upsertStep is not called`() = runTest {
        val profileRepo = FakeProfileRepository()
        val vm = makeVm(profileRepo = profileRepo, scope = this)
        advanceUntilIdle()

        vm.onNameSubmitted("   ")
        advanceUntilIdle()

        assertEquals(OnboardingStep.NAME, vm.state.value.currentStep)
        assertTrue(profileRepo.upsertCalls.isEmpty())
    }

    @Test
    fun `ONB-038 empty name is rejected and upsertStep is not called`() = runTest {
        val profileRepo = FakeProfileRepository()
        val vm = makeVm(profileRepo = profileRepo, scope = this)
        advanceUntilIdle()

        vm.onNameSubmitted("")
        advanceUntilIdle()

        assertEquals(OnboardingStep.NAME, vm.state.value.currentStep)
        assertTrue(profileRepo.upsertCalls.isEmpty())
    }

    // ── ONB-039: UserRepository.insert() on completion ───────────────────────

    @Test
    fun `ONB-039 UserRepository insert called with name and email on onboarding completion`() = runTest {
        val localRepo = FakeOnboardingLocalRepository()
        localRepo.setState(
            OnboardingPersistedState(
                name = "Alice",
                managementType = ManagementType.PUMP,
                diagnosisDuration = DiagnosisDuration.ONE_TO_FIVE
            )
        )
        val userRepo = FakeUserRepository()
        val authRepo = FakeAuthRepository().also { it.setSignedIn(true) }
        val vm = makeVm(localRepo = localRepo, userRepo = userRepo, authRepo = authRepo, scope = this)
        advanceUntilIdle()

        vm.onGoalSelected(Goal.SOMEONE_WHO_GETS_IT)
        advanceUntilIdle()

        assertEquals(1, userRepo.insertCalls.size)
        val call = userRepo.insertCalls.first()
        assertEquals("Alice", call.name)
        assertEquals("fake-uid", call.id)
        assertEquals("fake@example.com", call.email)
    }

    // ── isResuming variants ───────────────────────────────────────────────────

    @Test
    fun `isResuming true with name known reflects name in state`() = runTest {
        val localRepo = FakeOnboardingLocalRepository()
        val profileRepo = FakeProfileRepository()
        profileRepo.fetchResult = Result.success(
            OnboardingPersistedState(name = "Alice")
        )
        val vm = makeVm(localRepo = localRepo, profileRepo = profileRepo, scope = this)
        advanceUntilIdle()

        assertTrue(vm.state.value.isResuming)
        assertNotNull(vm.state.value.name)
    }

    @Test
    fun `isResuming true with name unknown has null name in state`() = runTest {
        val localRepo = FakeOnboardingLocalRepository()
        val profileRepo = FakeProfileRepository()
        profileRepo.fetchResult = Result.success(
            OnboardingPersistedState()
        )
        val vm = makeVm(localRepo = localRepo, profileRepo = profileRepo, scope = this)
        advanceUntilIdle()

        assertFalse(vm.state.value.isResuming)
        assertNull(vm.state.value.name)
    }

    // ── step progression happy path ───────────────────────────────────────────

    @Test
    fun `onNameSubmitted with valid name advances to MANAGEMENT_TYPE step`() = runTest {
        val vm = makeVm(scope = this)
        advanceUntilIdle()

        vm.onNameSubmitted("Alice")
        advanceUntilIdle()

        assertEquals(OnboardingStep.MANAGEMENT_TYPE, vm.state.value.currentStep)
        assertEquals("Alice", vm.state.value.name)
    }

    @Test
    fun `onManagementTypeSelected advances to DIAGNOSIS_DURATION step`() = runTest {
        val localRepo = FakeOnboardingLocalRepository()
        localRepo.setState(OnboardingPersistedState(name = "Alice"))
        val vm = makeVm(localRepo = localRepo, scope = this)
        advanceUntilIdle()

        vm.onManagementTypeSelected(ManagementType.CLOSED_LOOP)
        advanceUntilIdle()

        assertEquals(OnboardingStep.DIAGNOSIS_DURATION, vm.state.value.currentStep)
    }

    @Test
    fun `onDiagnosisDurationSelected advances to GOAL step`() = runTest {
        val localRepo = FakeOnboardingLocalRepository()
        localRepo.setState(
            OnboardingPersistedState(name = "Alice", managementType = ManagementType.INJECTIONS)
        )
        val vm = makeVm(localRepo = localRepo, scope = this)
        advanceUntilIdle()

        vm.onDiagnosisDurationSelected(DiagnosisDuration.FIVE_TO_TEN)
        advanceUntilIdle()

        assertEquals(OnboardingStep.GOAL, vm.state.value.currentStep)
    }

    // ── loading state ─────────────────────────────────────────────────────────

    @Test
    fun `isLoading is false after name submission succeeds`() = runTest {
        val vm = makeVm(scope = this)
        advanceUntilIdle()

        vm.onNameSubmitted("Alice")
        advanceUntilIdle()

        assertFalse(vm.state.value.isLoading)
    }

    @Test
    fun `isLoading is false after name submission fails`() = runTest {
        val profileRepo = FakeProfileRepository()
        profileRepo.upsertResult = Result.failure(RuntimeException("error"))
        val vm = makeVm(profileRepo = profileRepo, scope = this)
        advanceUntilIdle()

        vm.onNameSubmitted("Alice")
        advanceUntilIdle()

        assertFalse(vm.state.value.isLoading)
    }

    // ── clearError ────────────────────────────────────────────────────────────

    @Test
    fun `clearError removes error from state`() = runTest {
        val profileRepo = FakeProfileRepository()
        profileRepo.upsertResult = Result.failure(RuntimeException("network error"))
        val vm = makeVm(profileRepo = profileRepo, scope = this)
        advanceUntilIdle()

        vm.onNameSubmitted("Alice")
        advanceUntilIdle()
        assertNotNull(vm.state.value.error)

        vm.clearError()

        assertNull(vm.state.value.error)
    }

    // ── currentStep computed property ─────────────────────────────────────────

    @Test
    fun `currentStep is NAME when name is null`() {
        val state = OnboardingUiState()
        assertEquals(OnboardingStep.NAME, state.currentStep)
    }

    @Test
    fun `currentStep is MANAGEMENT_TYPE when only name is set`() {
        val state = OnboardingUiState(name = "Alice")
        assertEquals(OnboardingStep.MANAGEMENT_TYPE, state.currentStep)
    }

    @Test
    fun `currentStep is DIAGNOSIS_DURATION when name and managementType are set`() {
        val state = OnboardingUiState(name = "Alice", managementType = ManagementType.PUMP)
        assertEquals(OnboardingStep.DIAGNOSIS_DURATION, state.currentStep)
    }

    @Test
    fun `currentStep is GOAL when name managementType and diagnosisDuration are set`() {
        val state = OnboardingUiState(
            name = "Alice",
            managementType = ManagementType.PUMP,
            diagnosisDuration = DiagnosisDuration.ONE_TO_FIVE
        )
        assertEquals(OnboardingStep.GOAL, state.currentStep)
    }

    @Test
    fun `currentStep is COMPLETE when all fields are set`() {
        val state = OnboardingUiState(
            name = "Alice",
            managementType = ManagementType.INJECTIONS,
            diagnosisDuration = DiagnosisDuration.TEN_PLUS,
            goal = Goal.NOT_SURE
        )
        assertEquals(OnboardingStep.COMPLETE, state.currentStep)
    }
}
