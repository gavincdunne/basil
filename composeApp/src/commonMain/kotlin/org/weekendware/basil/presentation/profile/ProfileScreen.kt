package org.weekendware.basil.presentation.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import basil.composeapp.generated.resources.Res
import basil.composeapp.generated.resources.profile_action_cancel
import basil.composeapp.generated.resources.profile_action_edit
import basil.composeapp.generated.resources.profile_action_save
import basil.composeapp.generated.resources.profile_label_email
import basil.composeapp.generated.resources.profile_label_name
import basil.composeapp.generated.resources.profile_label_target_high
import basil.composeapp.generated.resources.profile_label_target_low
import basil.composeapp.generated.resources.profile_label_target_range
import basil.composeapp.generated.resources.profile_placeholder_name
import basil.composeapp.generated.resources.profile_placeholder_target
import basil.composeapp.generated.resources.profile_section_account
import basil.composeapp.generated.resources.profile_section_health
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import org.weekendware.basil.presentation.theme.BasilTheme
import org.weekendware.basil.presentation.theme.basilSpacing

/**
 * Profile screen — shows account info and the user's target BG range.
 * Entering edit mode allows updating targets; name is shown read-only
 * until full user-update support is wired to Supabase.
 */
@Composable
fun ProfileScreen() {
    val viewModel = koinViewModel<ProfileViewModel>()
    val state by viewModel.state.collectAsState()
    ProfileScreenContent(
        state           = state,
        onEditClick     = viewModel::onEditClick,
        onCancelClick   = viewModel::onCancelClick,
        onNameChange    = viewModel::onNameChange,
        onTargetLowChange  = viewModel::onTargetLowChange,
        onTargetHighChange = viewModel::onTargetHighChange,
        onSaveClick     = viewModel::onSaveClick
    )
}

@Composable
fun ProfileScreenContent(
    state: ProfileState,
    onEditClick: () -> Unit,
    onCancelClick: () -> Unit,
    onNameChange: (String) -> Unit,
    onTargetLowChange: (String) -> Unit,
    onTargetHighChange: (String) -> Unit,
    onSaveClick: () -> Unit,
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
        // ── Avatar + name header ──────────────────────────────
        ProfileHeader(name = state.name, email = state.email)

        // ── Account section ───────────────────────────────────
        ProfileSection(title = stringResource(Res.string.profile_section_account)) {
            ProfileReadOnlyRow(
                label = stringResource(Res.string.profile_label_name),
                value = state.name.ifBlank { stringResource(Res.string.profile_placeholder_name) }
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            ProfileReadOnlyRow(
                label = stringResource(Res.string.profile_label_email),
                value = state.email
            )
        }

        // ── Health profile section ────────────────────────────
        ProfileSection(title = stringResource(Res.string.profile_section_health)) {
            Text(
                text  = stringResource(Res.string.profile_label_target_range),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(spacing.sm))

            if (state.isEditing) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(spacing.md),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value         = state.targetBgLow,
                        onValueChange = onTargetLowChange,
                        label         = { Text(stringResource(Res.string.profile_label_target_low)) },
                        placeholder   = { Text(stringResource(Res.string.profile_placeholder_target)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine    = true,
                        modifier      = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value         = state.targetBgHigh,
                        onValueChange = onTargetHighChange,
                        label         = { Text(stringResource(Res.string.profile_label_target_high)) },
                        placeholder   = { Text(stringResource(Res.string.profile_placeholder_target)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine    = true,
                        modifier      = Modifier.weight(1f)
                    )
                }
            } else {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(spacing.xs),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text  = state.targetBgLow,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text  = "–",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text  = state.targetBgHigh,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Error
            state.error?.let { err ->
                Spacer(Modifier.height(spacing.xs))
                Text(
                    text  = stringResource(err),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        // ── Actions ───────────────────────────────────────────
        if (state.isEditing) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(spacing.md),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick  = onCancelClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(Res.string.profile_action_cancel).uppercase())
                }
                Button(
                    onClick  = onSaveClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(Res.string.profile_action_save).uppercase())
                }
            }
        } else {
            TextButton(
                onClick  = onEditClick,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(stringResource(Res.string.profile_action_edit).uppercase())
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Sub-composables
// ─────────────────────────────────────────────────────────────

@Composable
private fun ProfileHeader(name: String, email: String) {
    Column(
        modifier            = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.basilSpacing.sm)
    ) {
        // Initials avatar
        Surface(
            modifier  = Modifier.size(72.dp).clip(CircleShape),
            color     = MaterialTheme.colorScheme.primaryContainer
        ) {
            Text(
                text      = name.initials(),
                modifier  = Modifier.fillMaxSize().padding(top = 18.dp),
                textAlign = TextAlign.Center,
                style     = MaterialTheme.typography.headlineMedium,
                color     = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        if (name.isNotBlank()) {
            Text(
                text  = name,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        if (email.isNotBlank()) {
            Text(
                text  = email,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ProfileSection(
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
private fun ProfileReadOnlyRow(label: String, value: String) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(
            text  = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text  = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun String.initials(): String =
    trim().split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercaseChar().toString() }
        .ifBlank { "?" }

// ─────────────────────────────────────────────────────────────
// Previews
// ─────────────────────────────────────────────────────────────

@Preview
@Composable
internal fun ProfileScreenContentPreview() {
    BasilTheme {
        ProfileScreenContent(
            state = ProfileState(
                name         = "Gavin Dunne",
                email        = "gavin@weekendware.io",
                targetBgLow  = "3.9",
                targetBgHigh = "10.0",
                isEditing    = false
            ),
            onEditClick        = {},
            onCancelClick      = {},
            onNameChange       = {},
            onTargetLowChange  = {},
            onTargetHighChange = {},
            onSaveClick        = {}
        )
    }
}

@Preview
@Composable
internal fun ProfileScreenEditingPreview() {
    BasilTheme {
        ProfileScreenContent(
            state = ProfileState(
                name         = "Gavin Dunne",
                email        = "gavin@weekendware.io",
                targetBgLow  = "4.0",
                targetBgHigh = "9.0",
                isEditing    = true
            ),
            onEditClick        = {},
            onCancelClick      = {},
            onNameChange       = {},
            onTargetLowChange  = {},
            onTargetHighChange = {},
            onSaveClick        = {}
        )
    }
}
