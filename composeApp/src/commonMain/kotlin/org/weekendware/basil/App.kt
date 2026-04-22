package org.weekendware.basil

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import org.weekendware.basil.presentation.chat.ChatScreen
import org.weekendware.basil.presentation.components.BasilBottomBar
import org.weekendware.basil.presentation.components.BasilTopAppBar
import org.weekendware.basil.presentation.dashboard.DashboardScreen
import org.weekendware.basil.presentation.profile.ProfileScreen
import org.weekendware.basil.presentation.settings.SettingsScreen
import org.weekendware.basil.presentation.theme.BasilTheme

const val ROUTE_HOME = "home"
const val ROUTE_PROFILE = "profile"
const val ROUTE_CHAT = "chat"
const val ROUTE_SETTINGS = "settings"
val tabRoutes = setOf(ROUTE_HOME, ROUTE_PROFILE, ROUTE_CHAT)

/**
 * Root composable for the Basil application.
 *
 * Sets up the top-level navigation structure using Compose Multiplatform Navigation:
 * - A [NavHost] manages all destinations: Home, Profile, Chat, and Settings.
 * - A [Scaffold] wraps the content with [BasilTopAppBar] and [BasilBottomBar].
 *   The bottom bar is hidden on the Settings screen.
 *
 * All UI is wrapped in [BasilTheme] so every composable in the tree has access
 * to the Basil color, typography, shape, and spacing tokens.
 */
@Composable
fun App() {
    BasilTheme {
        val navController = rememberNavController()
        val backStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = backStackEntry?.destination?.route

        Scaffold(
            topBar = {
                BasilTopAppBar(
                    navController = navController,
                    currentRoute = currentRoute
                )
            },
            bottomBar = {
                if (currentRoute in tabRoutes) {
                    BasilBottomBar(
                        navController = navController,
                        currentRoute = currentRoute
                    )
                }
            }
        ) { innerPadding ->
            Surface(modifier = Modifier.padding(innerPadding)) {
                NavHost(navController = navController, startDestination = ROUTE_HOME) {
                    composable(ROUTE_HOME)     { DashboardScreen() }
                    composable(ROUTE_PROFILE)  { ProfileScreen() }
                    composable(ROUTE_CHAT)     { ChatScreen() }
                    composable(ROUTE_SETTINGS) { SettingsScreen() }
                }
            }
        }
    }
}
