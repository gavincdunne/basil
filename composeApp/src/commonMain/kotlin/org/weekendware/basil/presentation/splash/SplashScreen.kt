package org.weekendware.basil.presentation.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import basil.composeapp.generated.resources.Res
import basil.composeapp.generated.resources.app_name
import basil.composeapp.generated.resources.app_type1_label
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import org.weekendware.basil.presentation.components.BasilLeaf
import org.weekendware.basil.presentation.theme.BasilPalette

/**
 * Splash screen shown during [SessionState.Loading] while the Supabase SDK
 * restores a stored session on cold start.
 *
 * Displays the Basil leaf mark + wordmark on a sage-green background. After
 * a 1.6 second hold the screen fades out over 400 ms, then calls
 * [onFadeComplete] so [App] can proceed to the resolved session state.
 *
 * On Android the OS-level SplashScreen API (`core-splashscreen`) covers the
 * startup phase. This composable handles the subsequent session-restoration
 * window, which applies on all platforms.
 *
 * @param onFadeComplete Called when the fade-out animation finishes.
 */
@Composable
fun SplashScreen(onFadeComplete: () -> Unit) {
    val alpha = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        delay(1600)
        alpha.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 400)
        )
        onFadeComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(alpha.value)
            .background(BasilPalette.Sage600),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .background(
                        color = Color.White.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(24.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                BasilLeaf(
                    size = 48.dp,
                    fill = Color.White,
                    vein = BasilPalette.Sage800
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text  = stringResource(Res.string.app_name),
                style = MaterialTheme.typography.displaySmall,
                color = Color.White
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text          = stringResource(Res.string.app_type1_label),
                style         = MaterialTheme.typography.labelSmall,
                color         = Color.White.copy(alpha = 0.5f),
                letterSpacing = 2.5.sp
            )
        }
    }
}
