package org.weekendware.basil.presentation.theme

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDateTime
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

// ─────────────────────────────────────────────────────────────
// AC coverage:
//   AC 1  — correct scheme returned for each hour slot
//   AC 3  — wake target is exactly 5s before the boundary (ms arithmetic)
//   AC 4  — boundary hours are 5, 10, 18, 21
//   AC 5  — ViewModel initialises to the correct slot for current time
//   AC 6  — dark/light variants returned correctly for each slot
//
// Note: coroutine timing (AC 3/4 in live conditions) is verified on device.
// The pure functions `msUntilSchemeWake` and `nextSchemeTransitionHour` that
// drive the coroutine are fully covered here.
// ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalCoroutinesApi::class)
class BasilThemeViewModelTest {

    // Use UnconfinedTestDispatcher so viewModelScope.launch works in non-Android tests.
    // Tests that only check StateFlow initial values do NOT use runTest — the initial
    // value is set synchronously in the ViewModel constructor, and calling runTest would
    // trigger advanceUntilIdle() which spins indefinitely on the while(true) boundary loop.
    private val dispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun teardown() {
        Dispatchers.resetMain()
    }

    // ── AC 1: basilSchemeForHour returns the correct slot ─────

    @Test
    fun `basilSchemeForHour returns morning for hour 5`() {
        assertEquals(basilMorningColorScheme(), basilSchemeForHour(5, isDark = false))
    }

    @Test
    fun `basilSchemeForHour returns morning for hour 9`() {
        assertEquals(basilMorningColorScheme(), basilSchemeForHour(9, isDark = false))
    }

    @Test
    fun `basilSchemeForHour returns day for hour 10`() {
        assertEquals(basilDayColorScheme(), basilSchemeForHour(10, isDark = false))
    }

    @Test
    fun `basilSchemeForHour returns day for hour 17`() {
        assertEquals(basilDayColorScheme(), basilSchemeForHour(17, isDark = false))
    }

    @Test
    fun `basilSchemeForHour returns evening for hour 18`() {
        assertEquals(basilEveningColorScheme(), basilSchemeForHour(18, isDark = false))
    }

    @Test
    fun `basilSchemeForHour returns evening for hour 20`() {
        assertEquals(basilEveningColorScheme(), basilSchemeForHour(20, isDark = false))
    }

    @Test
    fun `basilSchemeForHour returns night for hour 21`() {
        assertEquals(basilNightColorScheme(), basilSchemeForHour(21, isDark = false))
    }

    @Test
    fun `basilSchemeForHour returns night for hour 0`() {
        assertEquals(basilNightColorScheme(), basilSchemeForHour(0, isDark = false))
    }

    @Test
    fun `basilSchemeForHour returns night for hour 4`() {
        assertEquals(basilNightColorScheme(), basilSchemeForHour(4, isDark = false))
    }

    // ── AC 6: dark variants when isDark = true ────────────────

    @Test
    fun `basilSchemeForHour returns morning dark`() {
        val scheme = basilSchemeForHour(7, isDark = true)
        assertTrue(scheme.isDark)
        assertEquals(basilMorningDarkColorScheme(), scheme)
    }

    @Test
    fun `basilSchemeForHour returns day dark`() {
        val scheme = basilSchemeForHour(12, isDark = true)
        assertTrue(scheme.isDark)
        assertEquals(basilDayDarkColorScheme(), scheme)
    }

    @Test
    fun `basilSchemeForHour returns evening dark`() {
        val scheme = basilSchemeForHour(19, isDark = true)
        assertTrue(scheme.isDark)
        assertEquals(basilEveningDarkColorScheme(), scheme)
    }

    @Test
    fun `basilSchemeForHour returns night dark`() {
        val scheme = basilSchemeForHour(23, isDark = true)
        assertTrue(scheme.isDark)
        assertEquals(basilNightDarkColorScheme(), scheme)
    }

    @Test
    fun `light scheme is not dark`() {
        assertFalse(basilSchemeForHour(12, isDark = false).isDark)
    }

    // ── AC 4: boundary hours are correct ─────────────────────

    @Test
    fun `next boundary after morning is 10`() {
        assertEquals(10, nextSchemeTransitionHour(7))
    }

    @Test
    fun `next boundary after day is 18`() {
        assertEquals(18, nextSchemeTransitionHour(14))
    }

    @Test
    fun `next boundary after evening is 21`() {
        assertEquals(21, nextSchemeTransitionHour(19))
    }

    @Test
    fun `next boundary after night at 23h is 5`() {
        assertEquals(5, nextSchemeTransitionHour(23))
    }

    @Test
    fun `next boundary at midnight is 5`() {
        assertEquals(5, nextSchemeTransitionHour(0))
    }

    @Test
    fun `next boundary at 4am is 5`() {
        assertEquals(5, nextSchemeTransitionHour(4))
    }

    // ── AC 3: msUntilSchemeWake is exactly boundary - 5s ─────

    @Test
    fun `wake for morning slot is 5s before 10h boundary`() {
        // At 07:00:00 — wake at 09:59:55 → (9*3600+59*60+55 - 7*3600) * 1000 = 10795000ms
        assertEquals(10_795_000L, msUntilSchemeWake(7, 0, 0))
    }

    @Test
    fun `wake for day slot is 5s before 18h boundary`() {
        // At 12:00:00 — wake at 17:59:55 → (17*3600+59*60+55 - 12*3600) * 1000 = 21595000ms
        assertEquals(21_595_000L, msUntilSchemeWake(12, 0, 0))
    }

    @Test
    fun `wake for evening slot is 5s before 21h boundary`() {
        // At 19:00:00 — wake at 20:59:55 → (20*3600+59*60+55 - 19*3600) * 1000 = 7195000ms
        assertEquals(7_195_000L, msUntilSchemeWake(19, 0, 0))
    }

    @Test
    fun `wake for night slot crosses midnight to 5am`() {
        // At 22:00:00 — wake at 04:59:55 next day
        // = (86400 + 4*3600+59*60+55 - 22*3600) * 1000 = 25195000ms
        assertEquals(25_195_000L, msUntilSchemeWake(22, 0, 0))
    }

    @Test
    fun `wake returns 0 when exactly at boundary minus 5s`() {
        // Exactly at 09:59:55 — the wake point for the 10h boundary
        assertEquals(0L, msUntilSchemeWake(9, 59, 55))
    }

    @Test
    fun `wake returns negative when inside the 5s transition window`() {
        // At 09:59:57 — 2s after the wake point (09:59:55), transition already started.
        // rawDiff = (10*3600 - 5) - (9*3600+59*60+57) = 35995 - 35997 = -2
        // Negative means: fire immediately, don't delay.
        assertTrue(msUntilSchemeWake(9, 59, 57) < 0, "Expected negative ms inside transition window")
    }

    @Test
    fun `wake value 5s before boundary is exactly 0`() {
        // Spot-check each boundary - 5s
        assertEquals(0L, msUntilSchemeWake(9, 59, 55))   // morning → day
        assertEquals(0L, msUntilSchemeWake(17, 59, 55))  // day → evening
        assertEquals(0L, msUntilSchemeWake(20, 59, 55))  // evening → night
        assertEquals(0L, msUntilSchemeWake(4, 59, 55))   // night → morning
    }

    // ── AC 5: ViewModel initialises to the correct hour ───────
    //
    // These tests do NOT use runTest. The StateFlow initial value is set
    // synchronously in the ViewModel constructor. Using runTest would trigger
    // advanceUntilIdle() which spins the infinite boundary loop forever.
    // With UnconfinedTestDispatcher (set in @BeforeTest), viewModelScope.launch
    // runs eagerly but suspends at the first delay() — before _hour is changed —
    // so the initial value is always startTime.hour for these test cases.

    @Test
    fun `ViewModel init hour reflects morning slot`() {
        val vm = BasilThemeViewModel(startTime = LocalDateTime(2026, 7, 21, 7, 0, 0, 0))
        assertEquals(7, vm.hour.value)
    }

    @Test
    fun `ViewModel init hour reflects day slot`() {
        val vm = BasilThemeViewModel(startTime = LocalDateTime(2026, 7, 21, 12, 0, 0, 0))
        assertEquals(12, vm.hour.value)
    }

    @Test
    fun `ViewModel init hour reflects evening slot`() {
        val vm = BasilThemeViewModel(startTime = LocalDateTime(2026, 7, 21, 19, 0, 0, 0))
        assertEquals(19, vm.hour.value)
    }

    @Test
    fun `ViewModel init hour reflects night slot`() {
        val vm = BasilThemeViewModel(startTime = LocalDateTime(2026, 7, 21, 23, 0, 0, 0))
        assertEquals(23, vm.hour.value)
    }

    @Test
    fun `ViewModel init hour reflects start of morning slot at 5am`() {
        val vm = BasilThemeViewModel(startTime = LocalDateTime(2026, 7, 21, 5, 0, 0, 0))
        assertEquals(5, vm.hour.value)
    }
}
