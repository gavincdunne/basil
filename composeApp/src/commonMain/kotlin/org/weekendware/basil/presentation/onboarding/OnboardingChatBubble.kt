package org.weekendware.basil.presentation.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontStyle
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
        topStart = 18.dp,
        topEnd = 18.dp,
        bottomEnd = 18.dp,
        bottomStart = 4.dp
    )
    Column(
        modifier = modifier
            .widthIn(max = 280.dp)
            .clip(shape)
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .border(1.dp, MaterialTheme.colorScheme.outline, shape)
            .padding(
                horizontal = MaterialTheme.basilSpacing.md,
                vertical = MaterialTheme.basilSpacing.sm
            )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
        if (subtext != null) {
            Text(
                text = subtext,
                style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
        topStart = 18.dp,
        topEnd = 18.dp,
        bottomEnd = 4.dp,
        bottomStart = 18.dp
    )
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onPrimary,
        modifier = modifier
            .widthIn(max = 280.dp)
            .clip(shape)
            .background(MaterialTheme.colorScheme.primary)
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
