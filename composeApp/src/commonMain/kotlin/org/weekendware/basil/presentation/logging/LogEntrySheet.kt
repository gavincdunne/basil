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
import androidx.compose.material3.ButtonDefaults
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
import basil.composeapp.generated.resources.Res
import basil.composeapp.generated.resources.log_entry_title
import basil.composeapp.generated.resources.log_field_blood_glucose
import basil.composeapp.generated.resources.log_field_carbs
import basil.composeapp.generated.resources.log_field_insulin
import basil.composeapp.generated.resources.log_placeholder_bg
import basil.composeapp.generated.resources.log_placeholder_grams
import basil.composeapp.generated.resources.log_placeholder_units
import basil.composeapp.generated.resources.log_save
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.weekendware.basil.domain.model.BgUnit
import org.weekendware.basil.presentation.theme.BasilTheme
import org.weekendware.basil.presentation.theme.BasilTokens
import org.weekendware.basil.presentation.theme.basilSpacing

/**
 * A [ModalBottomSheet] for recording a new log entry.
 *
 * Wires [LoggingViewModel] to [LogEntrySheetContent].
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
    ModalBottomSheet(onDismissRequest = onDismiss) {
        LogEntrySheetContent(
            state           = state,
            onBgValueChange = viewModel::onBgValueChange,
            onBgUnitChange  = viewModel::onBgUnitChange,
            onInsulinChange = viewModel::onInsulinChange,
            onCarbsChange   = viewModel::onCarbsChange,
            onSave          = { viewModel.save(onDismiss) }
        )
    }
}

/**
 * Stateless log-entry form.
 *
 * Presents three optional fields — blood glucose, insulin units, and
 * carbohydrates — any combination of which can be filled in per entry.
 * The Save button is enabled as soon as at least one field has a value.
 *
 * ### BG unit toggle
 * The blood glucose row includes a pair of buttons to switch between
 * mg/dL and mmol/L. The selected unit is visually filled; the inactive unit
 * is outlined. Tapping a unit persists the preference so the same unit is
 * pre-selected the next time the sheet opens.
 */
@Composable
fun LogEntrySheetContent(
    state:           LogFormState,
    onBgValueChange: (String) -> Unit,
    onBgUnitChange:  (BgUnit) -> Unit,
    onInsulinChange: (String) -> Unit,
    onCarbsChange:   (String) -> Unit,
    onSave:          () -> Unit
) {
    val spacing = MaterialTheme.basilSpacing

    Column(
        modifier = Modifier
            .padding(horizontal = BasilTokens.SheetHorizontalPadding)
            .padding(bottom = BasilTokens.SheetBottomPadding),
        verticalArrangement = Arrangement.spacedBy(BasilTokens.FormFieldGap)
    ) {
        Text(
            text  = stringResource(Res.string.log_entry_title),
            style = MaterialTheme.typography.titleLarge
        )

        // ── Blood Glucose ────────────────────────────────────
        LogFieldSection(label = stringResource(Res.string.log_field_blood_glucose)) {
            Row(horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
                OutlinedTextField(
                    value           = state.bgValue,
                    onValueChange   = onBgValueChange,
                    modifier        = Modifier.weight(1f),
                    placeholder     = { Text(stringResource(Res.string.log_placeholder_bg)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine      = true
                )
                BgUnitToggle(
                    selected = state.bgUnit,
                    onSelect = onBgUnitChange
                )
            }
        }

        // ── Insulin ──────────────────────────────────────────
        LogFieldSection(label = stringResource(Res.string.log_field_insulin)) {
            OutlinedTextField(
                value           = state.insulinUnits,
                onValueChange   = onInsulinChange,
                modifier        = Modifier.fillMaxWidth(),
                placeholder     = { Text(stringResource(Res.string.log_placeholder_units)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine      = true
            )
        }

        // ── Carbohydrates ────────────────────────────────────
        LogFieldSection(label = stringResource(Res.string.log_field_carbs)) {
            OutlinedTextField(
                value           = state.carbsGrams,
                onValueChange   = onCarbsChange,
                modifier        = Modifier.fillMaxWidth(),
                placeholder     = { Text(stringResource(Res.string.log_placeholder_grams)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine      = true
            )
        }

        // ── Save ─────────────────────────────────────────────
        Button(
            onClick  = onSave,
            modifier = Modifier.fillMaxWidth(),
            enabled  = state.hasAnyValue
        ) {
            Text(stringResource(Res.string.log_save))
        }

        Spacer(modifier = Modifier.height(spacing.sm))
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
                Button(
                    onClick = {},
                    enabled = false,
                    colors = ButtonDefaults.buttonColors(
                        disabledContainerColor = MaterialTheme.colorScheme.primary,
                        disabledContentColor   = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
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

// ─────────────────────────────────────────────────────────────
// Previews
// ─────────────────────────────────────────────────────────────

@Preview
@Composable
internal fun LogEntrySheetContentPreview() {
    BasilTheme {
        LogEntrySheetContent(
            state           = LogFormState(),
            onBgValueChange = {},
            onBgUnitChange  = {},
            onInsulinChange = {},
            onCarbsChange   = {},
            onSave          = {}
        )
    }
}

@Preview
@Composable
internal fun LogEntrySheetContentFilledPreview() {
    BasilTheme {
        LogEntrySheetContent(
            state           = LogFormState(bgValue = "6.2", insulinUnits = "4", carbsGrams = "45"),
            onBgValueChange = {},
            onBgUnitChange  = {},
            onInsulinChange = {},
            onCarbsChange   = {},
            onSave          = {}
        )
    }
}
