package org.weekendware.basil.presentation.dashboard

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
        id           = id,
        timestamp    = Clock.System.now().toEpochMilliseconds() - timestampOffset,
        bgValue      = bgValue,
        bgUnit       = bgUnit,
        insulinUnits = insulinUnits,
        carbsGrams   = carbsGrams
    )

    // ── initial state ─────────────────────────────────────────

    @Test
    fun `initial state is empty when repository returns no entries`() {
        whenever(logRepository.getRecent(100)).thenReturn(emptyList())
        val vm = DashboardViewModel(logRepository)

        assertTrue(vm.state.value.todayEntries.isEmpty())
        assertNull(vm.state.value.lastBgEntry)
    }

    // ── refresh ───────────────────────────────────────────────

    @Test
    fun `refresh populates todayEntries with entries from today`() {
        val todayEntry = makeEntry(id = 1L, bgValue = 100.0, bgUnit = BgUnit.MGDL)
        whenever(logRepository.getRecent(100)).thenReturn(listOf(todayEntry))

        val vm = DashboardViewModel(logRepository)

        assertEquals(1, vm.state.value.todayEntries.size)
        assertEquals(1L, vm.state.value.todayEntries.first().id)
    }

    @Test
    fun `refresh filters out entries from previous days`() {
        val yesterday = makeEntry(id = 1L, bgValue = 100.0, bgUnit = BgUnit.MGDL,
            timestampOffset = 25 * 60 * 60 * 1000L)  // 25 hours ago
        whenever(logRepository.getRecent(100)).thenReturn(listOf(yesterday))

        val vm = DashboardViewModel(logRepository)

        assertTrue(vm.state.value.todayEntries.isEmpty())
    }

    @Test
    fun `refresh sets lastBgEntry to first entry with a bgValue`() {
        val withBg    = makeEntry(id = 1L, bgValue = 120.0, bgUnit = BgUnit.MGDL)
        val withoutBg = makeEntry(id = 2L, insulinUnits = 4.0)
        whenever(logRepository.getRecent(100)).thenReturn(listOf(withBg, withoutBg))

        val vm = DashboardViewModel(logRepository)

        assertEquals(1L, vm.state.value.lastBgEntry?.id)
    }

    @Test
    fun `lastBgEntry is null when no entry has a bgValue`() {
        val entry = makeEntry(id = 1L, insulinUnits = 4.0)
        whenever(logRepository.getRecent(100)).thenReturn(listOf(entry))

        val vm = DashboardViewModel(logRepository)

        assertNull(vm.state.value.lastBgEntry)
    }

    @Test
    fun `lastBgEntry can come from outside today`() {
        val yesterday = makeEntry(id = 1L, bgValue = 95.0, bgUnit = BgUnit.MGDL,
            timestampOffset = 25 * 60 * 60 * 1000L)
        whenever(logRepository.getRecent(100)).thenReturn(listOf(yesterday))

        val vm = DashboardViewModel(logRepository)

        // Not in today's list…
        assertTrue(vm.state.value.todayEntries.isEmpty())
        // …but still the last known BG reading
        assertEquals(1L, vm.state.value.lastBgEntry?.id)
    }

    @Test
    fun `refresh updates state after new entries arrive`() {
        whenever(logRepository.getRecent(100)).thenReturn(emptyList())
        val vm = DashboardViewModel(logRepository)
        assertTrue(vm.state.value.todayEntries.isEmpty())

        val newEntry = makeEntry(id = 5L, bgValue = 110.0, bgUnit = BgUnit.MGDL)
        whenever(logRepository.getRecent(100)).thenReturn(listOf(newEntry))
        vm.refresh()

        assertEquals(1, vm.state.value.todayEntries.size)
    }

    // ── log sheet visibility ──────────────────────────────────

    @Test
    fun `showLogSheet starts as false`() {
        whenever(logRepository.getRecent(100)).thenReturn(emptyList())
        val vm = DashboardViewModel(logRepository)

        assertFalse(vm.showLogSheet.value)
    }

    @Test
    fun `openLogSheet sets showLogSheet to true`() {
        whenever(logRepository.getRecent(100)).thenReturn(emptyList())
        val vm = DashboardViewModel(logRepository)

        vm.openLogSheet()

        assertTrue(vm.showLogSheet.value)
    }

    @Test
    fun `closeLogSheet sets showLogSheet to false`() {
        whenever(logRepository.getRecent(100)).thenReturn(emptyList())
        val vm = DashboardViewModel(logRepository)

        vm.openLogSheet()
        vm.closeLogSheet()

        assertFalse(vm.showLogSheet.value)
    }
}
