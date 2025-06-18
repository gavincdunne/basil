package org.weekendware.basil.presentation.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions

object SettingsTab : Tab {

    override val options: TabOptions
        @Composable get() {
            return TabOptions(
                index = 0u,
                title = "Settings",
                icon = rememberVectorPainter(Icons.Default.Settings)
            )
        }

    @Composable
    override fun Content() {
        SettingsScreen.Content()
    }
}