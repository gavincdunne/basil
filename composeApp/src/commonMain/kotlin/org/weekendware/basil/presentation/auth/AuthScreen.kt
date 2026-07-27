package org.weekendware.basil.presentation.auth

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.compose.auth.composable.rememberSignInWithApple
import io.github.jan.supabase.compose.auth.composable.rememberSignInWithGoogle
import io.github.jan.supabase.compose.auth.composeAuth
import basil.composeapp.generated.resources.Res
import basil.composeapp.generated.resources.app_name
import basil.composeapp.generated.resources.auth_continue
import basil.composeapp.generated.resources.auth_continue_with_apple
import basil.composeapp.generated.resources.auth_continue_with_google
import basil.composeapp.generated.resources.auth_field_email
import basil.composeapp.generated.resources.auth_field_password
import basil.composeapp.generated.resources.auth_forgot_password
import basil.composeapp.generated.resources.auth_get_started
import basil.composeapp.generated.resources.auth_no_account_body
import basil.composeapp.generated.resources.auth_no_account_title
import basil.composeapp.generated.resources.auth_or
import basil.composeapp.generated.resources.auth_sign_in
import basil.composeapp.generated.resources.auth_signin_subtitle
import basil.composeapp.generated.resources.auth_tagline
import basil.composeapp.generated.resources.auth_try_different_email
import basil.composeapp.generated.resources.auth_use_different_account
import basil.composeapp.generated.resources.auth_welcome_back
import basil.composeapp.generated.resources.cd_hide_password
import basil.composeapp.generated.resources.cd_show_password
import basil.composeapp.generated.resources.error_auth_failed
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.weekendware.basil.presentation.theme.BasilPalette
import org.weekendware.basil.presentation.theme.BasilTheme
import org.weekendware.basil.presentation.theme.BasilTokens

/**
 * Authentication screen, shown when no valid session exists.
 *
 * Wires [AuthViewModel] to [AuthScreenContent]. Navigation away from this
 * screen happens automatically when [org.weekendware.basil.presentation.session.SessionViewModel]
 * detects a successful authentication.
 *
 * @param onGetStarted Called when a user with no recognized account taps
 *   "Get started" on the no-account-found state. Currently a no-op by
 *   default — the full silent-account-provisioning redirect into
 *   onboarding is separate, not-yet-wired routing work.
 * @param onForgotPassword Called with the current email when the user taps
 *   "Forgot password?" on the sign-in step.
 */
@Composable
fun AuthScreen(onGetStarted: () -> Unit = {}, onForgotPassword: (String) -> Unit = {}) {
    val viewModel = koinViewModel<AuthViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()

    // compose-auth's actions live here, not in AuthViewModel — the actual
    // OAuth/native-credential ceremony runs inside these composables
    // (Android CredentialManager for Google, iOS AuthenticationServices for
    // Apple; standard OAuth browser redirect as the fallback everywhere
    // else), directly against the Koin-injected SupabaseClient. The
    // ViewModel only reacts to the result via onSocialSignInResult.
    val supabaseClient = koinInject<SupabaseClient>()
    val googleSignIn = supabaseClient.composeAuth.rememberSignInWithGoogle(onResult = viewModel::onSocialSignInResult)
    val appleSignIn = supabaseClient.composeAuth.rememberSignInWithApple(onResult = viewModel::onSocialSignInResult)

    AuthScreenContent(
        state                       = state,
        onEmailChange               = viewModel::onEmailChange,
        onPasswordChange            = viewModel::onPasswordChange,
        onTogglePasswordVisibility  = viewModel::onTogglePasswordVisibility,
        onContinueEmail             = viewModel::onContinueEmail,
        onUseDifferentAccount       = viewModel::onUseDifferentAccount,
        onSubmit                    = viewModel::submit,
        onGoogleSignIn              = { viewModel.onSocialSignInStarted(); googleSignIn.startFlow() },
        onAppleSignIn               = { viewModel.onSocialSignInStarted(); appleSignIn.startFlow() },
        onGetStarted                = onGetStarted,
        onForgotPassword            = onForgotPassword,
    )
}

/**
 * Stateless authentication UI: an email-first step, then either a sign-in
 * password step (account recognized) or a no-account-found state, per the
 * approved UI Designer mockup.
 */
@Composable
fun AuthScreenContent(
    state:                      AuthUiState,
    onEmailChange:               (String) -> Unit,
    onPasswordChange:            (String) -> Unit,
    onTogglePasswordVisibility:  () -> Unit,
    onContinueEmail:              () -> Unit,
    onUseDifferentAccount:        () -> Unit,
    onSubmit:                     () -> Unit,
    onGoogleSignIn:                () -> Unit,
    onAppleSignIn:                 () -> Unit,
    onGetStarted:                  () -> Unit,
    onForgotPassword:               (String) -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BasilPalette.Cream)
    ) {
        AuthHeroBand(showTagline = state.emailStep)
        if (state.emailStep) {
            EmailStepForm(
                state           = state,
                onEmailChange   = onEmailChange,
                onContinueEmail = onContinueEmail,
                onGoogleSignIn  = onGoogleSignIn,
                onAppleSignIn   = onAppleSignIn,
            )
        } else when (state.mode) {
            AuthMode.SignIn -> SignInForm(
                state                      = state,
                onPasswordChange           = onPasswordChange,
                onTogglePasswordVisibility = onTogglePasswordVisibility,
                onUseDifferentAccount      = onUseDifferentAccount,
                onSubmit                   = onSubmit,
                onForgotPassword           = { onForgotPassword(state.email) },
            )
            AuthMode.SignUp -> NoAccountFound(
                email                 = state.email,
                onUseDifferentAccount = onUseDifferentAccount,
                onGetStarted          = onGetStarted,
            )
        }
    }
}

/**
 * The hero band is always the full column layout (52dp logo, 28sp wordmark)
 * on every step of this screen — email, sign-in, and no-account-found alike.
 * Only the tagline is conditional: shown on the email step, hidden once the
 * user has moved past it. There is no separate "compact" row variant here —
 * that belongs to [ResetPasswordScreen]'s distinct mini-header.
 */
@Composable
private fun AuthHeroBand(showTagline: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BasilPalette.Sage600)
            .statusBarsPadding()
            .padding(top = 20.dp, start = 20.dp, end = 20.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AuthLogoPlaceholder()
        Spacer(Modifier.height(8.dp))
        Text(
            text  = stringResource(Res.string.app_name),
            style = MaterialTheme.typography.displaySmall,
            color = Color.White,
        )
        if (showTagline) {
            Spacer(Modifier.height(8.dp))
            Text(
                text          = stringResource(Res.string.auth_tagline).uppercase(),
                style         = MaterialTheme.typography.labelSmall,
                color         = Color.White.copy(alpha = 0.55f),
                letterSpacing = 1.5.sp,
            )
        }
    }
}

/** Logo placeholder — final mark pending Design; a lettered square, not the splash [BasilLeaf]. */
@Composable
private fun AuthLogoPlaceholder() {
    Box(
        modifier = Modifier
            .size(BasilTokens.AuthLogoSize)
            .clip(RoundedCornerShape(BasilTokens.AuthLogoCorner))
            .background(Color.White.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text  = "b",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
        )
    }
}

@Composable
private fun EmailStepForm(
    state:            AuthUiState,
    onEmailChange:    (String) -> Unit,
    onContinueEmail:  () -> Unit,
    onGoogleSignIn:   () -> Unit,
    onAppleSignIn:    () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 24.dp, bottom = 28.dp),
    ) {
        Spacer(Modifier.height(8.dp))
        SocialButton(
            label      = stringResource(Res.string.auth_continue_with_apple),
            background = BasilPalette.Black,
            textColor  = Color.White,
            onClick    = onAppleSignIn,
        )
        Spacer(Modifier.height(10.dp))
        SocialButton(
            label      = stringResource(Res.string.auth_continue_with_google),
            background = Color.White,
            textColor  = BasilPalette.AuthNearBlack,
            borderColor = BasilPalette.AuthGoogleBorder,
            onClick    = onGoogleSignIn,
        )
        Spacer(Modifier.height(16.dp))
        OrDivider()
        Spacer(Modifier.height(12.dp))
        AuthFieldLabel(stringResource(Res.string.auth_field_email))
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value           = state.email,
            onValueChange   = onEmailChange,
            singleLine      = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction    = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(onDone = { onContinueEmail() }),
            colors          = authFieldColors(),
            shape           = RoundedCornerShape(BasilTokens.AuthFieldCorner),
            modifier        = Modifier
                .fillMaxWidth()
                .height(BasilTokens.AuthFieldHeight),
        )
        Spacer(Modifier.height(8.dp))
        PrimaryButton(
            label   = stringResource(Res.string.auth_continue),
            enabled = state.canContinueEmail,
            onClick = onContinueEmail,
        )
    }
}

@Composable
private fun SignInForm(
    state:                      AuthUiState,
    onPasswordChange:           (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onUseDifferentAccount:      () -> Unit,
    onSubmit:                   () -> Unit,
    onForgotPassword:           () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 24.dp, bottom = 28.dp),
    ) {
        Spacer(Modifier.height(16.dp))
        Text(
            text  = stringResource(Res.string.auth_welcome_back),
            style = MaterialTheme.typography.headlineSmall,
            color = BasilPalette.AuthNearBlack,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text  = stringResource(Res.string.auth_signin_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = BasilPalette.AuthFieldText,
        )
        Spacer(Modifier.height(22.dp))
        AuthFieldLabel(stringResource(Res.string.auth_field_email))
        Spacer(Modifier.height(6.dp))
        LockedField(text = state.email)
        Spacer(Modifier.height(14.dp))
        AuthFieldLabel(stringResource(Res.string.auth_field_password))
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value                = state.password,
            onValueChange        = onPasswordChange,
            singleLine           = true,
            visualTransformation = if (state.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions      = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction    = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(onDone = { onSubmit() }),
            trailingIcon = {
                IconButton(onClick = onTogglePasswordVisibility) {
                    Icon(
                        imageVector        = if (state.isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = stringResource(
                            if (state.isPasswordVisible) Res.string.cd_hide_password else Res.string.cd_show_password
                        ),
                        tint = BasilPalette.AuthFieldText,
                    )
                }
            },
            colors   = authFieldColors(),
            shape    = RoundedCornerShape(BasilTokens.AuthFieldCorner),
            modifier = Modifier
                .fillMaxWidth()
                .height(BasilTokens.AuthFieldHeight),
        )
        state.error?.let { errorRes ->
            Spacer(Modifier.height(8.dp))
            Text(
                text  = stringResource(errorRes),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
        Spacer(Modifier.height(6.dp))
        TextButton(
            onClick  = onForgotPassword,
            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
        ) {
            Text(
                text      = stringResource(Res.string.auth_forgot_password),
                style     = MaterialTheme.typography.labelMedium,
                color     = BasilPalette.Sage600,
                modifier  = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End,
            )
        }
        if (state.isLoading) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = BasilPalette.Sage600)
            }
        } else {
            PrimaryButton(
                label   = stringResource(Res.string.auth_sign_in),
                enabled = state.canSubmit,
                onClick = onSubmit,
            )
        }
        Spacer(Modifier.height(16.dp))
        CenteredLink(
            text    = stringResource(Res.string.auth_use_different_account),
            onClick = onUseDifferentAccount,
        )
    }
}

@Composable
private fun NoAccountFound(
    email:                 String,
    onUseDifferentAccount: () -> Unit,
    onGetStarted:          () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 24.dp, bottom = 28.dp),
    ) {
        Spacer(Modifier.height(16.dp))
        LockedField(text = email, trailingIcon = Icons.Default.Close)
        Spacer(Modifier.height(14.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(BasilTokens.AuthInfoCardCorner))
                .background(BasilPalette.AuthInfoCardBg)
                .padding(horizontal = 16.dp, vertical = 14.dp),
        ) {
            Text(
                text       = stringResource(Res.string.auth_no_account_title),
                style      = MaterialTheme.typography.labelLarge,
                color      = BasilPalette.Sage600,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text  = stringResource(Res.string.auth_no_account_body),
                style = MaterialTheme.typography.bodySmall,
                color = BasilPalette.Sage600.copy(alpha = 0.8f),
            )
        }
        Spacer(Modifier.height(16.dp))
        PrimaryButton(
            label   = stringResource(Res.string.auth_get_started),
            enabled = true,
            onClick = onGetStarted,
        )
        Spacer(Modifier.height(12.dp))
        CenteredLink(
            text    = stringResource(Res.string.auth_try_different_email),
            onClick = onUseDifferentAccount,
        )
    }
}

// ─────────────────────────────────────────────────────────────
// Shared pieces
// ─────────────────────────────────────────────────────────────

@Composable
private fun AuthFieldLabel(text: String) {
    Text(
        text          = text.uppercase(),
        style         = MaterialTheme.typography.labelSmall,
        color         = BasilPalette.AuthFieldText,
        fontWeight    = FontWeight.SemiBold,
        letterSpacing = 0.3.sp,
    )
}

@Composable
private fun LockedField(text: String, trailingIcon: ImageVector? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(BasilTokens.AuthFieldHeight)
            .clip(RoundedCornerShape(BasilTokens.AuthFieldCorner))
            .background(BasilPalette.Cream)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text     = text,
            style    = MaterialTheme.typography.bodyLarge,
            color    = BasilPalette.AuthFieldText,
            modifier = Modifier.weight(1f),
        )
        trailingIcon?.let {
            Icon(imageVector = it, contentDescription = null, tint = BasilPalette.AuthPlaceholderText)
        }
    }
}

@Composable
private fun PrimaryButton(label: String, enabled: Boolean, onClick: () -> Unit) {
    Button(
        onClick  = onClick,
        enabled  = enabled,
        shape    = RoundedCornerShape(50),
        colors   = ButtonDefaults.buttonColors(
            containerColor         = BasilPalette.Sage600,
            contentColor            = Color.White,
            disabledContainerColor  = BasilPalette.AuthButtonMuted,
            disabledContentColor    = Color.White,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(BasilTokens.ButtonHeight),
    ) {
        Text(text = label, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun SocialButton(
    label:       String,
    background:  Color,
    textColor:   Color,
    borderColor: Color? = null,
    onClick:     () -> Unit,
) {
    val border = borderColor?.let {
        BorderStroke(1.5.dp, it)
    }
    OutlinedButton(
        onClick  = onClick,
        shape    = RoundedCornerShape(BasilTokens.AuthFieldCorner),
        border   = border ?: BorderStroke(0.dp, Color.Transparent),
        colors   = ButtonDefaults.outlinedButtonColors(
            containerColor = background,
            contentColor   = textColor,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(BasilTokens.ButtonHeight),
    ) {
        Text(text = label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun OrDivider() {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color    = BasilPalette.AuthFieldBorder.copy(alpha = 0.3f),
        )
        Text(
            text     = stringResource(Res.string.auth_or),
            style    = MaterialTheme.typography.bodyMedium,
            color    = BasilPalette.AuthFieldText,
            modifier = Modifier.padding(horizontal = 12.dp),
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color    = BasilPalette.AuthFieldBorder.copy(alpha = 0.3f),
        )
    }
}

@Composable
private fun CenteredLink(text: String, onClick: () -> Unit) {
    TextButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Text(
            text  = text,
            style = MaterialTheme.typography.bodyMedium,
            color = BasilPalette.Sage600,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun authFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor   = BasilPalette.Cream,
    unfocusedContainerColor = BasilPalette.Cream,
    focusedBorderColor      = BasilPalette.Sage600,
    unfocusedBorderColor    = BasilPalette.AuthFieldBorder,
    focusedTextColor        = BasilPalette.AuthNearBlack,
    unfocusedTextColor      = BasilPalette.AuthNearBlack,
    cursorColor             = BasilPalette.Sage600,
)

// ─────────────────────────────────────────────────────────────
// Previews
// ─────────────────────────────────────────────────────────────

@Preview
@Composable
internal fun AuthScreenEmailStepPreview() {
    BasilTheme {
        AuthScreenContent(
            state                      = AuthUiState(),
            onEmailChange              = {},
            onPasswordChange           = {},
            onTogglePasswordVisibility = {},
            onContinueEmail            = {},
            onUseDifferentAccount      = {},
            onSubmit                   = {},
            onGoogleSignIn             = {},
            onAppleSignIn              = {},
            onGetStarted               = {},
        )
    }
}

@Preview
@Composable
internal fun AuthScreenSignInPreview() {
    BasilTheme {
        AuthScreenContent(
            state = AuthUiState(
                email     = "john@example.com",
                emailStep = false,
                mode      = AuthMode.SignIn,
            ),
            onEmailChange              = {},
            onPasswordChange           = {},
            onTogglePasswordVisibility = {},
            onContinueEmail            = {},
            onUseDifferentAccount      = {},
            onSubmit                   = {},
            onGoogleSignIn             = {},
            onAppleSignIn              = {},
            onGetStarted               = {},
        )
    }
}

@Preview
@Composable
internal fun AuthScreenNoAccountPreview() {
    BasilTheme {
        AuthScreenContent(
            state = AuthUiState(
                email     = "new@email.com",
                emailStep = false,
                mode      = AuthMode.SignUp,
            ),
            onEmailChange              = {},
            onPasswordChange           = {},
            onTogglePasswordVisibility = {},
            onContinueEmail            = {},
            onUseDifferentAccount      = {},
            onSubmit                   = {},
            onGoogleSignIn             = {},
            onAppleSignIn              = {},
            onGetStarted               = {},
        )
    }
}

@Preview
@Composable
internal fun AuthScreenLoadingPreview() {
    BasilTheme {
        AuthScreenContent(
            state = AuthUiState(
                email     = "gavin@example.com",
                password  = "password123",
                emailStep = false,
                mode      = AuthMode.SignIn,
                isLoading = true,
            ),
            onEmailChange              = {},
            onPasswordChange           = {},
            onTogglePasswordVisibility = {},
            onContinueEmail            = {},
            onUseDifferentAccount      = {},
            onSubmit                   = {},
            onGoogleSignIn             = {},
            onAppleSignIn              = {},
            onGetStarted               = {},
        )
    }
}

@Preview
@Composable
internal fun AuthScreenErrorPreview() {
    BasilTheme {
        AuthScreenContent(
            state = AuthUiState(
                email     = "gavin@example.com",
                password  = "wrongpassword",
                emailStep = false,
                mode      = AuthMode.SignIn,
                error     = Res.string.error_auth_failed,
            ),
            onEmailChange              = {},
            onPasswordChange           = {},
            onTogglePasswordVisibility = {},
            onContinueEmail            = {},
            onUseDifferentAccount      = {},
            onSubmit                   = {},
            onGoogleSignIn             = {},
            onAppleSignIn              = {},
            onGetStarted               = {},
        )
    }
}
