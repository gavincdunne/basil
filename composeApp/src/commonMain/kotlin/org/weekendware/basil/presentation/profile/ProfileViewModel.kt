package org.weekendware.basil.presentation.profile

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import basil.composeapp.generated.resources.Res
import basil.composeapp.generated.resources.error_avatar_upload_failed
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.weekendware.basil.data.repository.AvatarRepository
import org.weekendware.basil.data.repository.UserRepository
import org.weekendware.basil.domain.usecase.GetUserUseCase

class ProfileViewModel(
    private val getUser: GetUserUseCase,
    private val avatarRepository: AvatarRepository,
    private val userRepository: UserRepository,
    private val scope: CoroutineScope? = null
) : ViewModel() {

    private val coroutineScope get() = scope ?: viewModelScope

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    init {
        // Observe the users table reactively so that data inserted after initial
        // composition (e.g. on desktop where the SQLite table is seeded async
        // during startup) is picked up automatically.
        userRepository.getAllAsFlow()
            .onEach { users ->
                val user = users.firstOrNull() ?: return@onEach
                _state.update { it.copy(name = user.name, email = user.email) }
                val storedPath = user.avatarUrl ?: return@onEach
                if (storedPath.startsWith("http")) {
                    userRepository.updateAvatarUrl(user.id, null)
                } else {
                    avatarRepository.getSignedUrl(storedPath).onSuccess { signedUrl ->
                        _state.update { it.copy(avatarUrl = signedUrl) }
                    }
                }
            }
            .launchIn(coroutineScope)
    }

    fun onAvatarPicked(imageBytes: ByteArray) {
        val userId = getUser()?.id ?: return
        _state.update { it.copy(pendingAvatarBytes = imageBytes, isUploadingAvatar = true, error = null) }
        coroutineScope.launch {
            avatarRepository.uploadAvatar(userId, imageBytes)
                .onSuccess { path ->
                    userRepository.updateAvatarUrl(userId, path)
                    avatarRepository.getSignedUrl(path)
                        .onSuccess { signedUrl ->
                            _state.update { it.copy(avatarUrl = signedUrl, pendingAvatarBytes = null, isUploadingAvatar = false) }
                        }
                        .onFailure {
                            _state.update { it.copy(pendingAvatarBytes = null, isUploadingAvatar = false) }
                        }
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
}

@Immutable
data class ProfileState(
    val name: String               = "",
    val email: String              = "",
    val avatarUrl: String?         = null,
    val pendingAvatarBytes: ByteArray? = null,
    val isUploadingAvatar: Boolean = false,
    val error: StringResource?     = null
)
