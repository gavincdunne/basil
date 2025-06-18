package org.weekendware.basil.presentation.profile

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions

object ProfileTab : Tab {

    override val options: TabOptions
        @Composable get() {
            return TabOptions(
                index = 0u,
                title = "Profile",
                icon = rememberVectorPainter(Icons.Default.Person)
            )
        }

    @Composable
    override fun Content() {
        ProfileScreen.Content()
    }
}