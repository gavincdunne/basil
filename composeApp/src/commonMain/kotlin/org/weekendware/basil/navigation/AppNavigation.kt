package org.weekendware.basil.navigation

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.Navigator
import org.weekendware.basil.screens.DashboardScreen

@Composable
fun AppNavigation() {
    Navigator(DashboardScreen)
}