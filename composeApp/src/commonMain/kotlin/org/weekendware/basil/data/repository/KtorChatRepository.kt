package org.weekendware.basil.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.preparePost
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.utils.io.readUTF8Line
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.weekendware.basil.BuildKonfig
import org.weekendware.basil.domain.model.ChatMessage

/**
 * [ChatRepository] implementation that streams the assistant's reply from the
 * basil-chat-api backend over Server-Sent Events (SSE).
 *
 * The backend proxies requests to Anthropic and forwards the raw SSE stream.
 * This repository parses [content_block_delta] events and emits each text
 * fragment as a [String] so callers can accumulate them into a full reply.
 *
 * @param httpClient A configured Ktor [HttpClient] shared across the app.
 */
class KtorChatRepository(private val httpClient: HttpClient) : ChatRepository {

    /**
     * A lenient JSON parser that ignores unknown fields from the SSE stream.
     * Anthropic's SSE format contains many event types; we only need the
     * text deltas.
     */
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * POST the conversation to the chat API and stream each text delta as it
     * arrives. The flow completes when the SSE stream closes.
     *
     * @throws Exception if the HTTP request fails or the server returns an
     *   error status.
     */
    override fun streamChat(messages: List<ChatMessage>): Flow<String> = flow {
        val requestBody = ChatRequest(
            messages = messages.map { ApiMessage(role = it.role, content = it.content) }
        )

        // `preparePost` + `execute` keeps the response open so we can stream
        // the body without buffering the entire reply in memory.
        httpClient.preparePost("${BuildKonfig.CHAT_API_URL}/chat") {
            header(HttpHeaders.Authorization, "Bearer ${BuildKonfig.CHAT_API_KEY}")
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }.execute { response: HttpResponse ->
            val channel = response.bodyAsChannel()

            // SSE lines are in the format: "data: <json payload>"
            // A blank line separates events. "[DONE]" signals end of stream.
            while (!channel.isClosedForRead) {
                val line = channel.readUTF8Line() ?: break
                if (!line.startsWith("data: ")) continue

                val payload = line.removePrefix("data: ").trim()
                if (payload == "[DONE]") break

                // Parse the event and emit the text delta if present.
                // Ignoring parse failures is intentional — the stream may
                // contain non-delta event types (ping, message_start, etc.)
                // that we do not need to act on.
                val event = runCatching { json.decodeFromString<SseEvent>(payload) }.getOrNull()
                event?.delta?.text?.takeIf { it.isNotEmpty() }?.let { emit(it) }
            }
        }
    }

    // ── Private serialisation types ───────────────────────────

    /** Request body sent to `POST /chat`. */
    @Serializable
    private data class ChatRequest(val messages: List<ApiMessage>)

    /** A single turn in the conversation as understood by the chat API. */
    @Serializable
    private data class ApiMessage(val role: String, val content: String)

    /**
     * A single Server-Sent Event from the Anthropic SSE stream.
     *
     * Only [content_block_delta] events carry a [delta] with text; all other
     * types are ignored.
     */
    @Serializable
    private data class SseEvent(
        val type: String = "",
        val delta: TextDelta? = null,
    )

    /**
     * The delta payload inside a [content_block_delta] event.
     *
     * @property type Always `"text_delta"` for text content.
     * @property text The incremental text fragment to append to the reply.
     */
    @Serializable
    private data class TextDelta(
        val type: String = "",
        val text: String = "",
    )
}
