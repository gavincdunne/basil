package org.weekendware.basil.presentation.chat

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import basil.composeapp.generated.resources.Res
import basil.composeapp.generated.resources.error_chat_failed
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.weekendware.basil.domain.model.ChatMessage
import org.weekendware.basil.domain.usecase.SendMessageUseCase
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * UI state for [ChatScreen].
 *
 * @property messages   The full conversation history, ordered oldest-first.
 *                      The last message may be an assistant reply that is still
 *                      streaming ([ChatMessage.isStreaming] == true).
 * @property input      The current value of the text input field.
 * @property isLoading  True while waiting for the first streaming token after
 *                      the user sends a message (shows a typing indicator).
 * @property error      Non-null when the last send attempt failed; cleared by
 *                      calling [ChatViewModel.clearError].
 */
@Stable
data class ChatState(
    val messages: List<ChatMessage> = emptyList(),
    val input: String = "",
    val isLoading: Boolean = false,
    val error: StringResource? = null,
)

/**
 * ViewModel for [ChatScreen].
 *
 * Manages the conversation history and orchestrates streaming replies from the
 * AI backend via [SendMessageUseCase]. Each text delta emitted by the use case
 * is appended to the last assistant message in [state].
 *
 * @param sendMessage    Use case that streams the assistant's reply.
 * @param coroutineScope Scope for async operations. Defaults to [viewModelScope]
 *                       when null. Override in tests with a [TestScope].
 */
class ChatViewModel(
    private val sendMessage: SendMessageUseCase,
    coroutineScope: CoroutineScope? = null,
) : ViewModel() {

    private val scope = coroutineScope ?: viewModelScope

    private val _state = MutableStateFlow(ChatState())

    /** The current UI state. Observed by [ChatScreen] to drive recomposition. */
    val state: StateFlow<ChatState> = _state

    /** Updates the text input field as the user types. */
    fun onInputChange(text: String) {
        _state.update { it.copy(input = text) }
    }

    /**
     * Sends the current [ChatState.input] to the AI backend and streams the
     * reply into the conversation.
     *
     * Does nothing if the input is blank. On a network or API error the
     * incomplete assistant message is removed and [ChatState.error] is set.
     */
    @OptIn(ExperimentalUuidApi::class)
    fun sendMessage() {
        val text = _state.value.input.trim()
        if (text.isBlank()) return

        // Append the user message and clear the input immediately so the UI
        // feels responsive before the network call begins.
        val userMessage = ChatMessage(
            id = Uuid.random().toString(),
            role = "user",
            content = text,
        )
        _state.update { it.copy(messages = it.messages + userMessage, input = "") }

        // Capture the history to send to the API — this does NOT include the
        // assistant placeholder, which is a UI-only concept.
        val historyForApi = _state.value.messages

        // Reserve a slot in the message list for the streaming reply.
        val assistantId = Uuid.random().toString()
        val placeholder = ChatMessage(
            id = assistantId,
            role = "assistant",
            content = "",
            isStreaming = true,
        )
        _state.update { it.copy(messages = it.messages + placeholder, isLoading = true) }

        scope.launch {
            try {
                sendMessage(historyForApi).collect { delta ->
                    // Each delta is appended to the assistant message's content.
                    // isLoading is cleared on the first token so the typing
                    // indicator gives way to actual text.
                    _state.update { state ->
                        state.copy(
                            isLoading = false,
                            messages = state.messages.map { msg ->
                                if (msg.id == assistantId) msg.copy(content = msg.content + delta)
                                else msg
                            },
                        )
                    }
                }
                // Flow completed without error — mark the assistant message done.
                _state.update { state ->
                    state.copy(
                        isLoading = false,
                        messages = state.messages.map { msg ->
                            if (msg.id == assistantId) msg.copy(isStreaming = false)
                            else msg
                        },
                    )
                }
            } catch (_: Exception) {
                // Remove the incomplete reply and surface the error to the UI.
                _state.update { state ->
                    state.copy(
                        messages = state.messages.filter { it.id != assistantId },
                        isLoading = false,
                        error = Res.string.error_chat_failed,
                    )
                }
            }
        }
    }

    /** Dismisses the current error banner. */
    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    /**
     * Wipes all messages, input, and error state.
     *
     * Called on sign-out so that no PHI from the previous session persists
     * in memory when a new user logs in on the same device.
     */
    fun clearHistory() {
        _state.update { ChatState() }
    }
}
