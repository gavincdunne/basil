package org.weekendware.basil.presentation.logging

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import org.weekendware.basil.domain.model.BgUnit
import org.weekendware.basil.presentation.theme.BasilTokens
import org.weekendware.basil.presentation.theme.basilSpacing

/**
 * A [ModalBottomSheet] for recording a new log entry.
 *
 * Presents three optional fields — blood glucose, insulin units, and
 * carbohydrates — any combination of which can be filled in per entry.
 * The Save button is enabled as soon as at least one field has a value.
 *
 * ### BG unit toggle
 * The blood glucose row includes a pair of buttons to switch between
 * mg/dL and mmol/L. The selected unit is visually filled; the inactive unit
 * is outlined. Tapping a unit persists the preference via [LoggingViewModel]
 * so the same unit is pre-selected the next time the sheet opens.
 *
 * @param viewModel The [LoggingViewModel] managing form state and save logic.
 * @param onDismiss Called when the user dismisses the sheet or after a successful save.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogEntrySheet(
    viewModel: LoggingViewModel,
    onDismiss: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val spacing = MaterialTheme.basilSpacing

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .padding(horizontal = BasilTokens.SheetHorizontalPadding)
                .padding(bottom = BasilTokens.SheetBottomPadding),
            verticalArrangement = Arrangement.spacedBy(BasilTokens.FormFieldGap)
        ) {
            Text(
                text  = "Log Entry",
                style = MaterialTheme.typography.titleLarge
            )

            // ── Blood Glucose ────────────────────────────────────
            LogFieldSection(label = "Blood Glucose") {
                Row(horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
                    OutlinedTextField(
                        value         = state.bgValue,
                        onValueChange = viewModel::onBgValueChange,
                        modifier      = Modifier.weight(1f),
                        placeholder   = { Text("0") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine    = true
                    )
                    BgUnitToggle(
                        selected  = state.bgUnit,
                        onSelect  = viewModel::onBgUnitChange
                    )
                }
            }

            // ── Insulin ──────────────────────────────────────────
            LogFieldSection(label = "Insulin") {
                OutlinedTextField(
                    value         = state.insulinUnits,
                    onValueChange = viewModel::onInsulinChange,
                    modifier      = Modifier.fillMaxWidth(),
                    placeholder   = { Text("Units") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine    = true
                )
            }

            // ── Carbohydrates ────────────────────────────────────
            LogFieldSection(label = "Carbs") {
                OutlinedTextField(
                    value         = state.carbsGrams,
                    onValueChange = viewModel::onCarbsChange,
                    modifier      = Modifier.fillMaxWidth(),
                    placeholder   = { Text("Grams") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine    = true
                )
            }

            // ── Save ─────────────────────────────────────────────
            Button(
                onClick  = { viewModel.save(onDismiss) },
                modifier = Modifier.fillMaxWidth(),
                enabled  = state.hasAnyValue
            ) {
                Text("Save")
            }

            Spacer(modifier = Modifier.height(spacing.sm))
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Private sub-composables
// ─────────────────────────────────────────────────────────────

/**
 * A labeled wrapper for a single log input section.
 *
 * Renders [label] in [MaterialTheme.typography.labelLarge] above the
 * provided [content] composable, with consistent vertical spacing.
 *
 * @param label   The section label shown above the input field(s).
 * @param content The input field(s) for this section.
 */
@Composable
private fun LogFieldSection(
    label:   String,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.basilSpacing.xs)) {
        Text(
            text  = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        content()
    }
}

/**
 * A two-button toggle for selecting between [BgUnit.MGDL] and [BgUnit.MMOLL].
 *
 * The currently-selected unit uses a filled [Button]; the inactive unit
 * uses an [OutlinedButton], making the selection immediately obvious without
 * requiring an experimental Compose API.
 *
 * @param selected The currently-active [BgUnit].
 * @param onSelect Callback invoked when the user taps a unit button.
 */
@Composable
private fun BgUnitToggle(
    selected: BgUnit,
    onSelect: (BgUnit) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.basilSpacing.xs)) {
        BgUnit.entries.forEach { unit ->
            if (unit == selected) {
                Button(onClick = {}) {
                    Text(unit.label)
                }
            } else {
                OutlinedButton(onClick = { onSelect(unit) }) {
                    Text(unit.label)
                }
            }
        }
    }
}
