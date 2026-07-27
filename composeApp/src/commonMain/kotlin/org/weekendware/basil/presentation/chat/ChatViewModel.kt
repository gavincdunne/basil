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
import org.weekendware.basil.data.repository.AuthRepository
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
 * @property showVerificationBanner True when the signed-in user's email is
 *   unverified and they haven't dismissed the banner yet this session.
 *   Never true once [org.weekendware.basil.presentation.auth.VerificationWallScreen]
 *   has taken over (that's a separate, non-dismissible screen for the
 *   30-day-and-older case) — this banner is only for users still within
 *   the grace period.
 * @property isResendingVerification True while a resend request is in flight.
 */
@Stable
data class ChatState(
    val messages: List<ChatMessage> = emptyList(),
    val input: String = "",
    val isLoading: Boolean = false,
    val error: StringResource? = null,
    val showVerificationBanner: Boolean = false,
    val isResendingVerification: Boolean = false,
)

/**
 * ViewModel for [ChatScreen].
 *
 * Manages the conversation history and orchestrates streaming replies from the
 * AI backend via [SendMessageUseCase]. Each text delta emitted by the use case
 * is appended to the last assistant message in [state].
 *
 * @param sendMessage     Use case that streams the assistant's reply.
 * @param authRepository  Read once on init to decide [ChatState.showVerificationBanner] —
 *                        this screen is only ever shown to a verified user or one still
 *                        within the 30-day grace period (the wall takes over past that),
 *                        so no re-check against days-since-signup is needed here.
 * @param coroutineScope  Scope for async operations. Defaults to [viewModelScope]
 *                        when null. Override in tests with a [TestScope].
 */
class ChatViewModel(
    private val sendMessage: SendMessageUseCase,
    private val authRepository: AuthRepository,
    coroutineScope: CoroutineScope? = null,
) : ViewModel() {

    private val scope = coroutineScope ?: viewModelScope

    private val _state = MutableStateFlow(
        ChatState(showVerificationBanner = !authRepository.isEmailVerified())
    )

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
        if (text.isBlank() || _state.value.isLoading) return

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

    /**
     * Inserts a greeting as the first assistant message if the conversation is empty.
     * No-ops if messages already exist — safe to call on every recomposition.
     */
    @OptIn(ExperimentalUuidApi::class)
    fun setGreeting(text: String) {
        if (_state.value.messages.isEmpty()) {
            _state.update {
                it.copy(
                    messages = listOf(
                        ChatMessage(id = Uuid.random().toString(), role = "assistant", content = text)
                    )
                )
            }
        }
    }

    /** Dismisses the current error banner. */
    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    /** Dismisses the verification banner for the rest of this session. */
    fun onDismissVerificationBanner() {
        _state.update { it.copy(showVerificationBanner = false) }
    }

    /** Resends the verification email. Does not dismiss the banner — the user does that separately. */
    fun onResendVerification() {
        _state.update { it.copy(isResendingVerification = true) }
        scope.launch {
            authRepository.resendVerificationEmail()
            _state.update { it.copy(isResendingVerification = false) }
        }
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
