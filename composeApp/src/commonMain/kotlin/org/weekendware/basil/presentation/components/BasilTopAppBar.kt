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
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.tab.Tab
import org.weekendware.basil.presentation.settings.SettingsScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BasilTopAppBar(
    rootNavigator: Navigator,
    currentScreen: cafe.adriel.voyager.core.screen.Screen
) {
    val isRootScreen = currentScreen is Tab
    val title = when (currentScreen) {
        is SettingsScreen -> "Settings"
        else -> ""
    }

    CenterAlignedTopAppBar(
        title = { Text(title) },
        navigationIcon = {
            if (!isRootScreen) {
                IconButton(onClick = { rootNavigator.pop() }) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        actions = {
            if (isRootScreen) {
                IconButton(onClick = {
                    rootNavigator.push(SettingsScreen)
                }) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings")
                }
            }
        }
    )
}
