package org.weekendware.basil.presentation.dashboard

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.weekendware.basil.data.repository.LogRepository
import org.weekendware.basil.domain.model.BgUnit
import org.weekendware.basil.domain.model.LogEntry
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DashboardViewModelTest {

    private val logRepository = mock<LogRepository>()

    private fun makeEntry(
        id: Long,
        bgValue: Double? = null,
        bgUnit: BgUnit? = null,
        insulinUnits: Double? = null,
        carbsGrams: Double? = null,
        timestampOffset: Long = 0L
    ) = LogEntry(
        id = id,
        timestamp = Clock.System.now().toEpochMilliseconds() - timestampOffset,
        bgValue = bgValue,
        bgUnit = bgUnit,
        insulinUnits = insulinUnits,
        carbsGrams = carbsGrams
    )

    // ── initial state ─────────────────────────────────────────

    @Test
    fun `initial state is empty when repository returns no entries`() = runTest {
        whenever(logRepository.getRecent(100)).thenReturn(flowOf(emptyList()))
        val vm = DashboardViewModel(logRepository, this)
        advanceUntilIdle()

        assertTrue(vm.state.value.todayEntries.isEmpty())
        assertNull(vm.state.value.lastBgEntry)
    }

    // ── todayEntries ──────────────────────────────────────────

    @Test
    fun `state populates todayEntries with entries from today`() = runTest {
        val todayEntry = makeEntry(id = 1L, bgValue = 100.0, bgUnit = BgUnit.MGDL)
        whenever(logRepository.getRecent(100)).thenReturn(flowOf(listOf(todayEntry)))
        val vm = DashboardViewModel(logRepository, this)
        advanceUntilIdle()

        assertEquals(1, vm.state.value.todayEntries.size)
        assertEquals(1L, vm.state.value.todayEntries.first().id)
    }

    @Test
    fun `state filters out entries from previous days`() = runTest {
        val yesterday = makeEntry(
            id = 1L,
            bgValue = 100.0,
            bgUnit = BgUnit.MGDL,
            timestampOffset = 25 * 60 * 60 * 1000L
        )
        whenever(logRepository.getRecent(100)).thenReturn(flowOf(listOf(yesterday)))
        val vm = DashboardViewModel(logRepository, this)
        advanceUntilIdle()

        assertTrue(vm.state.value.todayEntries.isEmpty())
    }

    // ── lastBgEntry ───────────────────────────────────────────

    @Test
    fun `lastBgEntry is set to first entry with a bgValue`() = runTest {
        val withBg = makeEntry(id = 1L, bgValue = 120.0, bgUnit = BgUnit.MGDL)
        val withoutBg = makeEntry(id = 2L, insulinUnits = 4.0)
        whenever(logRepository.getRecent(100)).thenReturn(flowOf(listOf(withBg, withoutBg)))
        val vm = DashboardViewModel(logRepository, this)
        advanceUntilIdle()

        assertEquals(1L, vm.state.value.lastBgEntry?.id)
    }

    @Test
    fun `lastBgEntry is null when no entry has a bgValue`() = runTest {
        val entry = makeEntry(id = 1L, insulinUnits = 4.0)
        whenever(logRepository.getRecent(100)).thenReturn(flowOf(listOf(entry)))
        val vm = DashboardViewModel(logRepository, this)
        advanceUntilIdle()

        assertNull(vm.state.value.lastBgEntry)
    }

    @Test
    fun `lastBgEntry can come from outside today`() = runTest {
        val yesterday = makeEntry(
            id = 1L,
            bgValue = 95.0,
            bgUnit = BgUnit.MGDL,
            timestampOffset = 25 * 60 * 60 * 1000L
        )
        whenever(logRepository.getRecent(100)).thenReturn(flowOf(listOf(yesterday)))
        val vm = DashboardViewModel(logRepository, this)
        advanceUntilIdle()

        assertTrue(vm.state.value.todayEntries.isEmpty())
        assertEquals(1L, vm.state.value.lastBgEntry?.id)
    }

    // ── live updates ──────────────────────────────────────────

    @Test
    fun `state updates automatically when repository emits new entries`() = runTest {
        val source = MutableStateFlow<List<LogEntry>>(emptyList())
        whenever(logRepository.getRecent(100)).thenReturn(source)
        // UnconfinedTestDispatcher executes emissions eagerly — no advanceUntilIdle needed.
        // The scope is explicitly cancelled at the end so runTest doesn't flag it.
        @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
        val vmScope = CoroutineScope(UnconfinedTestDispatcher(testScheduler))
        val vm = DashboardViewModel(logRepository, vmScope)

        assertTrue(vm.state.value.todayEntries.isEmpty())

        source.value = listOf(makeEntry(id = 5L, bgValue = 110.0, bgUnit = BgUnit.MGDL))

        assertEquals(1, vm.state.value.todayEntries.size)
        vmScope.cancel()
    }

    // ── log sheet visibility ──────────────────────────────────

    @Test
    fun `showLogSheet starts as false`() = runTest {
        whenever(logRepository.getRecent(100)).thenReturn(flowOf(emptyList()))
        val vm = DashboardViewModel(logRepository, this)

        assertFalse(vm.showLogSheet.value)
    }

    @Test
    fun `openLogSheet sets showLogSheet to true`() = runTest {
        whenever(logRepository.getRecent(100)).thenReturn(flowOf(emptyList()))
        val vm = DashboardViewModel(logRepository, this)

        vm.openLogSheet()

        assertTrue(vm.showLogSheet.value)
    }

    @Test
    fun `closeLogSheet sets showLogSheet to false`() = runTest {
        whenever(logRepository.getRecent(100)).thenReturn(flowOf(emptyList()))
        val vm = DashboardViewModel(logRepository, this)

        vm.openLogSheet()
        vm.closeLogSheet()

        assertFalse(vm.showLogSheet.value)
    }
}
