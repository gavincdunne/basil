package org.weekendware.basil.data.repository

import kotlinx.coroutines.flow.Flow
import org.weekendware.basil.domain.model.ChatMessage

/**
 * Sends a conversation to the AI backend and streams the response.
 *
 * Each emission from [streamChat] is an incremental text delta (a token or
 * small chunk) from the assistant. The caller is responsible for accumulating
 * deltas into a full message string.
 */
interface ChatRepository {
    /**
     * Streams the assistant's reply for the given [messages] history.
     *
     * @param messages The full conversation so far, ending with a user message.
     * @return A cold [Flow] that emits text deltas and completes when the
     *         assistant finishes its response.
     * @throws Exception if the API request fails.
     */
    fun streamChat(messages: List<ChatMessage>): Flow<String>
}
