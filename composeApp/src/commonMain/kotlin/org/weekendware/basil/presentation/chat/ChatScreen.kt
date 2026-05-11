package org.weekendware.basil.presentation.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import basil.composeapp.generated.resources.Res
import basil.composeapp.generated.resources.cd_send_message
import basil.composeapp.generated.resources.chat_empty_subtitle
import basil.composeapp.generated.resources.chat_empty_title
import basil.composeapp.generated.resources.chat_input_placeholder
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.weekendware.basil.domain.model.ChatMessage
import org.weekendware.basil.presentation.theme.basilSpacing

/**
 * The Basil AI assistant screen.
 *
 * Renders a scrollable conversation thread and a fixed input bar at the bottom.
 * New messages are appended to the bottom and the list auto-scrolls to keep
 * the latest content visible.
 *
 * While an assistant reply is streaming, a typing indicator is shown. On
 * error, a Snackbar surfaces the problem and is dismissed when the user
 * acknowledges it.
 */
@Composable
fun ChatScreen() {
    val viewModel = koinViewModel<ChatViewModel>()
    val state by viewModel.state.collectAsState()
    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Scroll to the bottom whenever the message list grows.
    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.lastIndex)
        }
    }

    // Show the error snackbar and clear the error when dismissed.
    val error = state.error
    LaunchedEffect(error) {
        if (error != null) {
            snackbarHostState.showSnackbar(message = "Something went wrong. Please try again.")
            viewModel.clearError()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
        ) {
            // ── Message list ──────────────────────────────────────
            Box(modifier = Modifier.weight(1f)) {
                if (state.messages.isEmpty()) {
                    ChatEmptyState(modifier = Modifier.align(Alignment.Center))
                } else {
                    val spacing = MaterialTheme.basilSpacing
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = spacing.md),
                        verticalArrangement = Arrangement.spacedBy(spacing.sm),
                        contentPadding = PaddingValues(
                            top = spacing.md,
                            bottom = spacing.md,
                        ),
                    ) {
                        items(state.messages, key = { it.id }) { message ->
                            MessageBubble(message = message)
                        }

                        // Typing indicator shown while waiting for the first token.
                        if (state.isLoading) {
                            item(key = "typing_indicator") {
                                TypingIndicator()
                            }
                        }
                    }
                }
            }

            // ── Input bar ─────────────────────────────────────────
            ChatInputBar(
                input = state.input,
                onInputChange = viewModel::onInputChange,
                onSend = viewModel::sendMessage,
                isSendEnabled = state.input.isNotBlank() && !state.isLoading,
            )
        }

        // ── Error snackbar ────────────────────────────────────────
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

/**
 * Shown when the conversation has no messages yet.
 */
@Composable
private fun ChatEmptyState(modifier: Modifier = Modifier) {
    val spacing = MaterialTheme.basilSpacing
    Column(
        modifier = modifier.padding(horizontal = spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(Res.string.chat_empty_title),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(spacing.sm))
        Text(
            text = stringResource(Res.string.chat_empty_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * A single message bubble. User messages are right-aligned with the primary
 * container colour; assistant messages are left-aligned with the secondary
 * container colour.
 *
 * If the message is still streaming, a blinking cursor "▋" is appended to
 * signal to the user that more content is on the way.
 */
@Composable
private fun MessageBubble(message: ChatMessage) {
    val isUser = message.role == "user"
    val bubbleColor = if (isUser)
        MaterialTheme.colorScheme.primaryContainer
    else
        MaterialTheme.colorScheme.secondaryContainer
    val textColor = if (isUser)
        MaterialTheme.colorScheme.onPrimaryContainer
    else
        MaterialTheme.colorScheme.onSecondaryContainer
    val alignment = if (isUser) Alignment.End else Alignment.Start
    val bubbleShape = if (isUser) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
    } else {
        RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment,
    ) {
        Surface(
            modifier = Modifier.widthIn(max = 280.dp),
            shape = bubbleShape,
            color = bubbleColor,
            tonalElevation = 1.dp,
        ) {
            Text(
                // Append a block cursor while the reply is still streaming.
                text = if (message.isStreaming) message.content + "▋" else message.content,
                style = MaterialTheme.typography.bodyMedium,
                color = textColor,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            )
        }
    }
}

/**
 * Three-dot indicator shown while [ChatState.isLoading] is true and the
 * assistant has not yet emitted its first token.
 */
@Composable
private fun TypingIndicator() {
    Row(
        modifier = Modifier
            .padding(vertical = 4.dp)
            .clip(
                RoundedCornerShape(
                    topStart = 4.dp, topEnd = 16.dp,
                    bottomStart = 16.dp, bottomEnd = 16.dp,
                )
            )
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(3) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(
                        MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f)
                    )
            )
        }
    }
}

/**
 * The fixed input bar at the bottom of [ChatScreen].
 *
 * The send button and keyboard action are disabled while the previous message
 * is still loading or the input is blank.
 *
 * @param input         The current text field value.
 * @param onInputChange Called when the user edits the text.
 * @param onSend        Called when the user taps send or presses the keyboard
 *                      action button.
 * @param isSendEnabled Whether the send action is active.
 */
@Composable
private fun ChatInputBar(
    input: String,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    isSendEnabled: Boolean,
) {
    val spacing = MaterialTheme.basilSpacing
    Surface(
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 3.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.md, vertical = spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
        ) {
            OutlinedTextField(
                value = input,
                onValueChange = onInputChange,
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text(
                        text = stringResource(Res.string.chat_input_placeholder),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { if (isSendEnabled) onSend() }),
                maxLines = 4,
                shape = RoundedCornerShape(24.dp),
            )

            IconButton(
                onClick = onSend,
                enabled = isSendEnabled,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = stringResource(Res.string.cd_send_message),
                    tint = if (isSendEnabled)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                )
            }
        }
    }
}
