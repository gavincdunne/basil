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
import org.weekendware.basil.ROUTE_CHAT
import org.weekendware.basil.ROUTE_HOME
import org.weekendware.basil.ROUTE_PROFILE

private data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

private val TAB_ITEMS = listOf(
    BottomNavItem(ROUTE_HOME,    "Home",   Icons.Default.Home),
    BottomNavItem(ROUTE_PROFILE, "Profile", Icons.Default.Person),
    BottomNavItem(ROUTE_CHAT,    "Basil",  Icons.Default.EnergySavingsLeaf)
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
        TAB_ITEMS.forEach { item ->
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
                icon  = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) }
            )
        }
    }
}
