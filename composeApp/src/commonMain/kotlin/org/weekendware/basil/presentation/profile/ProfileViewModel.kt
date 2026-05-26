package org.weekendware.basil.presentation.profile

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import basil.composeapp.generated.resources.Res
import basil.composeapp.generated.resources.error_avatar_upload_failed
import basil.composeapp.generated.resources.error_profile_save_failed
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.weekendware.basil.data.repository.AvatarRepository
import org.weekendware.basil.data.repository.UserRepository
import org.weekendware.basil.domain.usecase.GetBgTargetsUseCase
import org.weekendware.basil.domain.usecase.GetUserUseCase
import org.weekendware.basil.domain.usecase.SetBgTargetsUseCase

/**
 * ViewModel for [ProfileScreen].
 *
 * Loads the current user and their BG targets on init, and exposes
 * an edit/save flow for updating targets and display name. Handles
 * avatar upload and removal via [AvatarRepository].
 */
class ProfileViewModel(
    private val getUser: GetUserUseCase,
    private val getBgTargets: GetBgTargetsUseCase,
    private val setBgTargets: SetBgTargetsUseCase,
    private val avatarRepository: AvatarRepository,
    private val userRepository: UserRepository,
    private val scope: CoroutineScope? = null
) : ViewModel() {

    private val coroutineScope get() = scope ?: viewModelScope

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    init {
        load()
    }

    private fun load() {
        val user    = getUser()
        val targets = getBgTargets()
        _state.update {
            it.copy(
                name         = user?.name ?: "",
                email        = user?.email ?: "",
                avatarUrl    = user?.avatarUrl,
                targetBgLow  = formatTarget(targets.low),
                targetBgHigh = formatTarget(targets.high)
            )
        }
    }

    fun onEditClick() {
        _state.update { it.copy(isEditing = true, error = null) }
    }

    fun onCancelClick() {
        load()
        _state.update { it.copy(isEditing = false, error = null) }
    }

    fun onNameChange(value: String) {
        _state.update { it.copy(name = value) }
    }

    fun onTargetLowChange(value: String) {
        _state.update { it.copy(targetBgLow = value) }
    }

    fun onTargetHighChange(value: String) {
        _state.update { it.copy(targetBgHigh = value) }
    }

    fun onSaveClick() {
        val s = _state.value
        val low  = s.targetBgLow.toDoubleOrNull()
        val high = s.targetBgHigh.toDoubleOrNull()

        if (low == null || high == null || low >= high) {
            _state.update { it.copy(error = Res.string.error_profile_save_failed) }
            return
        }

        runCatching { setBgTargets(low, high) }
            .onSuccess { _state.update { it.copy(isEditing = false, error = null) } }
            .onFailure { _state.update { it.copy(error = Res.string.error_profile_save_failed) } }
    }

    fun onAvatarPicked(imageBytes: ByteArray) {
        val userId = getUser()?.id ?: return
        // Show the picked image immediately before the upload completes
        _state.update { it.copy(pendingAvatarBytes = imageBytes, isUploadingAvatar = true, error = null) }
        coroutineScope.launch {
            avatarRepository.uploadAvatar(userId, imageBytes)
                .onSuccess { url ->
                    userRepository.updateAvatarUrl(userId, url)
                    _state.update { it.copy(avatarUrl = url, pendingAvatarBytes = null, isUploadingAvatar = false) }
                }
                .onFailure {
                    _state.update {
                        it.copy(pendingAvatarBytes = null, isUploadingAvatar = false, error = Res.string.error_avatar_upload_failed)
                    }
                }
        }
    }

    fun onRemoveAvatar() {
        val userId = getUser()?.id ?: return
        _state.update { it.copy(isUploadingAvatar = true, error = null) }
        coroutineScope.launch {
            avatarRepository.deleteAvatar(userId)
                .onSuccess {
                    userRepository.updateAvatarUrl(userId, null)
                    _state.update { it.copy(avatarUrl = null, isUploadingAvatar = false) }
                }
                .onFailure {
                    _state.update {
                        it.copy(isUploadingAvatar = false, error = Res.string.error_avatar_upload_failed)
                    }
                }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    // ── Helpers ───────────────────────────────────────────────

    private fun formatTarget(value: Double): String =
        if (value == value.toLong().toDouble()) value.toLong().toString()
        else value.toString()
}

@Immutable
data class ProfileState(
    val name: String               = "",
    val email: String              = "",
    val avatarUrl: String?         = null,
    val pendingAvatarBytes: ByteArray? = null,
    val isUploadingAvatar: Boolean = false,
    val targetBgLow: String        = "",
    val targetBgHigh: String       = "",
    val isEditing: Boolean         = false,
    val error: StringResource?     = null
)
