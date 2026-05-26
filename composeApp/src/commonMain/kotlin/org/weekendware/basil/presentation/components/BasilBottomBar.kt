package org.weekendware.basil.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EnergySavingsLeaf
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import basil.composeapp.generated.resources.Res
import basil.composeapp.generated.resources.nav_chat
import basil.composeapp.generated.resources.nav_home
import basil.composeapp.generated.resources.nav_profile
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.weekendware.basil.AppRoute
import org.weekendware.basil.presentation.theme.BasilTheme

private data class BottomNavItem(
    val route:    String,
    val labelRes: StringResource,
    val icon:     ImageVector
)

private val tabItems = listOf(
    BottomNavItem(AppRoute.Home.route,    Res.string.nav_home,    Icons.Default.Home),
    BottomNavItem(AppRoute.Profile.route, Res.string.nav_profile, Icons.Default.Person),
    BottomNavItem(AppRoute.Chat.route,    Res.string.nav_chat,    Icons.Default.EnergySavingsLeaf)
)

/**
 * The bottom navigation bar for the Basil app.
 *
 * Renders a [NavigationBar] item for each of the three main tabs. Tapping an
 * item calls [onNavigate] with the destination route; the caller is responsible
 * for the actual navigation logic (single-top, state restoration, etc.).
 *
 * @param currentRoute  The currently-active route, used to highlight the selected item.
 * @param onNavigate    Called with the target route when a tab is tapped.
 */
@Composable
fun BasilBottomBar(currentRoute: String?, onNavigate: (String) -> Unit) {
    NavigationBar {
        tabItems.forEach { item ->
            val label = stringResource(item.labelRes)
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick  = { onNavigate(item.route) },
                icon     = { Icon(item.icon, contentDescription = label) },
                label    = { Text(label) }
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Previews
// ─────────────────────────────────────────────────────────────

@Preview
@Composable
internal fun BasilBottomBarHomePreview() {
    BasilTheme {
        BasilBottomBar(currentRoute = AppRoute.Home.route, onNavigate = {})
    }
}

@Preview
@Composable
internal fun BasilBottomBarProfilePreview() {
    BasilTheme {
        BasilBottomBar(currentRoute = AppRoute.Profile.route, onNavigate = {})
    }
}

@Preview
@Composable
internal fun BasilBottomBarChatPreview() {
    BasilTheme {
        BasilBottomBar(currentRoute = AppRoute.Chat.route, onNavigate = {})
    }
}
