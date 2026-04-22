package org.weekendware.basil.presentation.chat

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * ViewModel for [ChatScreen].
 *
 * Currently a placeholder. As the AI assistant feature is implemented,
 * this ViewModel will manage the conversation message list, handle
 * LLM API requests, and surface loading/error states.
 *
 * @param coroutineScope Scope for async operations. Override in tests with a [kotlinx.coroutines.test.TestScope].
 */
class ChatViewModel(
    @Suppress("UnusedPrivateMember")
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
) {
    private val _title = MutableStateFlow("Basil")

    /** The screen title, used as a placeholder until the chat UI is built. */
    val title: StateFlow<String> = _title
}
