package org.weekendware.basil.presentation.chat

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import cafe.adriel.voyager.core.screen.Screen
import org.koin.compose.koinInject

object ChatScreen : Screen {
    @Composable
    override fun Content() {
        val viewModel = koinInject<ChatViewModel>()
        val title = viewModel.title.collectAsState()

        Text(title.value)
    }
}