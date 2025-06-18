package org.weekendware.basil.presentation.profile

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ProfileViewModel {
    private val _title = MutableStateFlow("Profile")
    val title: StateFlow<String> = _title
}