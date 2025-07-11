package org.weekendware.basil.presentation.dashboard

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions

object DashboardTab : Tab {

    override val options: TabOptions
        @Composable get() {
            return TabOptions(
                index = 0u,
                title = "Home",
                icon = rememberVectorPainter(Icons.Default.Home)
            )
        }

    @Composable
    override fun Content() {
        DashboardScreen.Content()
    }
}