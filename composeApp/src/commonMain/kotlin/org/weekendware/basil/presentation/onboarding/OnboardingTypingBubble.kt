package org.weekendware.basil.presentation.onboarding

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.weekendware.basil.presentation.theme.BasilTheme

private const val CYCLE_MS = 900
private const val DOT_UP = -5f
private const val DOT_STAGGER = 150

@Composable
fun OnboardingTypingBubble(modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(
        topStart = 16.dp,
        topEnd = 16.dp,
        bottomEnd = 16.dp,
        bottomStart = 4.dp
    )
    val transition = rememberInfiniteTransition(label = "typing")

    fun spec(delayMs: Int) = infiniteRepeatable<Float>(
        animation = keyframes {
            durationMillis = CYCLE_MS
            0f at delayMs using androidx.compose.animation.core.LinearEasing
            DOT_UP at (delayMs + 150) using androidx.compose.animation.core.LinearEasing
            0f at (delayMs + 300) using androidx.compose.animation.core.LinearEasing
            0f at CYCLE_MS using androidx.compose.animation.core.LinearEasing
        },
        repeatMode = RepeatMode.Restart
    )

    val dot1 by transition.animateFloat(0f, DOT_UP, spec(0), label = "dot1")
    val dot2 by transition.animateFloat(0f, DOT_UP, spec(DOT_STAGGER), label = "dot2")
    val dot3 by transition.animateFloat(0f, DOT_UP, spec(DOT_STAGGER * 2), label = "dot3")

    Row(
        modifier = modifier
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, shape)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        listOf(dot1, dot2, dot3).forEach { offset ->
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .offset(y = offset.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurfaceVariant)
            )
        }
    }
}

@Preview
@Composable
private fun PreviewOnboardingTypingBubble() {
    BasilTheme {
        OnboardingTypingBubble()
    }
}
