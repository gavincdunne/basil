package org.weekendware.basil.presentation.chat

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions

object ChatTab : Tab {

    override val options: TabOptions
        @Composable get() {
            return TabOptions(
                index = 0u,
                title = "Chat",
                icon = rememberVectorPainter(Icons.Default.ChatBubble)
            )
        }

    @Composable
    override fun Content() {
        ChatScreen.Content()
    }
}