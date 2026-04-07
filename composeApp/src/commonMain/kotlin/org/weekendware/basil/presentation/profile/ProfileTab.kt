package org.weekendware.basil.presentation.profile

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions

/**
 * The Profile tab in the bottom navigation bar.
 *
 * Tab index 1, positioned between Home and Basil. Delegates its content
 * to [ProfileScreen], which will eventually display the user's health profile,
 * insulin settings, and personal preferences.
 */
object ProfileTab : Tab {

    /** Tab metadata — title, icon, and index used by [BasilBottomBar]. */
    override val options: TabOptions
        @Composable get() = TabOptions(
            index = 1u,
            title = "Profile",
            icon  = rememberVectorPainter(Icons.Default.Person)
        )

    @Composable
    override fun Content() {
        ProfileScreen.Content()
    }
}
