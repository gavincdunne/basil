package org.weekendware.basil.presentation.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Returns the boundary hour (0–23) that follows the time slot containing [hour].
 *
 * Boundaries: morning→day at 10, day→evening at 18, evening→night at 21, night→morning at 5.
 */
internal fun nextSchemeTransitionHour(hour: Int): Int = when (hour) {
    in 5..9   -> 10
    in 10..17 -> 18
    in 18..20 -> 21
    else      -> 5
}

/**
 * Milliseconds from the given local time (h:m:s) until exactly 5 seconds before the next
 * scheme boundary. Returns a negative value if the wake point has already passed.
 *
 * The coroutine uses this to sleep until it is time to begin emitting the new scheme.
 * A negative return means the wake point is in the past — transition should fire immediately.
 */
internal fun msUntilSchemeWake(hour: Int, minute: Int, second: Int): Long {
    val currentSec = hour * 3600L + minute * 60L + second
    val nextBoundary = nextSchemeTransitionHour(hour)
    val boundarySec = nextBoundary * 3600L
    val rawDiff = if (boundarySec > currentSec) {
        boundarySec - 5 - currentSec        // same day
    } else {
        24 * 3600L + boundarySec - 5 - currentSec  // crosses midnight (night → morning)
    }
    return rawDiff * 1000L
}

/**
 * Drives the reactive time-of-day colour scheme for the app.
 *
 * Exposes [hour] as a [StateFlow<Int>]. [BasilTheme] collects this and derives
 * [basilSchemeForHour] from it, animating each colour token over 10 seconds.
 *
 * ### Boundary coroutine
 * On init the coroutine sleeps until exactly 5 seconds before the next slot boundary,
 * then emits the new boundary hour. After the 10-second transition window, it reads
 * the real clock and repeats for the next boundary. The coroutine fires at most 4 times
 * per day and continues running while the app is backgrounded (viewModelScope survives
 * background). On foreground the StateFlow already holds the correct hour — no flash.
 *
 * ### Resume behaviour
 * If the app was backgrounded mid-transition and the process survived, the coroutine
 * already updated [hour] before the user returned — first composition sees the correct
 * scheme. If the process was killed, [startTime] defaults to the real current clock so
 * [hour] is initialised to the correct current hour on cold re-create.
 *
 * @param startTime Injection point for tests. Defaults to the real local clock.
 */
class BasilThemeViewModel(
    startTime: LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
) : ViewModel() {

    private val _hour = MutableStateFlow(startTime.hour)
    val hour: StateFlow<Int> = _hour.asStateFlow()

    init {
        viewModelScope.launch {
            var h = startTime.hour
            var m = startTime.minute
            var s = startTime.second
            while (true) {
                val msToWake = msUntilSchemeWake(h, m, s)
                if (msToWake > 0) delay(msToWake)
                // Emit the first hour of the next slot so basilSchemeForHour picks the right scheme.
                _hour.value = nextSchemeTransitionHour(_hour.value)
                // Sleep past the 10s transition window before re-evaluating, with a small buffer.
                delay(15_000L)
                // Re-read the real clock so the next iteration targets the correct next boundary.
                val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                h = now.hour; m = now.minute; s = now.second
            }
        }
    }
}
