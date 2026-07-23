package org.weekendware.basil.presentation.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/** Which screen [AuthFlowScreen] currently shows. */
private sealed interface AuthDestination {
    data object SignIn : AuthDestination
    data class ResetRequest(val email: String) : AuthDestination
}

/**
 * Owns local navigation between the sign-in screen and the password-reset
 * request flow — the same "plain composable state, no nav library" pattern
 * [org.weekendware.basil.AuthenticatedRoot] uses for tab switching. This is
 * the entry point [org.weekendware.basil.App] should call instead of
 * [AuthScreen] directly.
 *
 * [NewPasswordScreen] (AUTH-07) is deliberately not part of this flow — it's
 * reached via a deep link, not in-app navigation, and that wiring doesn't
 * exist yet.
 *
 * @param onGetStarted Forwarded to [AuthScreen] — see its documentation.
 */
@Composable
fun AuthFlowScreen(onGetStarted: () -> Unit = {}) {
    var destination by remember { mutableStateOf<AuthDestination>(AuthDestination.SignIn) }

    when (val dest = destination) {
        AuthDestination.SignIn -> AuthScreen(
            onGetStarted     = onGetStarted,
            onForgotPassword = { email -> destination = AuthDestination.ResetRequest(email) },
        )
        is AuthDestination.ResetRequest -> ResetPasswordScreen(
            initialEmail = dest.email,
            onBack       = { destination = AuthDestination.SignIn },
        )
    }
}
