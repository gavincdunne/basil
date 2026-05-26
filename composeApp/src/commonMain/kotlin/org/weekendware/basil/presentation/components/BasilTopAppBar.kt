package org.weekendware.basil.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import basil.composeapp.generated.resources.Res
import basil.composeapp.generated.resources.cd_close
import basil.composeapp.generated.resources.cd_settings
import basil.composeapp.generated.resources.screen_settings
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.weekendware.basil.AppRoute
import org.weekendware.basil.presentation.theme.BasilTheme

/**
 * The top app bar used across all Basil screens.
 *
 * Behaviour adapts based on the currently-active route:
 * - **Tab screens** (Home, Profile, Chat): shows a settings gear icon in the
 *   trailing actions area. Tapping it calls [onSettingsClick].
 * - **Stack screens** (Settings): shows a close icon in the leading navigation
 *   slot. Tapping it calls [onBackClick].
 *
 * @param currentRoute    The currently-active route string.
 * @param onSettingsClick Called when the settings gear is tapped on tab screens.
 * @param onBackClick     Called when the close icon is tapped on stack screens.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BasilTopAppBar(
    currentRoute: String?,
    onSettingsClick: () -> Unit,
    onBackClick: () -> Unit
) {
    val isTabScreen = currentRoute == null || currentRoute in AppRoute.tabRoutes
    val title = if (currentRoute == AppRoute.Settings.route) stringResource(Res.string.screen_settings) else ""

    CenterAlignedTopAppBar(
        title = { Text(title) },
        navigationIcon = {
            if (!isTabScreen) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.Close, contentDescription = stringResource(Res.string.cd_close))
                }
            }
        },
        actions = {
            if (isTabScreen) {
                IconButton(onClick = onSettingsClick) {
                    Icon(Icons.Default.Settings, contentDescription = stringResource(Res.string.cd_settings))
                }
            }
        }
    )
}

// ─────────────────────────────────────────────────────────────
// Previews
// ─────────────────────────────────────────────────────────────

@Preview
@Composable
internal fun BasilTopAppBarTabScreenPreview() {
    BasilTheme {
        BasilTopAppBar(
            currentRoute    = AppRoute.Home.route,
            onSettingsClick = {},
            onBackClick     = {}
        )
    }
}

@Preview
@Composable
internal fun BasilTopAppBarSettingsScreenPreview() {
    BasilTheme {
        BasilTopAppBar(
            currentRoute    = AppRoute.Settings.route,
            onSettingsClick = {},
            onBackClick     = {}
        )
    }
}
