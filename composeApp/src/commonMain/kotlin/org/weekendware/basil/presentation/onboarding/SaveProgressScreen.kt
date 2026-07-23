package org.weekendware.basil.presentation.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import basil.composeapp.generated.resources.Res
import basil.composeapp.generated.resources.auth_continue_with_apple
import basil.composeapp.generated.resources.auth_continue_with_google
import basil.composeapp.generated.resources.auth_field_email
import basil.composeapp.generated.resources.auth_field_password
import basil.composeapp.generated.resources.auth_or
import basil.composeapp.generated.resources.cd_back
import basil.composeapp.generated.resources.cd_hide_password
import basil.composeapp.generated.resources.cd_show_password
import basil.composeapp.generated.resources.save_progress_basil_message
import basil.composeapp.generated.resources.save_progress_continue_with_email
import basil.composeapp.generated.resources.save_progress_create_account
import basil.composeapp.generated.resources.save_progress_create_account_title
import basil.composeapp.generated.resources.save_progress_maybe_later
import basil.composeapp.generated.resources.save_progress_title
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import org.weekendware.basil.presentation.auth.PasswordRequirement
import org.weekendware.basil.presentation.auth.PasswordStrength
import org.weekendware.basil.presentation.theme.BasilPalette
import org.weekendware.basil.presentation.theme.BasilTheme
import org.weekendware.basil.presentation.theme.BasilTokens
import org.weekendware.basil.presentation.theme.backgroundBrush
import org.weekendware.basil.presentation.theme.basilColors

/**
 * "Save your progress" — shown at the end of onboarding to create an
 * account (AUTH-12 choose-method, AUTH-12b email/password entry).
 *
 * Not wired into app routing yet: reaching this screen depends on the
 * full silent-account-provisioning rearchitecture — [OnboardingScreen]
 * currently only renders inside the authenticated app shell, not before
 * sign-in, so there's no unauthenticated path that reaches onboarding (and
 * therefore this screen) at all yet. That's a genuine App.kt routing
 * rearchitecture, not something to bolt on here. This composable is
 * complete and tested standalone, ready for that wiring once it exists.
 *
 * @param onMaybeLater Called when the user dismisses this screen without
 *   creating an account. Per the TDD, they're routed back here on their
 *   next launch — not wired to any state here, purely a navigation signal
 *   for whatever screen hosts this one.
 */
@Composable
fun SaveProgressScreen(onMaybeLater: () -> Unit = {}) {
    val viewModel = koinViewModel<SaveProgressViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()

    SaveProgressScreenContent(
        state                      = state,
        onContinueWithEmail        = viewModel::onContinueWithEmail,
        onBackToChooseMethod       = viewModel::onBackToChooseMethod,
        onEmailChange              = viewModel::onEmailChange,
        onPasswordChange           = viewModel::onPasswordChange,
        onTogglePasswordVisibility = viewModel::onTogglePasswordVisibility,
        onGoogleSignUp             = viewModel::onGoogleSignUp,
        onAppleSignUp              = viewModel::onAppleSignUp,
        onCreateAccount            = viewModel::onCreateAccount,
        onMaybeLater               = onMaybeLater,
    )
}

@Composable
fun SaveProgressScreenContent(
    state:                      SaveProgressUiState,
    onContinueWithEmail:         () -> Unit,
    onBackToChooseMethod:        () -> Unit,
    onEmailChange:               (String) -> Unit,
    onPasswordChange:            (String) -> Unit,
    onTogglePasswordVisibility:  () -> Unit,
    onGoogleSignUp:               () -> Unit,
    onAppleSignUp:                () -> Unit,
    onCreateAccount:              () -> Unit,
    onMaybeLater:                 () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.basilColors.backgroundBrush()),
    ) {
        // Onboarding conversation strip — Basil's closing message, matching
        // the mockup's "conversation visible above the sheet" framing.
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(
                text  = "basil",
                style = MaterialTheme.typography.labelSmall,
                color = BasilPalette.AuthPlaceholderText,
            )
            Spacer(Modifier.height(3.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 4.dp))
                    .background(Color.White)
                    .border(1.dp, BasilPalette.AuthGoogleBorder, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 4.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
            ) {
                Text(
                    text  = stringResource(Res.string.save_progress_basil_message),
                    style = MaterialTheme.typography.bodyMedium,
                    color = BasilPalette.AuthNearBlack,
                )
            }
        }

        Spacer(Modifier.weight(1f))

        // Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                .background(Color.White)
                .padding(horizontal = 20.dp)
                .padding(top = 20.dp, bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            when (state.step) {
                SaveProgressStep.ChooseMethod -> ChooseMethodContent(
                    onGoogleSignUp      = onGoogleSignUp,
                    onAppleSignUp       = onAppleSignUp,
                    onContinueWithEmail = onContinueWithEmail,
                    onMaybeLater        = onMaybeLater,
                )
                SaveProgressStep.EmailEntry -> EmailEntryContent(
                    state                      = state,
                    onEmailChange              = onEmailChange,
                    onPasswordChange           = onPasswordChange,
                    onTogglePasswordVisibility = onTogglePasswordVisibility,
                    onCreateAccount            = onCreateAccount,
                    onBack                     = onBackToChooseMethod,
                )
            }
        }
    }
}

@Composable
private fun ChooseMethodContent(
    onGoogleSignUp:      () -> Unit,
    onAppleSignUp:        () -> Unit,
    onContinueWithEmail: () -> Unit,
    onMaybeLater:        () -> Unit,
) {
    Text(
        text  = stringResource(Res.string.save_progress_title),
        style = MaterialTheme.typography.headlineSmall,
        color = BasilPalette.AuthNearBlack,
    )
    Spacer(Modifier.height(16.dp))
    OutlinedButton(
        onClick  = onAppleSignUp,
        shape    = RoundedCornerShape(BasilTokens.AuthFieldCorner),
        border   = BorderStroke(0.dp, Color.Transparent),
        colors   = ButtonDefaults.outlinedButtonColors(containerColor = BasilPalette.Black, contentColor = Color.White),
        modifier = Modifier.fillMaxWidth().height(BasilTokens.ButtonHeight),
    ) {
        Text(text = stringResource(Res.string.auth_continue_with_apple), style = MaterialTheme.typography.labelLarge)
    }
    Spacer(Modifier.height(10.dp))
    OutlinedButton(
        onClick  = onGoogleSignUp,
        shape    = RoundedCornerShape(BasilTokens.AuthFieldCorner),
        border   = BorderStroke(1.5.dp, BasilPalette.AuthGoogleBorder),
        colors   = ButtonDefaults.outlinedButtonColors(containerColor = Color.White, contentColor = BasilPalette.AuthNearBlack),
        modifier = Modifier.fillMaxWidth().height(BasilTokens.ButtonHeight),
    ) {
        Text(text = stringResource(Res.string.auth_continue_with_google), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium)
    }
    Spacer(Modifier.height(14.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = BasilPalette.AuthFieldBorder.copy(alpha = 0.3f))
        Text(
            text     = stringResource(Res.string.auth_or),
            style    = MaterialTheme.typography.bodyMedium,
            color    = BasilPalette.AuthFieldText,
            modifier = Modifier.padding(horizontal = 12.dp),
        )
        HorizontalDivider(modifier = Modifier.weight(1f), color = BasilPalette.AuthFieldBorder.copy(alpha = 0.3f))
    }
    Spacer(Modifier.height(8.dp))
    OutlinedButton(
        onClick  = onContinueWithEmail,
        shape    = RoundedCornerShape(50),
        border   = BorderStroke(1.5.dp, BasilPalette.Sage600),
        colors   = ButtonDefaults.outlinedButtonColors(containerColor = Color.Transparent, contentColor = BasilPalette.Sage600),
        modifier = Modifier.fillMaxWidth().height(BasilTokens.ButtonHeight),
    ) {
        Text(text = stringResource(Res.string.save_progress_continue_with_email), style = MaterialTheme.typography.labelLarge)
    }
    Spacer(Modifier.height(12.dp))
    TextButton(onClick = onMaybeLater, modifier = Modifier.fillMaxWidth()) {
        Text(
            text  = stringResource(Res.string.save_progress_maybe_later),
            style = MaterialTheme.typography.bodyMedium,
            color = BasilPalette.AuthPlaceholderText,
        )
    }
}

@Composable
private fun EmailEntryContent(
    state:                      SaveProgressUiState,
    onEmailChange:               (String) -> Unit,
    onPasswordChange:            (String) -> Unit,
    onTogglePasswordVisibility:  () -> Unit,
    onCreateAccount:              () -> Unit,
    onBack:                       () -> Unit,
) {
    Text(
        text  = stringResource(Res.string.save_progress_create_account_title),
        style = MaterialTheme.typography.titleLarge,
        color = BasilPalette.AuthNearBlack,
    )
    Spacer(Modifier.height(20.dp))
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
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
        colors          = OutlinedTextFieldDefaults.colors(
            focusedContainerColor   = BasilPalette.Cream,
            unfocusedContainerColor = BasilPalette.Cream,
            focusedBorderColor      = BasilPalette.Sage600,
            unfocusedBorderColor    = BasilPalette.AuthFieldBorder,
            cursorColor             = BasilPalette.Sage600,
        ),
        shape    = RoundedCornerShape(BasilTokens.AuthFieldCorner),
        modifier = Modifier.fillMaxWidth().height(BasilTokens.AuthFieldHeight),
    )
    Spacer(Modifier.height(14.dp))
    Text(
        text          = stringResource(Res.string.auth_field_password).uppercase(),
        style         = MaterialTheme.typography.labelSmall,
        color         = BasilPalette.AuthFieldText,
        fontWeight    = FontWeight.SemiBold,
    )
    Spacer(Modifier.height(6.dp))
    OutlinedTextField(
        value                = state.password,
        onValueChange        = onPasswordChange,
        singleLine           = true,
        visualTransformation = if (state.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions      = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
        keyboardActions      = KeyboardActions(onDone = { onCreateAccount() }),
        trailingIcon = {
            IconButton(onClick = onTogglePasswordVisibility) {
                Icon(
                    imageVector        = if (state.isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = stringResource(if (state.isPasswordVisible) Res.string.cd_hide_password else Res.string.cd_show_password),
                    tint               = BasilPalette.AuthFieldText,
                )
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor   = BasilPalette.Cream,
            unfocusedContainerColor = BasilPalette.Cream,
            focusedBorderColor      = if (state.passwordStrength == PasswordStrength.Strong) BasilPalette.AuthStrengthGreen else BasilPalette.Sage600,
            unfocusedBorderColor    = if (state.passwordStrength == PasswordStrength.Strong) BasilPalette.AuthStrengthGreen else BasilPalette.AuthFieldBorder,
            cursorColor             = BasilPalette.Sage600,
        ),
        shape    = RoundedCornerShape(BasilTokens.AuthFieldCorner),
        modifier = Modifier.fillMaxWidth().height(BasilTokens.AuthFieldHeight),
    )
    PasswordStrengthIndicator(strength = state.passwordStrength, requirements = state.passwordRequirements)
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
            onClick  = onCreateAccount,
            enabled  = state.canCreateAccount,
            shape    = RoundedCornerShape(50),
            colors   = ButtonDefaults.buttonColors(
                containerColor        = BasilPalette.Sage600,
                contentColor          = Color.White,
                disabledContainerColor = BasilPalette.AuthButtonMuted,
                disabledContentColor   = Color.White,
            ),
            modifier = Modifier.fillMaxWidth().height(BasilTokens.ButtonHeight),
        ) {
            Text(text = stringResource(Res.string.save_progress_create_account), style = MaterialTheme.typography.labelLarge)
        }
    }
    Spacer(Modifier.height(12.dp))
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(Res.string.cd_back),
                tint               = BasilPalette.AuthFieldText,
                modifier           = Modifier.size(16.dp),
            )
        }
    }
}

/** Same segmented bar + requirement checklist as [org.weekendware.basil.presentation.auth.NewPasswordScreen]. */
@Composable
private fun PasswordStrengthIndicator(strength: PasswordStrength, requirements: List<PasswordRequirement>) {
    if (strength == PasswordStrength.None) return

    val (filledCount, color, label) = when (strength) {
        PasswordStrength.Weak   -> Triple(1, BasilPalette.Error, "Weak")
        PasswordStrength.Medium -> Triple(2, BasilPalette.AuthStrengthAmber, "Medium")
        PasswordStrength.Strong -> Triple(3, BasilPalette.AuthStrengthGreen, "Strong")
        PasswordStrength.None   -> Triple(0, BasilPalette.AuthFieldBorder, "")
    }

    Column(modifier = Modifier.padding(top = 10.dp, bottom = 10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            repeat(3) { index ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .padding(end = if (index < 2) 3.dp else 0.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (index < filledCount) color else BasilPalette.AuthGoogleBorder),
                )
            }
            Spacer(Modifier.width(8.dp))
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(8.dp))
        requirements.forEach { requirement ->
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                Box(
                    modifier = if (requirement.met) {
                        Modifier.size(14.dp).clip(CircleShape).background(BasilPalette.Sage600)
                    } else {
                        Modifier.size(14.dp).clip(CircleShape).border(1.5.dp, BasilPalette.AuthFieldBorder, CircleShape)
                    },
                    contentAlignment = Alignment.Center,
                ) {
                    if (requirement.met) {
                        Text(text = "✓", style = MaterialTheme.typography.labelSmall, color = Color.White)
                    }
                }
                Spacer(Modifier.width(7.dp))
                Text(text = stringResource(requirement.label), style = MaterialTheme.typography.labelSmall, color = BasilPalette.AuthFieldText)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Previews
// ─────────────────────────────────────────────────────────────

@Preview
@Composable
internal fun SaveProgressChooseMethodPreview() {
    BasilTheme {
        SaveProgressScreenContent(
            state                      = SaveProgressUiState(),
            onContinueWithEmail        = {},
            onBackToChooseMethod       = {},
            onEmailChange              = {},
            onPasswordChange           = {},
            onTogglePasswordVisibility = {},
            onGoogleSignUp             = {},
            onAppleSignUp              = {},
            onCreateAccount            = {},
            onMaybeLater               = {},
        )
    }
}

@Preview
@Composable
internal fun SaveProgressEmailEntryPreview() {
    BasilTheme {
        SaveProgressScreenContent(
            state = SaveProgressUiState(step = SaveProgressStep.EmailEntry, email = "john@example.com"),
            onContinueWithEmail        = {},
            onBackToChooseMethod       = {},
            onEmailChange              = {},
            onPasswordChange           = {},
            onTogglePasswordVisibility = {},
            onGoogleSignUp             = {},
            onAppleSignUp              = {},
            onCreateAccount            = {},
            onMaybeLater               = {},
        )
    }
}
