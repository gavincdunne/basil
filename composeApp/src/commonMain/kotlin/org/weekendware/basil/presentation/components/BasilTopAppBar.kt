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
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.tab.Tab
import org.weekendware.basil.presentation.settings.SettingsScreen

/**
 * The top app bar used across all Basil screens.
 *
 * Behaviour adapts based on the currently-displayed screen:
 * - **Tab screens** (e.g. Dashboard, Profile, Chat): shows a settings gear
 *   icon in the trailing actions area. Tapping it pushes [SettingsScreen]
 *   onto [rootNavigator].
 * - **Stack screens** (e.g. Settings): shows a close icon in the leading
 *   navigation slot. Tapping it pops the current screen.
 *
 * The title slot is populated for named stack screens (currently [SettingsScreen]);
 * tab screens leave it empty to keep the top bar clean.
 *
 * @param rootNavigator The root Voyager [Navigator] used for push/pop operations.
 * @param currentScreen The screen that is currently visible, used to determine
 *   which icons and title to display.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BasilTopAppBar(
    rootNavigator: Navigator,
    currentScreen: Screen
) {
    val isTab = currentScreen is Tab
    val title = when (currentScreen) {
        is SettingsScreen -> "Settings"
        else              -> ""
    }

    CenterAlignedTopAppBar(
        title = { Text(title) },
        navigationIcon = {
            if (!isTab) {
                IconButton(onClick = { rootNavigator.pop() }) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        actions = {
            if (isTab) {
                IconButton(onClick = { rootNavigator.push(SettingsScreen) }) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings")
                }
            }
        }
    )
}
