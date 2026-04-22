package org.weekendware.basil.presentation.profile

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * ViewModel for [ProfileScreen].
 *
 * Currently a placeholder. As the profile feature is implemented, this
 * ViewModel will expose and manage the user's health profile data,
 * loading it from the database via a `UserRepository` and coordinating
 * saves when the user edits their profile.
 *
 * @param coroutineScope Scope for async operations. Override in tests with a [kotlinx.coroutines.test.TestScope].
 */
class ProfileViewModel(
    @Suppress("UnusedPrivateMember")
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
) {
    private val _title = MutableStateFlow("Profile")

    /** The screen title, used as a placeholder until the profile UI is built. */
    val title: StateFlow<String> = _title
}
