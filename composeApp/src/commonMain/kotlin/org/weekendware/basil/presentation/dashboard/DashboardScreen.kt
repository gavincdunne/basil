package org.weekendware.basil.presentation.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import org.weekendware.basil.domain.model.LogEntry
import org.weekendware.basil.presentation.logging.LogEntrySheet
import org.weekendware.basil.presentation.logging.LoggingViewModel
import org.weekendware.basil.presentation.theme.BasilTokens
import org.weekendware.basil.presentation.theme.basilColors
import org.weekendware.basil.presentation.theme.basilSpacing

/**
 * The main Dashboard screen, displayed on the home tab.
 *
 * Shows:
 * - A summary card with the most recent blood glucose reading and its status.
 * - A chronological list of today's log entries.
 * - A FAB to open the [LogEntrySheet] for new entries.
 *
 * The list refreshes automatically after a new entry is saved.
 */
@Composable
fun DashboardScreen() {
        val viewModel        = koinInject<DashboardViewModel>()
        val loggingViewModel = koinInject<LoggingViewModel>()
        val uiState  by viewModel.state.collectAsState()
        val showSheet by viewModel.showLogSheet.collectAsState()
        val spacing = MaterialTheme.basilSpacing

        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start  = spacing.lg,
                    end    = spacing.lg,
                    top    = spacing.lg,
                    bottom = BasilTokens.FabSize + BasilTokens.FabEdgePadding * 2
                ),
                verticalArrangement = Arrangement.spacedBy(spacing.sm)
            ) {
                // ── Last reading summary ──────────────────────
                item {
                    LastReadingCard(entry = uiState.lastBgEntry)
                }

                // ── Today section header ──────────────────────
                item {
                    TodayHeader(
                        modifier = Modifier.padding(top = spacing.md, bottom = spacing.xs)
                    )
                }

                // ── Today's entries or empty state ────────────
                if (uiState.todayEntries.isEmpty()) {
                    item { EmptyTodayState() }
                } else {
                    items(uiState.todayEntries, key = { it.id }) { entry ->
                        LogEntryItem(entry = entry)
                    }
                }
            }

            // ── FAB ───────────────────────────────────────────
            FloatingActionButton(
                onClick = {
                    loggingViewModel.reset()
                    viewModel.openLogSheet()
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(BasilTokens.FabEdgePadding)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Log Entry")
            }
        }

        if (showSheet) {
            LogEntrySheet(
                viewModel = loggingViewModel,
                onDismiss = { viewModel.closeLogSheet() }
            )
        }
}

// ─────────────────────────────────────────────────────────────
// Sub-composables
// ─────────────────────────────────────────────────────────────

/**
 * Hero card showing the most recent blood glucose reading.
 *
 * When no BG data exists yet, shows a prompt to log the first reading.
 * The reading value and status label are coloured using the glucose
 * semantic colours from [BasilColorScheme].
 *
 * @param entry The most recent [LogEntry] that contains a BG value, or null.
 */
@Composable
private fun LastReadingCard(entry: LogEntry?, modifier: Modifier = Modifier) {
    ElevatedCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(
                horizontal = MaterialTheme.basilSpacing.xl,
                vertical   = MaterialTheme.basilSpacing.lg
            )
        ) {
            if (entry?.bgValue != null && entry.bgUnit != null) {
                val status = glucoseStatus(entry.bgValue, entry.bgUnit)
                val color  = status.color()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text  = "Last Reading",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text  = formatRelativeTime(entry.timestamp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(Modifier.height(MaterialTheme.basilSpacing.sm))

                Text(
                    text  = entry.bgValue.toFormattedValue(),
                    style = MaterialTheme.typography.displayMedium,
                    color = color
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text  = entry.bgUnit.label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text  = "·",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                    Text(
                        text  = status.label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = color
                    )
                }
            } else {
                Text(
                    text  = "Last Reading",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(MaterialTheme.basilSpacing.sm))
                Text(
                    text  = "No readings yet",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text  = "Tap + to log your first reading",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Section header dividing the summary card from today's entry list.
 */
@Composable
private fun TodayHeader(modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text  = "Today",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        HorizontalDivider(
            modifier = Modifier.padding(top = MaterialTheme.basilSpacing.xs),
            color    = MaterialTheme.colorScheme.outlineVariant
        )
    }
}

/**
 * A single log entry row in today's timeline.
 *
 * Shows the entry time and a horizontal summary of all recorded values.
 * BG values are coloured by their glucose status range.
 *
 * @param entry The [LogEntry] to display.
 */
@Composable
private fun LogEntryItem(entry: LogEntry, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(
                horizontal = MaterialTheme.basilSpacing.lg,
                vertical   = MaterialTheme.basilSpacing.md
            ),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.basilSpacing.xs)
        ) {
            Text(
                text  = formatTime(entry.timestamp),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.basilSpacing.lg),
                verticalAlignment = Alignment.CenterVertically
            ) {
                entry.bgValue?.let { bg ->
                    entry.bgUnit?.let { unit ->
                        val color = glucoseStatus(bg, unit).color()
                        Text(
                            text  = "${bg.toFormattedValue()} ${unit.label}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = color
                        )
                    }
                }
                entry.insulinUnits?.let { insulin ->
                    Text(
                        text  = "${insulin.toFormattedValue()}u",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                entry.carbsGrams?.let { carbs ->
                    Text(
                        text  = "${carbs.toFormattedValue()}g",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

/**
 * Shown in place of the entry list when no entries have been logged today.
 */
@Composable
private fun EmptyTodayState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = MaterialTheme.basilSpacing.xxxl),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.basilSpacing.xs)
        ) {
            Text(
                text  = "Nothing logged today",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text  = "Tap + to add your first entry",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Glucose status colour — Compose-only extension on the shared enum
// ─────────────────────────────────────────────────────────────

/** Returns the semantic color for this status from the current [BasilColorScheme]. */
@Composable
private fun GlucoseStatus.color(): Color = when (this) {
    GlucoseStatus.VERY_LOW  -> MaterialTheme.basilColors.glucoseVeryLow
    GlucoseStatus.LOW       -> MaterialTheme.basilColors.glucoseLow
    GlucoseStatus.IN_RANGE  -> MaterialTheme.basilColors.glucoseInRange
    GlucoseStatus.HIGH      -> MaterialTheme.basilColors.glucoseHigh
    GlucoseStatus.VERY_HIGH -> MaterialTheme.basilColors.glucoseVeryHigh
}
