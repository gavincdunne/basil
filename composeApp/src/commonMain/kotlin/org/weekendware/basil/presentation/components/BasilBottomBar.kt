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
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import basil.composeapp.generated.resources.Res
import basil.composeapp.generated.resources.nav_chat
import basil.composeapp.generated.resources.nav_home
import basil.composeapp.generated.resources.nav_profile
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.weekendware.basil.ROUTE_CHAT
import org.weekendware.basil.ROUTE_HOME
import org.weekendware.basil.ROUTE_PROFILE

private data class BottomNavItem(
    val route:    String,
    val labelRes: StringResource,
    val icon:     ImageVector
)

private val tabItems = listOf(
    BottomNavItem(ROUTE_HOME,    Res.string.nav_home,    Icons.Default.Home),
    BottomNavItem(ROUTE_PROFILE, Res.string.nav_profile, Icons.Default.Person),
    BottomNavItem(ROUTE_CHAT,    Res.string.nav_chat,    Icons.Default.EnergySavingsLeaf)
)

/**
 * The bottom navigation bar for the Basil app.
 *
 * Renders a [NavigationBar] item for each of the three main tabs. Tapping an
 * item navigates to that tab's destination, preserving state and avoiding
 * back-stack duplication via [NavController.navigate] with [launchSingleTop].
 *
 * @param navController The [NavController] used for navigation events.
 * @param currentRoute  The currently-active route, used to highlight the
 *   selected item.
 */
@Composable
fun BasilBottomBar(navController: NavController, currentRoute: String?) {
    NavigationBar {
        tabItems.forEach { item ->
            val label = stringResource(item.labelRes)
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick  = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState    = true
                    }
                },
                icon  = { Icon(item.icon, contentDescription = label) },
                label = { Text(label) }
            )
        }
    }
}
