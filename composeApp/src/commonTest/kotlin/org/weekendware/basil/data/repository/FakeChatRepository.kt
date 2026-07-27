package org.weekendware.basil.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.weekendware.basil.domain.model.ChatMessage

/**
 * Test double for [ChatRepository]. Set [streamChatFlow] before invoking the
 * ViewModel under test to control what [streamChat] emits — a plain success
 * flow, a flow of deltas, or a flow that throws to exercise error handling.
 */
class FakeChatRepository : ChatRepository {

    var streamChatFlow: Flow<String> = flowOf()

    override fun streamChat(messages: List<ChatMessage>): Flow<String> = streamChatFlow
}
