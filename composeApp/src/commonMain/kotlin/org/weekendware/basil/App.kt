package org.weekendware.basil

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.navigator.tab.*
import org.weekendware.basil.presentation.dashboard.DashboardTab
import org.weekendware.basil.presentation.profile.ProfileTab
import org.weekendware.basil.presentation.settings.SettingsTab

@Composable
fun App() {
    val tabs = listOf(DashboardTab, ProfileTab, SettingsTab)

    TabNavigator(DashboardTab) { tabNavigator ->
        Scaffold(
            bottomBar = {
                NavigationBar {
                    tabs.forEach { tab ->
                        val isSelected = tabNavigator.current == tab

                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { tabNavigator.current = tab },
                            icon = {
                                tab.options.icon?.let { icon ->
                                    Icon(
                                        painter = icon,
                                        contentDescription = tab.options.title
                                    )
                                }
                            },
                            label = { Text(tab.options.title) }
                        )
                    }
                }
            }
        ) { innerPadding ->
            Surface(modifier = Modifier.padding(innerPadding)) {
                CurrentTab()
            }
        }
    }
}
