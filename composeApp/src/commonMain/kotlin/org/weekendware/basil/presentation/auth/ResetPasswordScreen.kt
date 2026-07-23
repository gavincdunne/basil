package org.weekendware.basil.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import basil.composeapp.generated.resources.Res
import basil.composeapp.generated.resources.auth_check_email_body
import basil.composeapp.generated.resources.auth_check_email_title
import basil.composeapp.generated.resources.auth_field_email
import basil.composeapp.generated.resources.auth_resend_email
import basil.composeapp.generated.resources.auth_reset_subtitle
import basil.composeapp.generated.resources.auth_reset_title
import basil.composeapp.generated.resources.auth_send_reset_link
import basil.composeapp.generated.resources.cd_back
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import org.weekendware.basil.presentation.theme.BasilPalette
import org.weekendware.basil.presentation.theme.BasilTheme
import org.weekendware.basil.presentation.theme.BasilTokens

/**
 * Password reset screen — AUTH-05 (request form) before a link has been
 * sent, AUTH-06 (check-your-email) after.
 *
 * @param initialEmail Prefilled from the sign-in screen's email field.
 * @param onBack        Called from the back chevron, and from the link
 *   below "Resend email" once a reset link has been sent.
 */
@Composable
fun ResetPasswordScreen(initialEmail: String, onBack: () -> Unit) {
    val viewModel = koinViewModel<ResetPasswordViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()

    androidx.compose.runtime.LaunchedEffect(initialEmail) {
        if (state.email.isEmpty()) viewModel.onEmailChange(initialEmail)
    }

    ResetPasswordScreenContent(
        state         = state,
        onEmailChange = viewModel::onEmailChange,
        onSend        = viewModel::sendResetLink,
        onBack        = onBack,
    )
}

@Composable
fun ResetPasswordScreenContent(
    state:         ResetPasswordUiState,
    onEmailChange: (String) -> Unit,
    onSend:        () -> Unit,
    onBack:        () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BasilPalette.Cream)
    ) {
        ResetHeroBar(onBack = onBack)
        if (state.isSent) {
            ResetSentContent(email = state.email, onResend = onSend, onBack = onBack)
        } else {
            ResetRequestContent(state = state, onEmailChange = onEmailChange, onSend = onSend)
        }
    }
}

@Composable
private fun ResetHeroBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BasilPalette.Sage600)
            .statusBarsPadding()
            .padding(vertical = 16.dp, horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(Res.string.cd_back),
                tint               = Color.White.copy(alpha = 0.75f),
            )
        }
        Spacer(Modifier.width(4.dp))
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = "b", style = MaterialTheme.typography.titleMedium, color = Color.White)
        }
    }
}

@Composable
private fun ResetRequestContent(
    state:         ResetPasswordUiState,
    onEmailChange: (String) -> Unit,
    onSend:        () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 20.dp, bottom = 28.dp),
    ) {
        Text(
            text  = stringResource(Res.string.auth_reset_title),
            style = MaterialTheme.typography.headlineSmall,
            color = BasilPalette.AuthNearBlack,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text  = stringResource(Res.string.auth_reset_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = BasilPalette.AuthFieldText,
        )
        Spacer(Modifier.height(22.dp))
        Text(
            text          = stringResource(Res.string.auth_field_email).uppercase(),
            style         = MaterialTheme.typography.labelSmall,
            color         = BasilPalette.AuthFieldText,
            fontWeight    = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value           = state.email,
            onValueChange   = onEmailChange,
            singleLine      = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onSend() }),
            colors          = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                focusedContainerColor   = BasilPalette.Cream,
                unfocusedContainerColor = BasilPalette.Cream,
                focusedBorderColor      = BasilPalette.Sage600,
                unfocusedBorderColor    = BasilPalette.AuthFieldBorder,
                cursorColor             = BasilPalette.Sage600,
            ),
            shape    = RoundedCornerShape(BasilTokens.AuthFieldCorner),
            modifier = Modifier.fillMaxWidth().height(BasilTokens.AuthFieldHeight),
        )
        state.error?.let { errorRes ->
            Spacer(Modifier.height(8.dp))
            Text(text = stringResource(errorRes), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
        }
        Spacer(Modifier.height(8.dp))
        if (state.isLoading) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = BasilPalette.Sage600)
            }
        } else {
            Button(
                onClick  = onSend,
                enabled  = state.canSend,
                shape    = RoundedCornerShape(50),
                colors   = ButtonDefaults.buttonColors(
                    containerColor        = BasilPalette.Sage600,
                    contentColor          = Color.White,
                    disabledContainerColor = BasilPalette.AuthButtonMuted,
                    disabledContentColor   = Color.White,
                ),
                modifier = Modifier.fillMaxWidth().height(BasilTokens.ButtonHeight),
            ) {
                Text(text = stringResource(Res.string.auth_send_reset_link), style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun ResetSentContent(email: String, onResend: () -> Unit, onBack: () -> Unit) {
    Column(
        modifier             = Modifier.fillMaxSize().padding(horizontal = 24.dp).padding(top = 36.dp, bottom = 40.dp),
        horizontalAlignment  = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(BasilPalette.AuthInfoCardBg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = BasilPalette.Sage600)
        }
        Spacer(Modifier.height(20.dp))
        Text(
            text  = stringResource(Res.string.auth_check_email_title),
            style = MaterialTheme.typography.headlineSmall,
            color = BasilPalette.AuthNearBlack,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text  = stringResource(Res.string.auth_check_email_body),
            style = MaterialTheme.typography.bodyMedium,
            color = BasilPalette.AuthFieldText,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text       = email,
            style      = MaterialTheme.typography.bodyMedium,
            color      = BasilPalette.AuthNearBlack,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(28.dp))
        OutlinedButton(
            onClick = onResend,
            shape   = RoundedCornerShape(50),
            colors  = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                containerColor = Color.White,
                contentColor   = BasilPalette.Sage600,
            ),
            border  = androidx.compose.foundation.BorderStroke(1.5.dp, BasilPalette.AuthGoogleBorder),
            modifier = Modifier.width(200.dp).height(48.dp),
        ) {
            Text(text = stringResource(Res.string.auth_resend_email), style = MaterialTheme.typography.labelMedium)
        }
        Spacer(Modifier.height(16.dp))
        TextButton(onClick = onBack) {
            Icon(
                imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(Res.string.cd_back),
                tint               = BasilPalette.AuthFieldText,
                modifier           = Modifier.size(16.dp),
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Previews
// ─────────────────────────────────────────────────────────────

@Preview
@Composable
internal fun ResetPasswordRequestPreview() {
    BasilTheme {
        ResetPasswordScreenContent(
            state         = ResetPasswordUiState(email = "john@example.com"),
            onEmailChange = {},
            onSend        = {},
            onBack        = {},
        )
    }
}

@Preview
@Composable
internal fun ResetPasswordSentPreview() {
    BasilTheme {
        ResetPasswordScreenContent(
            state         = ResetPasswordUiState(email = "john@example.com", isSent = true),
            onEmailChange = {},
            onSend        = {},
            onBack        = {},
        )
    }
}
