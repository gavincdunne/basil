package org.weekendware.basil

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import org.koin.compose.viewmodel.koinViewModel
import org.weekendware.basil.presentation.auth.AuthScreen
import org.weekendware.basil.presentation.chat.ChatScreen
import org.weekendware.basil.presentation.chat.ChatViewModel
import org.weekendware.basil.presentation.components.BasilBottomBar
import org.weekendware.basil.presentation.components.BasilTopAppBar
import org.weekendware.basil.presentation.dashboard.DashboardScreen
import org.weekendware.basil.presentation.profile.ProfileScreen
import org.weekendware.basil.presentation.session.SessionState
import org.weekendware.basil.presentation.session.SessionViewModel
import org.weekendware.basil.presentation.settings.SettingsScreen
import org.weekendware.basil.presentation.splash.SplashScreen
import org.weekendware.basil.presentation.theme.BasilTheme

/**
 * Returns true when the splash screen should be shown.
 *
 * The splash remains visible in two cases:
 * 1. The session is still loading — we have not yet heard from Supabase.
 * 2. The session has resolved but the splash fade animation has not completed —
 *    we hold the screen briefly to avoid a jarring cut.
 *
 * @param sessionState   The current authentication session state.
 * @param splashFadeDone Whether the fade-out animation has completed.
 */
fun shouldShowSplash(sessionState: SessionState, splashFadeDone: Boolean): Boolean =
    sessionState == SessionState.Loading || !splashFadeDone

/** Compile-safe navigation destinations for the app. */
sealed class AppRoute(val route: String) {
    data object Home     : AppRoute("home")
    data object Profile  : AppRoute("profile")
    data object Chat     : AppRoute("chat")
    data object Settings : AppRoute("settings")

    companion object {
        /** Routes that show the bottom navigation bar. */
        val tabRoutes = setOf(Home.route, Profile.route, Chat.route)
    }
}

/**
 * Root composable for the Basil application.
 *
 * Observes [SessionViewModel] to decide which graph to display:
 * - [SessionState.Loading] — [SplashScreen] while the SDK restores a stored session.
 *   Once the session resolves AND the splash fade completes, the appropriate
 *   screen is shown.
 * - [SessionState.Unauthenticated] — [AuthScreen]; no scaffold, no back stack.
 * - [SessionState.Authenticated]   — full app scaffold with [NavHost].
 *
 * Auth and main-app navigation are kept in separate sub-trees so the back stack
 * can never return to the sign-in screen from inside the app.
 */
@Composable
fun App() {
    BasilTheme {
        val sessionViewModel = koinViewModel<SessionViewModel>()
        val sessionState by sessionViewModel.state.collectAsState()
        val chatViewModel = koinViewModel<ChatViewModel>()

        // Clear all in-memory chat history whenever the session ends.
        // This ensures no PHI from a previous session persists in memory
        // when the user signs out or a different user signs in on the same device.
        LaunchedEffect(sessionState) {
            if (sessionState == SessionState.Unauthenticated) {
                chatViewModel.clearHistory()
            }
        }

        // Track whether the splash fade animation has finished. We wait for
        // both: the session to resolve *and* the splash to fade out before
        // showing the next screen, preventing any jarring cut.
        var splashDone by remember { mutableStateOf(false) }

        val showSplash = shouldShowSplash(sessionState, splashDone)

        if (showSplash) {
            SplashScreen(onFadeComplete = { splashDone = true })
        } else {
            when (sessionState) {
                SessionState.Unauthenticated -> AuthScreen()
                SessionState.Authenticated   -> MainApp()
                SessionState.Loading         -> Box(Modifier.fillMaxSize()) // unreachable
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
            if (currentRoute in AppRoute.tabRoutes) {
                BasilBottomBar(
                    navController = navController,
                    currentRoute = currentRoute
                )
            }
        }
    ) { innerPadding ->
        Surface(modifier = Modifier.padding(innerPadding)) {
            NavHost(navController = navController, startDestination = AppRoute.Home.route) {
                composable(AppRoute.Home.route)     { DashboardScreen() }
                composable(AppRoute.Profile.route)  { ProfileScreen() }
                composable(AppRoute.Chat.route)     { ChatScreen() }
                composable(AppRoute.Settings.route) { SettingsScreen() }
            }
        }
    }
}
