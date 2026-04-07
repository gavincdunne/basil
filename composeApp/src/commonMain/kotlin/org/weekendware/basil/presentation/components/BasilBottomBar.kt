package org.weekendware.basil.presentation.components

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabNavigator

/**
 * The bottom navigation bar for the Basil app.
 *
 * Renders a [NavigationBar] item for each tab in [tabs]. The active item is
 * highlighted automatically based on [TabNavigator.current]. Tapping an item
 * switches the visible tab without pushing a new screen onto the back stack.
 *
 * @param tabNavigator The [TabNavigator] whose [TabNavigator.current] controls
 *   the selected state and receives tab-switch events.
 * @param tabs The ordered list of [Tab]s to display as navigation items.
 *   Each tab must provide a non-null [Tab.options] with a title and icon.
 */
@Composable
fun BasilBottomBar(
    tabNavigator: TabNavigator,
    tabs: List<Tab>
) {
    NavigationBar {
        tabs.forEach { tab ->
            val isSelected = tabNavigator.current == tab
            NavigationBarItem(
                selected = isSelected,
                onClick  = { tabNavigator.current = tab },
                icon = {
                    tab.options.icon?.let {
                        Icon(painter = it, contentDescription = tab.options.title)
                    }
                },
                label = { Text(tab.options.title) }
            )
        }
    }
}
