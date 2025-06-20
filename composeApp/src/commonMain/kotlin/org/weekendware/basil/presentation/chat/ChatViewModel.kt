package org.weekendware.basil.presentation.chat

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ChatViewModel {
    private val _title = MutableStateFlow("Basil")
    val title: StateFlow<String> = _title
}