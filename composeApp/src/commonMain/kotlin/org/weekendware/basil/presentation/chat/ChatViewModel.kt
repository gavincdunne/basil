package org.weekendware.basil.presentation.chat

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * ViewModel for [ChatScreen].
 *
 * Currently a placeholder. As the AI assistant feature is implemented,
 * this ViewModel will manage the conversation message list, handle
 * LLM API requests, and surface loading/error states.
 */
class ChatViewModel {
    private val _title = MutableStateFlow("Basil")

    /** The screen title, used as a placeholder until the chat UI is built. */
    val title: StateFlow<String> = _title
}
