package org.weekendware.basil.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.compose.viewmodel.koinViewModel
import org.weekendware.basil.presentation.components.BasilLeaf
import org.weekendware.basil.presentation.theme.BasilPalette

/**
 * Authentication screen, shown when no valid session exists.
 *
 * Layout matches the "Option A — Warm & Grounded" design handoff:
 * - Sage-600 hero band with the leaf mark + "basil" wordmark
 * - Cream background form area below with email/password fields
 *
 * Handles both sign-in and sign-up via a mode toggle. Navigation away from
 * this screen happens automatically when [SessionViewModel] detects a
 * successful authentication.
 */
@Composable
fun AuthScreen() {
    val viewModel = koinViewModel<AuthViewModel>()
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BasilPalette.Cream)
    ) {
        AuthHeroBand()
        AuthForm(state = state, viewModel = viewModel)
    }
}

@Composable
private fun AuthHeroBand() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = BasilPalette.Sage600,
                shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
            )
            .statusBarsPadding()
            .padding(top = 48.dp, bottom = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BasilLeaf(size = 38.dp, fill = Color.White, vein = BasilPalette.Sage800)
        Spacer(Modifier.height(10.dp))
        Text(
            text  = "basil",
            style = MaterialTheme.typography.displaySmall,
            color = Color.White
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text  = "Your T1D companion",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun AuthForm(state: AuthFormState, viewModel: AuthViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 28.dp)
    ) {
        Text(
            text  = if (state.isSignUp) "Create account" else "Welcome back",
            style = MaterialTheme.typography.headlineSmall,
            color = BasilPalette.Stone900
        )
        Spacer(Modifier.height(4.dp))
        val subtitle = if (state.isSignUp) {
            "Sign up to start managing your T1D."
        } else {
            "Sign in to continue to Basil."
        }
        Text(
            text  = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = BasilPalette.Stone500
        )
        Spacer(Modifier.height(22.dp))
        AuthFieldLabel("EMAIL")
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value         = state.email,
            onValueChange = viewModel::onEmailChange,
            singleLine    = true,
            placeholder   = { Text("you@example.com") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction    = ImeAction.Next
            ),
            colors   = authFieldColors(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(14.dp))
        AuthFieldLabel("PASSWORD")
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value                = state.password,
            onValueChange        = viewModel::onPasswordChange,
            singleLine           = true,
            placeholder          = { Text("••••••••") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions      = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction    = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = { viewModel.submit() }),
            colors   = authFieldColors(),
            modifier = Modifier.fillMaxWidth()
        )
        state.error?.let { error ->
            Spacer(Modifier.height(8.dp))
            Text(
                text  = error,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
        Spacer(Modifier.height(22.dp))
        AuthSubmitArea(state = state, viewModel = viewModel)
    }
}

@Composable
private fun AuthFieldLabel(text: String) {
    Text(
        text          = text,
        style         = MaterialTheme.typography.labelSmall,
        color         = BasilPalette.Stone500,
        fontWeight    = FontWeight.SemiBold,
        letterSpacing = 0.8.sp
    )
}

@Composable
private fun AuthSubmitArea(state: AuthFormState, viewModel: AuthViewModel) {
    Column(
        modifier            = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (state.isLoading) {
            CircularProgressIndicator(color = BasilPalette.Sage600)
        } else {
            Button(
                onClick  = viewModel::submit,
                enabled  = state.canSubmit,
                shape    = RoundedCornerShape(50),
                colors   = ButtonDefaults.buttonColors(
                    containerColor = BasilPalette.Sage600,
                    contentColor   = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(
                    text  = if (state.isSignUp) "Create account" else "Sign in",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
        Spacer(Modifier.height(14.dp))
        TextButton(onClick = viewModel::toggleMode) {
            val toggleLabel = if (state.isSignUp) {
                "Already have an account? Sign in"
            } else {
                "New to Basil? Create account"
            }
            Text(
                text  = toggleLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = BasilPalette.Sage600
            )
        }
    }
}

@Composable
private fun authFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor   = BasilPalette.Sage600,
    unfocusedBorderColor = BasilPalette.Stone200,
    focusedLabelColor    = BasilPalette.Sage600,
    cursorColor          = BasilPalette.Sage600
)
