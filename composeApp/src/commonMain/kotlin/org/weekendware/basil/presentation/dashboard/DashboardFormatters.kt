package org.weekendware.basil.presentation.dashboard

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.weekendware.basil.domain.model.BgUnit

/**
 * Blood glucose status ranges, based on standard clinical thresholds in mg/dL.
 *
 * @property label Human-readable status label shown in the UI.
 */
internal enum class GlucoseStatus(val label: String) {
    VERY_LOW("Very Low"),
    LOW("Low"),
    IN_RANGE("In Range"),
    HIGH("High"),
    VERY_HIGH("Very High")
}

/**
 * Classifies a blood glucose reading into a [GlucoseStatus] range.
 *
 * mmol/L values are converted to mg/dL before classification using the
 * standard factor of 18.016.
 *
 * Thresholds (mg/dL): < 54 very low · 54–69 low · 70–180 in range · 181–250 high · > 250 very high.
 *
 * @param value The glucose reading.
 * @param unit  The unit of [value].
 */
internal fun glucoseStatus(value: Double, unit: BgUnit): GlucoseStatus {
    val mgdl = if (unit == BgUnit.MGDL) value else value * 18.016
    return when {
        mgdl < 54   -> GlucoseStatus.VERY_LOW
        mgdl < 70   -> GlucoseStatus.LOW
        mgdl <= 180 -> GlucoseStatus.IN_RANGE
        mgdl <= 250 -> GlucoseStatus.HIGH
        else        -> GlucoseStatus.VERY_HIGH
    }
}

/**
 * Formats this [Double] for display as a health value.
 *
 * Whole numbers are shown without a decimal point; fractional values are
 * shown with one decimal place. Implemented without platform-specific
 * format APIs for KMP compatibility.
 */
internal fun Double.toFormattedValue(): String {
    if (this - toLong() == 0.0) return toLong().toString()
    val tenths = ((this * 10).toLong() % 10).let { if (it < 0) -it else it }
    return "${toLong()}.$tenths"
}

/**
 * Formats a Unix epoch millisecond timestamp as a 12-hour clock time string,
 * e.g. `"2:30 PM"`.
 */
internal fun formatTime(epochMillis: Long): String {
    val local = Instant.fromEpochMilliseconds(epochMillis)
        .toLocalDateTime(TimeZone.currentSystemDefault())
    val hour   = local.hour
    val minute = local.minute.toString().padStart(2, '0')
    val amPm   = if (hour < 12) "AM" else "PM"
    val h      = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else      -> hour
    }
    return "$h:$minute $amPm"
}

/**
 * Returns a human-readable relative time string for a past timestamp,
 * e.g. `"just now"`, `"5m ago"`, `"2h ago"`, `"3d ago"`.
 */
internal fun formatRelativeTime(epochMillis: Long): String {
    val diffMs  = Clock.System.now().toEpochMilliseconds() - epochMillis
    val minutes = diffMs / 60_000
    return when {
        minutes < 1    -> "just now"
        minutes < 60   -> "${minutes}m ago"
        minutes < 1440 -> "${minutes / 60}h ago"
        else           -> "${minutes / 1440}d ago"
    }
}
