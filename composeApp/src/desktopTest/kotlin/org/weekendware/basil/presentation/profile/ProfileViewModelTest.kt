package org.weekendware.basil.presentation.profile

import basil.composeapp.generated.resources.Res
import basil.composeapp.generated.resources.error_profile_save_failed
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.weekendware.basil.data.repository.PreferencesRepository
import org.weekendware.basil.data.repository.UserRepository
import org.weekendware.basil.domain.model.User
import org.weekendware.basil.domain.usecase.GetBgTargetsUseCase
import org.weekendware.basil.domain.usecase.GetUserUseCase
import org.weekendware.basil.domain.usecase.SetBgTargetsUseCase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ProfileViewModelTest {

    private val userRepository        = mock<UserRepository>()
    private val preferencesRepository = mock<PreferencesRepository>()

    private val getUser       = GetUserUseCase(userRepository)
    private val getBgTargets  = GetBgTargetsUseCase(preferencesRepository)
    private val setBgTargets  = SetBgTargetsUseCase(preferencesRepository)

    private fun makeVm(
        name: String  = "Test User",
        email: String = "test@example.com",
        low: Double   = 3.9,
        high: Double  = 10.0
    ): ProfileViewModel {
        whenever(userRepository.getAll()).thenReturn(listOf(User("1", name, email)))
        whenever(preferencesRepository.getBgTargetLow()).thenReturn(low)
        whenever(preferencesRepository.getBgTargetHigh()).thenReturn(high)
        return ProfileViewModel(getUser, getBgTargets, setBgTargets)
    }

    // ── initial load ──────────────────────────────────────────

    @Test
    fun `loads name and email from user repository`() {
        val vm = makeVm(name = "Gavin Dunne", email = "gavin@weekendware.io")

        assertEquals("Gavin Dunne", vm.state.value.name)
        assertEquals("gavin@weekendware.io", vm.state.value.email)
    }

    @Test
    fun `loads target BG range from preferences`() {
        val vm = makeVm(low = 4.0, high = 9.0)

        // Whole numbers are formatted without decimal (4.0 → "4", 9.0 → "9")
        assertEquals("4", vm.state.value.targetBgLow)
        assertEquals("9", vm.state.value.targetBgHigh)
    }

    @Test
    fun `name and email are empty when no user exists`() {
        whenever(userRepository.getAll()).thenReturn(emptyList())
        whenever(preferencesRepository.getBgTargetLow()).thenReturn(3.9)
        whenever(preferencesRepository.getBgTargetHigh()).thenReturn(10.0)
        val vm = ProfileViewModel(getUser, getBgTargets, setBgTargets)

        assertEquals("", vm.state.value.name)
        assertEquals("", vm.state.value.email)
    }

    @Test
    fun `isEditing starts false`() {
        val vm = makeVm()
        assertFalse(vm.state.value.isEditing)
    }

    // ── edit / cancel ─────────────────────────────────────────

    @Test
    fun `onEditClick sets isEditing to true`() {
        val vm = makeVm()
        vm.onEditClick()
        assertTrue(vm.state.value.isEditing)
    }

    @Test
    fun `onCancelClick sets isEditing to false and reloads data`() {
        val vm = makeVm(low = 3.9, high = 10.0)
        vm.onEditClick()
        vm.onTargetLowChange("1.0")
        vm.onCancelClick()

        assertFalse(vm.state.value.isEditing)
        assertEquals("3.9", vm.state.value.targetBgLow)
    }

    // ── field changes ─────────────────────────────────────────

    @Test
    fun `onTargetLowChange updates targetBgLow in state`() {
        val vm = makeVm()
        vm.onTargetLowChange("4.5")
        assertEquals("4.5", vm.state.value.targetBgLow)
    }

    @Test
    fun `onTargetHighChange updates targetBgHigh in state`() {
        val vm = makeVm()
        vm.onTargetHighChange("11.0")
        assertEquals("11.0", vm.state.value.targetBgHigh)
    }

    // ── save ──────────────────────────────────────────────────

    @Test
    fun `onSaveClick persists targets and exits edit mode on success`() {
        val vm = makeVm()
        vm.onEditClick()
        vm.onTargetLowChange("4.0")
        vm.onTargetHighChange("9.0")
        vm.onSaveClick()

        verify(preferencesRepository).setBgTargetLow(4.0)
        verify(preferencesRepository).setBgTargetHigh(9.0)
        assertFalse(vm.state.value.isEditing)
        assertNull(vm.state.value.error)
    }

    @Test
    fun `onSaveClick sets error when low is not a valid number`() {
        val vm = makeVm()
        vm.onEditClick()
        vm.onTargetLowChange("abc")
        vm.onTargetHighChange("10.0")
        vm.onSaveClick()

        assertEquals(Res.string.error_profile_save_failed, vm.state.value.error)
        assertTrue(vm.state.value.isEditing)
    }

    @Test
    fun `onSaveClick sets error when low is greater than or equal to high`() {
        val vm = makeVm()
        vm.onEditClick()
        vm.onTargetLowChange("10.0")
        vm.onTargetHighChange("4.0")
        vm.onSaveClick()

        assertEquals(Res.string.error_profile_save_failed, vm.state.value.error)
    }

    @Test
    fun `onSaveClick sets error when repository throws`() {
        val vm = makeVm()
        vm.onEditClick()
        vm.onTargetLowChange("4.0")
        vm.onTargetHighChange("9.0")
        doThrow(RuntimeException("DB error")).whenever(preferencesRepository).setBgTargetLow(4.0)

        vm.onSaveClick()

        assertEquals(Res.string.error_profile_save_failed, vm.state.value.error)
    }

    // ── clearError ────────────────────────────────────────────

    @Test
    fun `clearError removes error from state`() {
        val vm = makeVm()
        vm.onEditClick()
        vm.onTargetLowChange("abc")
        vm.onSaveClick()
        assertEquals(Res.string.error_profile_save_failed, vm.state.value.error)

        vm.clearError()

        assertNull(vm.state.value.error)
    }
}
