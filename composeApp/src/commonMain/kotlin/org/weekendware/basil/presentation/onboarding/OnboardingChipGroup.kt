package org.weekendware.basil.presentation.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.weekendware.basil.presentation.theme.BasilTheme
import org.weekendware.basil.presentation.theme.basilSpacing

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun <T> OnboardingChipGroup(
    modifier: Modifier = Modifier,
    options: ImmutableList<Pair<String, T>>,
    onSelect: (T) -> Unit,
    isEnabled: Boolean = true,
) {
    val chipShape = RoundedCornerShape(50)
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.basilSpacing.sm),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.basilSpacing.sm)
    ) {
        options.forEach { (label, value) ->
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = if (isEnabled) {
                    MaterialTheme.colorScheme.onSecondaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                },
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .widthIn(min = 56.dp)
                    .clip(chipShape)
                    .background(
                        if (isEnabled) {
                            MaterialTheme.colorScheme.secondaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                        }
                    )
                    .clickable(enabled = isEnabled) { onSelect(value) }
                    .padding(
                        horizontal = MaterialTheme.basilSpacing.lg,
                        vertical = MaterialTheme.basilSpacing.sm
                    )
            )
        }
    }
}

@Preview
@Composable
private fun PreviewOnboardingChipGroup() {
    BasilTheme {
        OnboardingChipGroup(
            options = persistentListOf(
                "Injections" to "INJECTIONS",
                "Pump" to "PUMP",
                "Closed loop" to "CLOSED_LOOP"
            ),
            onSelect = {}
        )
    }
}
