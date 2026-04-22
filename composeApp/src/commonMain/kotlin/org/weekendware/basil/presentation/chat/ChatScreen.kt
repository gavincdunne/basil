package org.weekendware.basil.presentation.chat

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import org.koin.compose.viewmodel.koinViewModel

/**
 * The Basil AI assistant screen.
 *
 * Currently a placeholder. This screen will host the conversational
 * LLM-powered interface where users can ask questions about their
 * logged data, receive context-aware suggestions, and get plain-language
 * explanations of trends.
 */
@Composable
fun ChatScreen() {
    val viewModel = koinViewModel<ChatViewModel>()
    val title = viewModel.title.collectAsState()
    Text(title.value)
}
