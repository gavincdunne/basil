package org.weekendware.basil.presentation.profile

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import org.koin.compose.viewmodel.koinViewModel

/**
 * The user Profile screen.
 *
 * Currently a placeholder. This screen will allow the user to configure
 * their health profile, including:
 * - Diabetes type and diagnosis date
 * - Insulin type(s) and typical doses
 * - Target blood glucose range
 * - Personal preferences (units, notification settings)
 */
@Composable
fun ProfileScreen() {
    val viewModel = koinViewModel<ProfileViewModel>()
    val title = viewModel.title.collectAsState()
    Text(title.value)
}
