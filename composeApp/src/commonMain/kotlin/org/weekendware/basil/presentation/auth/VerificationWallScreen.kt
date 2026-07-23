package org.weekendware.basil.presentation.auth

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Shown when a user has gone 30+ days without verifying their email — the
 * soft-block wall. Blank placeholder pending UI design; Frontend Builder
 * fills this in with the verify/resend/sign-out content.
 */
@Composable
fun VerificationWallScreen(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize())
}
