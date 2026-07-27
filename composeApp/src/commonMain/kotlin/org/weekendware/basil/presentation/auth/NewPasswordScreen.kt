package org.weekendware.basil.presentation.auth

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
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
import basil.composeapp.generated.resources.app_name
import basil.composeapp.generated.resources.auth_field_confirm_password
import basil.composeapp.generated.resources.auth_field_new_password
import basil.composeapp.generated.resources.auth_new_password_subtitle
import basil.composeapp.generated.resources.auth_new_password_title
import basil.composeapp.generated.resources.auth_set_new_password
import basil.composeapp.generated.resources.cd_hide_password
import basil.composeapp.generated.resources.cd_show_password
import basil.composeapp.generated.resources.error_auth_failed
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import org.weekendware.basil.presentation.theme.BasilPalette
import org.weekendware.basil.presentation.theme.BasilTheme
import org.weekendware.basil.presentation.theme.BasilTokens
import org.weekendware.basil.presentation.theme.authHeadingStyle
import org.weekendware.basil.presentation.theme.authHeroWordmarkStyle

/**
 * New-password screen (AUTH-07) — reached via a password-reset deep link,
 * not from in-app navigation, so there is no back affordance.
 *
 * Not wired into app routing yet: reaching this screen depends on
 * [org.weekendware.basil.data.repository.DeepLinkHandler] actually
 * triggering navigation on a successful deep-link exchange, which is a
 * separate, not-yet-built event bridge from the data layer to the UI
 * layer. This composable is complete and tested standalone, ready for
 * that wiring once it exists.
 */
@Composable
fun NewPasswordScreen() {
    val viewModel = koinViewModel<NewPasswordViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()

    NewPasswordScreenContent(
        state                      = state,
        onPasswordChange           = viewModel::onPasswordChange,
        onConfirmPasswordChange    = viewModel::onConfirmPasswordChange,
        onTogglePasswordVisibility = viewModel::onTogglePasswordVisibility,
        onSubmit                   = viewModel::submit,
    )
}

@Composable
fun NewPasswordScreenContent(
    state:                      NewPasswordUiState,
    onPasswordChange:           (String) -> Unit,
    onConfirmPasswordChange:    (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onSubmit:                   () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize().background(BasilPalette.Cream)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(BasilPalette.Sage600)
                .statusBarsPadding()
                .padding(top = 20.dp, start = 20.dp, end = 20.dp, bottom = 24.dp),
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
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 20.dp, bottom = 28.dp),
        ) {
            Text(
                text  = stringResource(Res.string.auth_new_password_title),
                style = authHeadingStyle(),
                color = BasilPalette.AuthNearBlack,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text  = stringResource(Res.string.auth_new_password_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = BasilPalette.AuthFieldText,
            )
            Spacer(Modifier.height(22.dp))
            PasswordFieldLabel(stringResource(Res.string.auth_field_new_password))
            Spacer(Modifier.height(6.dp))
            PasswordField(
                value                = state.password,
                onValueChange        = onPasswordChange,
                isVisible            = state.isPasswordVisible,
                onToggleVisibility   = onTogglePasswordVisibility,
                borderColorOverride  = if (state.passwordStrength == PasswordStrength.Strong) BasilPalette.AuthStrengthGreen else null,
            )
            StrengthIndicator(strength = state.passwordStrength, requirements = state.passwordRequirements)
            Spacer(Modifier.height(14.dp))
            PasswordFieldLabel(stringResource(Res.string.auth_field_confirm_password))
            Spacer(Modifier.height(6.dp))
            PasswordField(
                value              = state.confirmPassword,
                onValueChange      = onConfirmPasswordChange,
                isVisible          = state.isPasswordVisible,
                onToggleVisibility = onTogglePasswordVisibility,
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
                    onClick  = onSubmit,
                    enabled  = state.canSubmit,
                    shape    = RoundedCornerShape(50),
                    colors   = ButtonDefaults.buttonColors(
                        containerColor         = BasilPalette.Sage600,
                        contentColor            = Color.White,
                        disabledContainerColor  = BasilPalette.AuthButtonMuted,
                        disabledContentColor    = Color.White,
                    ),
                    modifier = Modifier.fillMaxWidth().height(BasilTokens.ButtonHeight),
                ) {
                    Text(text = stringResource(Res.string.auth_set_new_password), style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
private fun PasswordFieldLabel(text: String) {
    Text(
        text          = text.uppercase(),
        style         = MaterialTheme.typography.labelSmall,
        color         = BasilPalette.AuthFieldText,
        fontWeight    = FontWeight.SemiBold,
    )
}

@Composable
private fun PasswordField(
    value:               String,
    onValueChange:       (String) -> Unit,
    isVisible:           Boolean,
    onToggleVisibility:  () -> Unit,
    borderColorOverride: Color? = null,
) {
    OutlinedTextField(
        value                = value,
        onValueChange        = onValueChange,
        singleLine           = true,
        visualTransformation = if (isVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions      = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
        keyboardActions      = KeyboardActions(),
        trailingIcon = {
            IconButton(onClick = onToggleVisibility) {
                Icon(
                    imageVector        = if (isVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = stringResource(if (isVisible) Res.string.cd_hide_password else Res.string.cd_show_password),
                    tint               = BasilPalette.AuthFieldText,
                )
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor   = BasilPalette.Cream,
            unfocusedContainerColor = BasilPalette.Cream,
            focusedBorderColor      = borderColorOverride ?: BasilPalette.Sage600,
            unfocusedBorderColor    = borderColorOverride ?: BasilPalette.AuthFieldBorder,
            cursorColor             = BasilPalette.Sage600,
        ),
        shape    = RoundedCornerShape(BasilTokens.AuthFieldCorner),
        modifier = Modifier.fillMaxWidth().height(BasilTokens.AuthFieldHeight),
    )
}

@Composable
private fun StrengthIndicator(strength: PasswordStrength, requirements: List<PasswordRequirement>) {
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
        Column {
            requirements.forEach { requirement ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                    Box(
                        modifier = if (requirement.met) {
                            Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(BasilPalette.Sage600)
                        } else {
                            Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .border(1.5.dp, BasilPalette.AuthFieldBorder, CircleShape)
                        },
                        contentAlignment = Alignment.Center,
                    ) {
                        if (requirement.met) {
                            Text(text = "✓", style = MaterialTheme.typography.labelSmall, color = Color.White)
                        }
                    }
                    Spacer(Modifier.width(7.dp))
                    Text(
                        text  = stringResource(requirement.label),
                        style = MaterialTheme.typography.labelSmall,
                        color = BasilPalette.AuthFieldText,
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Previews
// ─────────────────────────────────────────────────────────────

@Preview
@Composable
internal fun NewPasswordEmptyPreview() {
    BasilTheme {
        NewPasswordScreenContent(
            state                      = NewPasswordUiState(),
            onPasswordChange           = {},
            onConfirmPasswordChange    = {},
            onTogglePasswordVisibility = {},
            onSubmit                   = {},
        )
    }
}
