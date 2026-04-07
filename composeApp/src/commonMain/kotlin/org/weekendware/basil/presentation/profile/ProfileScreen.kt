package org.weekendware.basil.presentation.profile

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import cafe.adriel.voyager.core.screen.Screen
import org.koin.compose.koinInject

/**
 * The user Profile screen.
 *
 * Currently a placeholder. This screen will allow the user to configure
 * their health profile, including:
 * - Diabetes type and diagnosis date
 * - Insulin type(s) and typical doses
 * - Target blood glucose range
 * - Personal preferences (units, notification settings)
 *
 * Navigated to via [ProfileTab] in the bottom navigation bar.
 */
object ProfileScreen : Screen {

    /**
     * Renders the profile content.
     *
     * Placeholder: displays the screen title from [ProfileViewModel].
     */
    @Composable
    override fun Content() {
        val viewModel = koinInject<ProfileViewModel>()
        val title = viewModel.title.collectAsState()
        Text(title.value)
    }
}
