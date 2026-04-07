package org.weekendware.basil.presentation.chat

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import cafe.adriel.voyager.core.screen.Screen
import org.koin.compose.koinInject

/**
 * The Basil AI assistant screen.
 *
 * Currently a placeholder. This screen will host the conversational
 * LLM-powered interface where users can ask questions about their
 * logged data, receive context-aware suggestions, and get plain-language
 * explanations of trends.
 *
 * Navigated to via [ChatTab] in the bottom navigation bar.
 */
object ChatScreen : Screen {

    /**
     * Renders the chat screen content.
     *
     * Placeholder: displays the screen title from [ChatViewModel].
     */
    @Composable
    override fun Content() {
        val viewModel = koinInject<ChatViewModel>()
        val title = viewModel.title.collectAsState()
        Text(title.value)
    }
}
