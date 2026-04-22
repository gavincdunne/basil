package org.weekendware.basil.presentation.settings

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import org.koin.compose.koinInject
import org.weekendware.basil.presentation.theme.basilColors
import org.weekendware.basil.presentation.theme.basilSpacing

/**
 * The Settings screen, accessible from the top app bar gear icon.
 *
 * Currently a placeholder surface. As settings are defined (units, notifications,
 * theme preference, profile data, etc.) they will be added here as grouped
 * preference rows.
 */
@Composable
fun SettingsScreen() {
    val viewModel = koinInject<SettingsViewModel>()
    val title = viewModel.title.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.basilColors.surfaceVariant
    ) {
        Text(
            text     = title.value,
            modifier = Modifier.padding(MaterialTheme.basilSpacing.xl),
            style    = MaterialTheme.typography.headlineMedium
        )
    }
}
