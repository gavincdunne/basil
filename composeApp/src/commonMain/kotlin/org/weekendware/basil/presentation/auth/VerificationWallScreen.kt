package org.weekendware.basil.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import basil.composeapp.generated.resources.Res
import basil.composeapp.generated.resources.app_name
import basil.composeapp.generated.resources.auth_resend_verification
import basil.composeapp.generated.resources.auth_sign_out_different_account
import basil.composeapp.generated.resources.auth_verify_wall_body
import basil.composeapp.generated.resources.auth_verify_wall_sent_to
import basil.composeapp.generated.resources.auth_verify_wall_title
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import org.weekendware.basil.presentation.theme.BasilPalette
import org.weekendware.basil.presentation.theme.BasilTheme
import org.weekendware.basil.presentation.theme.BasilTokens
import org.weekendware.basil.presentation.theme.authHeadingStyle
import org.weekendware.basil.presentation.theme.authHeroWordmarkStyle

/**
 * Shown when a user has gone 30+ days without verifying their email — the
 * soft-block wall (AUTH-09). Full screen, no dismiss affordance: the only
 * two exits are verifying (outside the app, via the emailed link) or
 * signing out to try a different account.
 */
@Composable
fun VerificationWallScreen(modifier: Modifier = Modifier) {
    val viewModel = koinViewModel<VerificationWallViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()

    VerificationWallScreenContent(
        state      = state,
        onResend   = viewModel::onResend,
        onSignOut  = viewModel::onSignOut,
        modifier   = modifier,
    )
}

@Composable
fun VerificationWallScreenContent(
    state:     VerificationWallUiState,
    onResend:  () -> Unit,
    onSignOut: () -> Unit,
    modifier:  Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize().background(BasilPalette.Cream)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(BasilPalette.Sage600)
                .statusBarsPadding()
                .padding(top = 20.dp, start = 20.dp, end = 20.dp, bottom = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(BasilTokens.AuthLogoSize)
                    .clip(RoundedCornerShape(BasilTokens.AuthLogoCorner))
                    .background(Color.White.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "b", style = MaterialTheme.typography.headlineMedium, color = Color.White)
            }
            Spacer(Modifier.height(8.dp))
            Text(text = stringResource(Res.string.app_name), style = authHeroWordmarkStyle(), color = Color.White)
        }

        Column(
            modifier            = Modifier.fillMaxSize().padding(horizontal = 24.dp).padding(top = 20.dp, bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(BasilPalette.AuthInfoCardBg),
                contentAlignment = Alignment.Center,
            ) {
                Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = BasilPalette.Sage600)
            }
            Spacer(Modifier.height(20.dp))
            Text(
                text      = stringResource(Res.string.auth_verify_wall_title),
                style     = authHeadingStyle(),
                color     = BasilPalette.AuthNearBlack,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text      = stringResource(Res.string.auth_verify_wall_body),
                style     = MaterialTheme.typography.bodyMedium,
                color     = BasilPalette.AuthFieldText,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text      = stringResource(Res.string.auth_verify_wall_sent_to),
                style     = MaterialTheme.typography.bodyMedium,
                color     = BasilPalette.AuthFieldText,
                textAlign = TextAlign.Center,
            )
            Text(
                text       = state.email,
                style      = MaterialTheme.typography.bodyMedium,
                color      = BasilPalette.Sage600,
                fontWeight = FontWeight.SemiBold,
                textAlign  = TextAlign.Center,
            )
            Spacer(Modifier.height(28.dp))
            if (state.isLoading) {
                CircularProgressIndicator(color = BasilPalette.Sage600)
            } else {
                Button(
                    onClick  = onResend,
                    enabled  = state.canResend,
                    shape    = RoundedCornerShape(50),
                    colors   = ButtonDefaults.buttonColors(
                        containerColor        = BasilPalette.Sage600,
                        contentColor          = Color.White,
                        disabledContainerColor = BasilPalette.AuthButtonMuted,
                        disabledContentColor   = Color.White,
                    ),
                    modifier = Modifier.fillMaxWidth().height(BasilTokens.ButtonHeight),
                ) {
                    Text(text = stringResource(Res.string.auth_resend_verification), style = MaterialTheme.typography.labelLarge)
                }
            }
            Spacer(Modifier.height(16.dp))
            TextButton(onClick = onSignOut) {
                Text(
                    text  = stringResource(Res.string.auth_sign_out_different_account),
                    style = MaterialTheme.typography.labelMedium,
                    color = BasilPalette.Sage600,
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Previews
// ─────────────────────────────────────────────────────────────

@Preview
@Composable
internal fun VerificationWallPreview() {
    BasilTheme {
        VerificationWallScreenContent(
            state     = VerificationWallUiState(email = "john@example.com"),
            onResend  = {},
            onSignOut = {},
        )
    }
}

@Preview
@Composable
internal fun VerificationWallCooldownPreview() {
    BasilTheme {
        VerificationWallScreenContent(
            state     = VerificationWallUiState(email = "john@example.com", canResend = false),
            onResend  = {},
            onSignOut = {},
        )
    }
}
