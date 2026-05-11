package org.weekendware.basil.domain.model

/**
 * A single turn in a conversation with the Basil AI assistant.
 *
 * @property id          Stable unique identifier used as a `LazyColumn` key.
 * @property role        `"user"` or `"assistant"`.
 * @property content     The message text. Empty while the assistant is streaming.
 * @property isStreaming `true` while the assistant response is still arriving.
 *                       Used to show a typing indicator in the UI.
 */
data class ChatMessage(
    val id: String,
    val role: String,
    val content: String,
    val isStreaming: Boolean = false,
)
