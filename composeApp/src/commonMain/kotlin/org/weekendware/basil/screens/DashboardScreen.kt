@file:OptIn(ExperimentalMaterial3Api::class)

package org.weekendware.basil.screens

import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator

object DashboardScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Basil") },
                    actions = {
                        IconButton(onClick = { navigator?.push(SettingsScreen) }) {
                            Icon(Icons.Filled.Settings, contentDescription = "Settings")
                        }
                    }
                )
            }
        ) {
            // Your main content here
        }
    }
}
