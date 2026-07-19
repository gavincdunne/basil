package org.weekendware.basil.presentation.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.weekendware.basil.presentation.theme.BasilTheme
import org.weekendware.basil.presentation.theme.basilSpacing

@Composable
fun OnboardingChatBubble(
    modifier: Modifier = Modifier,
    text: String,
    subtext: String? = null,
) {
    val shape = RoundedCornerShape(
        topStart = 4.dp,
        topEnd = 16.dp,
        bottomEnd = 16.dp,
        bottomStart = 16.dp
    )
    Column(
        modifier = modifier
            .widthIn(max = 280.dp)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(
                horizontal = MaterialTheme.basilSpacing.md,
                vertical = MaterialTheme.basilSpacing.sm
            )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (subtext != null) {
            Text(
                text = subtext,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = MaterialTheme.basilSpacing.xs)
            )
        }
    }
}

@Composable
fun OnboardingUserBubble(
    modifier: Modifier = Modifier,
    text: String,
) {
    val shape = RoundedCornerShape(
        topStart = 16.dp,
        topEnd = 4.dp,
        bottomEnd = 16.dp,
        bottomStart = 16.dp
    )
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onPrimaryContainer,
        modifier = modifier
            .widthIn(max = 280.dp)
            .clip(shape)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(
                horizontal = MaterialTheme.basilSpacing.md,
                vertical = MaterialTheme.basilSpacing.sm
            )
    )
}

@Preview
@Composable
private fun PreviewOnboardingChatBubble() {
    BasilTheme {
        OnboardingChatBubble(
            text = "Hey. I'm Basil. I'm here to talk, and I've got time. What should I call you?",
        )
    }
}

@Preview
@Composable
private fun PreviewOnboardingChatBubbleWithSubtext() {
    BasilTheme {
        OnboardingChatBubble(
            text = "Good to meet you, Alice. How do you manage your T1D?",
            subtext = "Helps me understand what your days look like."
        )
    }
}
