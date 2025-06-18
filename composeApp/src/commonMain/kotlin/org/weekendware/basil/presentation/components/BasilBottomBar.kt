package org.weekendware.basil.presentation.components

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabNavigator

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
                onClick = { tabNavigator.current = tab },
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
