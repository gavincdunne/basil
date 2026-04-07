package org.weekendware.basil.presentation.chat

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EnergySavingsLeaf
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions

/**
 * The Basil AI assistant tab in the bottom navigation bar.
 *
 * Uses the leaf icon to reflect the Basil brand identity. Tab index 2
 * places it as the rightmost item in the navigation bar.
 */
object ChatTab : Tab {

    /** Tab metadata — title, icon, and index used by [BasilBottomBar]. */
    override val options: TabOptions
        @Composable get() = TabOptions(
            index = 2u,
            title = "Basil",
            icon  = rememberVectorPainter(Icons.Default.EnergySavingsLeaf)
        )

    @Composable
    override fun Content() {
        ChatScreen.Content()
    }
}
