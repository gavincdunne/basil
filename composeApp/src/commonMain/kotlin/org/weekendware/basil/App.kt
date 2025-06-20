package org.weekendware.basil

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import org.weekendware.basil.presentation.chat.ChatTab
import org.weekendware.basil.presentation.components.BasilBottomBar
import org.weekendware.basil.presentation.components.BasilTopAppBar
import org.weekendware.basil.presentation.dashboard.DashboardTab
import org.weekendware.basil.presentation.profile.ProfileTab

@Composable
fun App() {
    val tabs = listOf(DashboardTab, ProfileTab, ChatTab)

    Navigator(DashboardTab) { rootNavigator ->
        TabNavigator(DashboardTab) { tabNavigator ->
            val currentScreen = rootNavigator.lastItem

            Scaffold(
                topBar = {
                    BasilTopAppBar(
                        rootNavigator = rootNavigator,
                        currentScreen = currentScreen
                    )
                },
                bottomBar = {
                    if (currentScreen is Tab) {
                        BasilBottomBar(tabNavigator = tabNavigator, tabs = tabs)
                    }
                }
            ) { innerPadding ->
                Surface(modifier = Modifier.padding(innerPadding)) {
                    when (currentScreen) {
                        is Tab -> {
                            tabNavigator.current.Content()
                        }

                        else -> {
                            currentScreen.Content()
                        }
                    }
                }
            }
        }
    }
}
