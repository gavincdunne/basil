package org.weekendware.basil.presentation.dashboard

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions

/**
 * The Dashboard tab in the bottom navigation bar.
 *
 * Acts as the home/default tab (index 0). Delegates its content to
 * [DashboardScreen], which hosts the glucose log FAB and, eventually,
 * the summary dashboard UI.
 */
object DashboardTab : Tab {

    /** Tab metadata — title, icon, and index used by [BasilBottomBar]. */
    override val options: TabOptions
        @Composable get() = TabOptions(
            index = 0u,
            title = "Home",
            icon  = rememberVectorPainter(Icons.Default.Home)
        )

    @Composable
    override fun Content() {
        DashboardScreen.Content()
    }
}
