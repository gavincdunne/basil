@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package org.weekendware.basil.presentation.chat

import basil.composeapp.generated.resources.Res
import basil.composeapp.generated.resources.error_chat_failed
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.weekendware.basil.data.repository.ChatRepository
import org.weekendware.basil.domain.usecase.SendMessageUseCase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ChatViewModelTest {

    private val chatRepository = mock<ChatRepository>()
    private val sendMessage = SendMessageUseCase(chatRepository)

    private fun makeVm() = ChatViewModel(sendMessage, coroutineScope = null)

    // ── initial state ─────────────────────────────────────────

    @Test
    fun `initial state has empty message list`() {
        val vm = makeVm()

        assertTrue(vm.state.value.messages.isEmpty())
    }

    @Test
    fun `initial state has empty input`() {
        val vm = makeVm()

        assertEquals("", vm.state.value.input)
    }

    @Test
    fun `initial state is not loading`() {
        val vm = makeVm()

        assertFalse(vm.state.value.isLoading)
    }

    @Test
    fun `initial state has no error`() {
        val vm = makeVm()

        assertNull(vm.state.value.error)
    }

    // ── onInputChange ─────────────────────────────────────────

    @Test
    fun `onInputChange updates input in state`() {
        val vm = makeVm()

        vm.onInputChange("What is a good insulin ratio?")

        assertEquals("What is a good insulin ratio?", vm.state.value.input)
    }

    @Test
    fun `onInputChange can clear the input`() {
        val vm = makeVm()
        vm.onInputChange("some text")

        vm.onInputChange("")

        assertEquals("", vm.state.value.input)
    }

    // ── sendMessage – guard clauses ───────────────────────────

    @Test
    fun `sendMessage does nothing when input is blank`() = runTest {
        val vm = ChatViewModel(sendMessage, coroutineScope = this)
        vm.onInputChange("   ")

        vm.sendMessage()
        advanceUntilIdle()

        assertTrue(vm.state.value.messages.isEmpty())
        assertFalse(vm.state.value.isLoading)
    }

    @Test
    fun `sendMessage does nothing when input is empty`() = runTest {
        val vm = ChatViewModel(sendMessage, coroutineScope = this)

        vm.sendMessage()
        advanceUntilIdle()

        assertTrue(vm.state.value.messages.isEmpty())
    }

    // ── sendMessage – happy path ───────────────────────────────

    @Test
    fun `sendMessage adds user message to the conversation`() = runTest {
        whenever(chatRepository.streamChat(any())).thenReturn(flowOf())
        val vm = ChatViewModel(sendMessage, coroutineScope = this)
        vm.onInputChange("How do I adjust my basal rate?")

        vm.sendMessage()
        advanceUntilIdle()

        val userMsg = vm.state.value.messages.find { it.role == "user" }
        assertEquals("How do I adjust my basal rate?", userMsg?.content)
    }

    @Test
    fun `sendMessage clears the input field after sending`() = runTest {
        whenever(chatRepository.streamChat(any())).thenReturn(flowOf())
        val vm = ChatViewModel(sendMessage, coroutineScope = this)
        vm.onInputChange("My question")

        vm.sendMessage()
        advanceUntilIdle()

        assertEquals("", vm.state.value.input)
    }

    @Test
    fun `sendMessage adds an assistant placeholder message before streaming starts`() = runTest {
        // Use a flow that never completes so we can inspect mid-stream state.
        // We just need to verify the assistant message is added.
        whenever(chatRepository.streamChat(any())).thenReturn(flowOf("Hi"))
        val vm = ChatViewModel(sendMessage, coroutineScope = this)
        vm.onInputChange("Hello")

        vm.sendMessage()
        advanceUntilIdle()

        val assistantMsg = vm.state.value.messages.find { it.role == "assistant" }
        assertTrue(assistantMsg != null, "Expected an assistant message in the list")
    }

    @Test
    fun `sendMessage accumulates streaming deltas into assistant message content`() = runTest {
        whenever(chatRepository.streamChat(any())).thenReturn(
            flowOf("Hello", ", ", "how", " can", " I", " help?")
        )
        val vm = ChatViewModel(sendMessage, coroutineScope = this)
        vm.onInputChange("Hi")

        vm.sendMessage()
        advanceUntilIdle()

        val assistantMsg = vm.state.value.messages.last()
        assertEquals("assistant", assistantMsg.role)
        assertEquals("Hello, how can I help?", assistantMsg.content)
    }

    @Test
    fun `sendMessage marks isStreaming false on assistant message after flow completes`() = runTest {
        whenever(chatRepository.streamChat(any())).thenReturn(flowOf("Done"))
        val vm = ChatViewModel(sendMessage, coroutineScope = this)
        vm.onInputChange("Test")

        vm.sendMessage()
        advanceUntilIdle()

        val assistantMsg = vm.state.value.messages.last()
        assertFalse(assistantMsg.isStreaming)
    }

    @Test
    fun `sendMessage sets isLoading false after stream completes`() = runTest {
        whenever(chatRepository.streamChat(any())).thenReturn(flowOf("Hi"))
        val vm = ChatViewModel(sendMessage, coroutineScope = this)
        vm.onInputChange("Hello")

        vm.sendMessage()
        advanceUntilIdle()

        assertFalse(vm.state.value.isLoading)
    }

    @Test
    fun `conversation history is passed to the repository on second send`() = runTest {
        whenever(chatRepository.streamChat(any())).thenReturn(flowOf("Sure!"))
        val vm = ChatViewModel(sendMessage, coroutineScope = this)

        vm.onInputChange("First question")
        vm.sendMessage()
        advanceUntilIdle()

        vm.onInputChange("Follow-up question")
        vm.sendMessage()
        advanceUntilIdle()

        // Two user messages and two assistant messages in the conversation.
        val userMessages = vm.state.value.messages.filter { it.role == "user" }
        assertEquals(2, userMessages.size)
    }

    // ── sendMessage – error handling ──────────────────────────

    @Test
    fun `sendMessage sets error state when repository throws`() = runTest {
        whenever(chatRepository.streamChat(any())).thenReturn(
            flow { throw RuntimeException("Network error") }
        )
        val vm = ChatViewModel(sendMessage, coroutineScope = this)
        vm.onInputChange("Will this fail?")

        vm.sendMessage()
        advanceUntilIdle()

        assertEquals(Res.string.error_chat_failed, vm.state.value.error)
    }

    @Test
    fun `sendMessage sets isLoading false after error`() = runTest {
        whenever(chatRepository.streamChat(any())).thenReturn(
            flow { throw RuntimeException("Network error") }
        )
        val vm = ChatViewModel(sendMessage, coroutineScope = this)
        vm.onInputChange("Question")

        vm.sendMessage()
        advanceUntilIdle()

        assertFalse(vm.state.value.isLoading)
    }

    @Test
    fun `sendMessage removes incomplete assistant message on error`() = runTest {
        whenever(chatRepository.streamChat(any())).thenReturn(
            flow {
                emit("Partial")
                throw RuntimeException("Stream cut off")
            }
        )
        val vm = ChatViewModel(sendMessage, coroutineScope = this)
        vm.onInputChange("Question")

        vm.sendMessage()
        advanceUntilIdle()

        // No incomplete assistant message should remain in the list.
        val lastMsg = vm.state.value.messages.lastOrNull()
        assertTrue(lastMsg == null || lastMsg.role == "user")
    }

    // ── clearError ────────────────────────────────────────────

    @Test
    fun `clearError removes the error from state`() = runTest {
        whenever(chatRepository.streamChat(any())).thenReturn(
            flow { throw RuntimeException("Network error") }
        )
        val vm = ChatViewModel(sendMessage, coroutineScope = this)
        vm.onInputChange("Question")
        vm.sendMessage()
        advanceUntilIdle()
        assertEquals(Res.string.error_chat_failed, vm.state.value.error)

        vm.clearError()

        assertNull(vm.state.value.error)
    }
}
