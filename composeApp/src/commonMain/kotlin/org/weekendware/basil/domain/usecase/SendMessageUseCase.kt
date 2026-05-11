package org.weekendware.basil.domain.usecase

import kotlinx.coroutines.flow.Flow
import org.weekendware.basil.data.repository.ChatRepository
import org.weekendware.basil.domain.model.ChatMessage

/**
 * Sends the current conversation to the AI backend and returns a stream of
 * text deltas that form the assistant's reply.
 *
 * This is a thin delegation layer that keeps the ViewModel decoupled from the
 * repository implementation.
 */
class SendMessageUseCase(private val chatRepository: ChatRepository) {
    operator fun invoke(messages: List<ChatMessage>): Flow<String> =
        chatRepository.streamChat(messages)
}
