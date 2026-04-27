package org.weekendware.basil

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import org.koin.compose.viewmodel.koinViewModel
import org.weekendware.basil.presentation.auth.AuthScreen
import org.weekendware.basil.presentation.chat.ChatScreen
import org.weekendware.basil.presentation.components.BasilBottomBar
import org.weekendware.basil.presentation.components.BasilTopAppBar
import org.weekendware.basil.presentation.dashboard.DashboardScreen
import org.weekendware.basil.presentation.profile.ProfileScreen
import org.weekendware.basil.presentation.session.SessionState
import org.weekendware.basil.presentation.session.SessionViewModel
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
 * Observes [SessionViewModel] to decide which graph to display:
 * - [SessionState.Loading]         — blank surface while the SDK restores a stored session.
 * - [SessionState.Unauthenticated] — [AuthScreen] only; no scaffold, no back stack.
 * - [SessionState.Authenticated]   — full app scaffold with [NavHost].
 *
 * Auth and main-app navigation are kept in separate sub-trees so the
 * back stack can never return to the sign-in screen from inside the app.
 */
@Composable
fun App() {
    BasilTheme {
        val sessionViewModel = koinViewModel<SessionViewModel>()
        val sessionState by sessionViewModel.state.collectAsState()

        when (sessionState) {
            SessionState.Loading -> {
                // Blank surface — avoids flashing auth or app UI while the SDK
                // resolves the stored session on cold start.
                Box(modifier = Modifier.fillMaxSize())
            }

            SessionState.Unauthenticated -> {
                AuthScreen()
            }

            SessionState.Authenticated -> {
                MainApp()
            }
        }
    }
}

/**
 * The main application scaffold, shown only when the user is authenticated.
 *
 * Contains the [NavHost] for all primary destinations and the top/bottom bars.
 */
@Composable
private fun MainApp() {
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
