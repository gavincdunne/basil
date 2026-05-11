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
 * ### HIPAA hardening
 * - **HTTPS-only**: throws [IllegalStateException] if [BuildKonfig.CHAT_API_URL]
 *   is not an `https://` URL, preventing accidental transmission of PHI over
 *   plain HTTP.
 * - **Context window cap**: only the most recent [MAX_MESSAGES_PER_REQUEST]
 *   messages are sent per request (minimum-necessary principle). The client
 *   holds the full history locally but the server never receives the entire
 *   lifetime conversation.
 * - **No HTTP logging**: the Ktor [httpClient] must not have a logging plugin
 *   installed; request bodies contain PHI and must never be written to logs.
 *
 * @param httpClient A configured Ktor [HttpClient] shared across the app.
 */
class KtorChatRepository(private val httpClient: HttpClient) : ChatRepository {

    companion object {
        /**
         * Maximum number of messages included in a single API request.
         *
         * Must not exceed the server-side `MAX_MESSAGES` limit in basil-chat-api.
         * Keeping only a sliding window of recent messages enforces the
         * minimum-necessary principle — the full conversation history never
         * leaves the device in one payload.
         */
        const val MAX_MESSAGES_PER_REQUEST = 40
    }

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
     * @throws IllegalStateException if the configured API URL does not use HTTPS.
     * @throws Exception if the HTTP request fails or the server returns an
     *   error status.
     */
    override fun streamChat(messages: List<ChatMessage>): Flow<String> = flow {
        // HTTPS-only guard: PHI must never travel over an unencrypted channel.
        // This will catch misconfigured `local.properties` before any data leaves
        // the device. An http:// URL in production is a critical configuration error.
        require(BuildKonfig.CHAT_API_URL.startsWith("https://")) {
            "CHAT_API_URL must use HTTPS to protect PHI in transit. " +
            "Got: ${BuildKonfig.CHAT_API_URL}"
        }

        // Context window cap: send only the most recent messages to limit the
        // volume of PHI per request (minimum-necessary principle).
        val window = messages.takeLast(MAX_MESSAGES_PER_REQUEST)

        val requestBody = ChatRequest(
            messages = window.map { ApiMessage(role = it.role, content = it.content) }
        )

        // `preparePost` + `execute` keeps the response open so we can stream
        // the body without buffering the entire reply in memory.
        httpClient.preparePost("${BuildKonfig.CHAT_API_URL.trimEnd('/')}/chat") {
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
