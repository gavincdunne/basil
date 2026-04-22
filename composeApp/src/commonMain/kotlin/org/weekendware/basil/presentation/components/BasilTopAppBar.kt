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
import androidx.navigation.NavController
import org.weekendware.basil.ROUTE_SETTINGS
import org.weekendware.basil.TAB_ROUTES

/**
 * The top app bar used across all Basil screens.
 *
 * Behaviour adapts based on the currently-active route:
 * - **Tab screens** (Home, Profile, Chat): shows a settings gear icon in the
 *   trailing actions area. Tapping it navigates to [ROUTE_SETTINGS].
 * - **Stack screens** (Settings): shows a close icon in the leading navigation
 *   slot. Tapping it pops back to the previous destination.
 *
 * @param navController The [NavController] used for navigation events.
 * @param currentRoute  The currently-active route string.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BasilTopAppBar(navController: NavController, currentRoute: String?) {
    val isTabScreen = currentRoute == null || currentRoute in TAB_ROUTES
    val title = if (currentRoute == ROUTE_SETTINGS) "Settings" else ""

    CenterAlignedTopAppBar(
        title = { Text(title) },
        navigationIcon = {
            if (!isTabScreen) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        actions = {
            if (isTabScreen) {
                IconButton(onClick = { navController.navigate(ROUTE_SETTINGS) }) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings")
                }
            }
        }
    )
}
