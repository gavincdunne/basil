package org.weekendware.basil.presentation.settings

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen

object SettingsScreen : Screen {
    @Composable
    override fun Content() {
        Text("Settings Screen")
    }
}