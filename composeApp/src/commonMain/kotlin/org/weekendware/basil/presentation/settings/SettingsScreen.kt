package org.weekendware.basil.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import basil.composeapp.generated.resources.Res
import basil.composeapp.generated.resources.settings_label_bg_unit
import basil.composeapp.generated.resources.settings_label_reminders
import basil.composeapp.generated.resources.settings_label_reminders_hint
import basil.composeapp.generated.resources.settings_label_version
import basil.composeapp.generated.resources.settings_notifications_coming_soon
import basil.composeapp.generated.resources.settings_section_about
import basil.composeapp.generated.resources.settings_section_data
import basil.composeapp.generated.resources.settings_section_notifications
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import org.weekendware.basil.domain.model.BgUnit
import org.weekendware.basil.presentation.theme.BasilTheme
import org.weekendware.basil.presentation.theme.basilSpacing

/**
 * Settings screen — BG unit selection, notifications placeholder, and app info.
 */
@Composable
fun SettingsScreen() {
    val viewModel = koinViewModel<SettingsViewModel>()
    val state by viewModel.state.collectAsState()
    SettingsScreenContent(
        state          = state,
        onBgUnitChange = viewModel::onBgUnitChange
    )
}

@Composable
fun SettingsScreenContent(
    state: SettingsState,
    onBgUnitChange: (BgUnit) -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = MaterialTheme.basilSpacing

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = spacing.lg, vertical = spacing.xl),
        verticalArrangement = Arrangement.spacedBy(spacing.xl)
    ) {
        // ── Data ──────────────────────────────────────────────
        SettingsSection(title = stringResource(Res.string.settings_section_data)) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    text  = stringResource(Res.string.settings_label_bg_unit),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                BgUnitToggle(
                    selected       = state.bgUnit,
                    onUnitSelected = onBgUnitChange
                )
            }

            state.error?.let { err ->
                Text(
                    text  = stringResource(err),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        // ── Notifications ─────────────────────────────────────
        SettingsSection(title = stringResource(Res.string.settings_section_notifications)) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text  = stringResource(Res.string.settings_label_reminders),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text  = stringResource(Res.string.settings_label_reminders_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked         = false,
                    onCheckedChange = null,
                    enabled         = false
                )
            }
            Text(
                text  = stringResource(Res.string.settings_notifications_coming_soon),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // ── About ─────────────────────────────────────────────
        SettingsSection(title = stringResource(Res.string.settings_section_about)) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    text  = stringResource(Res.string.settings_label_version),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text  = "1.0.0",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Sub-composables
// ─────────────────────────────────────────────────────────────

@Composable
private fun SettingsSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val spacing = MaterialTheme.basilSpacing
    ElevatedCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier            = Modifier.padding(spacing.xl),
            verticalArrangement = Arrangement.spacedBy(spacing.md)
        ) {
            Text(
                text  = title.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
            content()
        }
    }
}

@Composable
private fun BgUnitToggle(
    selected: BgUnit,
    onUnitSelected: (BgUnit) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(MaterialTheme.basilSpacing.xs)) {
        BgUnit.entries.forEach { unit ->
            if (unit == selected) {
                Button(
                    onClick  = {},
                    enabled  = false,
                    colors   = ButtonDefaults.buttonColors(
                        disabledContainerColor = MaterialTheme.colorScheme.primary,
                        disabledContentColor   = MaterialTheme.colorScheme.onPrimary
                    )
                ) { Text(unit.label) }
            } else {
                OutlinedButton(onClick = { onUnitSelected(unit) }) {
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
internal fun SettingsScreenMgdlPreview() {
    BasilTheme {
        SettingsScreenContent(
            state          = SettingsState(bgUnit = BgUnit.MGDL),
            onBgUnitChange = {}
        )
    }
}

@Preview
@Composable
internal fun SettingsScreenMmollPreview() {
    BasilTheme {
        SettingsScreenContent(
            state          = SettingsState(bgUnit = BgUnit.MMOLL),
            onBgUnitChange = {}
        )
    }
}
