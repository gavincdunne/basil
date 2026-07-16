package org.weekendware.basil.presentation.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import basil.composeapp.generated.resources.Res
import basil.composeapp.generated.resources.cd_profile_avatar
import basil.composeapp.generated.resources.profile_label_email
import basil.composeapp.generated.resources.profile_label_name
import basil.composeapp.generated.resources.profile_pick_photo
import basil.composeapp.generated.resources.profile_placeholder_name
import basil.composeapp.generated.resources.profile_remove_photo
import basil.composeapp.generated.resources.profile_section_account
import coil3.compose.AsyncImage
import com.mohamedrejeb.calf.core.LocalPlatformContext
import com.mohamedrejeb.calf.io.readByteArray
import com.mohamedrejeb.calf.picker.FilePickerFileType
import com.mohamedrejeb.calf.picker.FilePickerSelectionMode
import com.mohamedrejeb.calf.picker.rememberFilePickerLauncher
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import org.weekendware.basil.presentation.theme.BasilTheme
import org.weekendware.basil.presentation.theme.basilSpacing

@Composable
fun ProfileScreen() {
    val viewModel = koinViewModel<ProfileViewModel>()
    val state by viewModel.state.collectAsState()
    ProfileScreenContent(
        state          = state,
        onAvatarPicked = viewModel::onAvatarPicked,
        onRemoveAvatar = viewModel::onRemoveAvatar
    )
}

@Composable
fun ProfileScreenContent(
    state: ProfileState,
    onAvatarPicked: (ByteArray) -> Unit,
    onRemoveAvatar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = MaterialTheme.basilSpacing

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = spacing.lg, vertical = spacing.xl),
        verticalArrangement = Arrangement.spacedBy(spacing.xl)
    ) {
        ProfileHeader(
            name               = state.name,
            email              = state.email,
            avatarUrl          = state.avatarUrl,
            pendingAvatarBytes = state.pendingAvatarBytes,
            isUploading        = state.isUploadingAvatar,
            onAvatarPicked     = onAvatarPicked,
            onRemoveAvatar     = onRemoveAvatar
        )

        ProfileSection(title = stringResource(Res.string.profile_section_account)) {
            ProfileReadOnlyRow(
                label = stringResource(Res.string.profile_label_name),
                value = state.name.ifBlank { stringResource(Res.string.profile_placeholder_name) }
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            ProfileReadOnlyRow(
                label = stringResource(Res.string.profile_label_email),
                value = state.email
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Sub-composables
// ─────────────────────────────────────────────────────────────

private val AvatarSize = 96.dp

@Composable
private fun ProfileHeader(
    name: String,
    email: String,
    avatarUrl: String?,
    pendingAvatarBytes: ByteArray?,
    isUploading: Boolean,
    onAvatarPicked: (ByteArray) -> Unit,
    onRemoveAvatar: () -> Unit
) {
    val spacing = MaterialTheme.basilSpacing
    val coroutineScope = rememberCoroutineScope()
    val platformContext = LocalPlatformContext.current

    val pickerLauncher = rememberFilePickerLauncher(
        type = FilePickerFileType.Image,
        selectionMode = FilePickerSelectionMode.Single
    ) { files ->
        val file = files.firstOrNull() ?: return@rememberFilePickerLauncher
        coroutineScope.launch {
            val bytes = file.readByteArray(platformContext)
            onAvatarPicked(bytes)
        }
    }

    Column(
        modifier            = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacing.sm)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(AvatarSize)
                .clip(CircleShape)
                .clickable(enabled = !isUploading) { pickerLauncher.launch() }
        ) {
            when {
                pendingAvatarBytes != null -> AsyncImage(
                    model              = pendingAvatarBytes,
                    contentDescription = stringResource(Res.string.cd_profile_avatar),
                    contentScale       = ContentScale.Crop,
                    modifier           = Modifier.fillMaxSize()
                )
                avatarUrl != null -> AsyncImage(
                    model              = avatarUrl,
                    contentDescription = stringResource(Res.string.cd_profile_avatar),
                    contentScale       = ContentScale.Crop,
                    modifier           = Modifier.fillMaxSize()
                )
                else -> Surface(
                    modifier = Modifier.fillMaxSize(),
                    color    = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text      = name.initials(),
                        modifier  = Modifier.fillMaxSize().padding(top = 24.dp),
                        textAlign = TextAlign.Center,
                        style     = MaterialTheme.typography.headlineLarge,
                        color     = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            if (!isUploading) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(AvatarSize * 0.35f)
                        .align(Alignment.BottomCenter)
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color    = Color.Black.copy(alpha = 0.45f)
                    ) {}
                    Icon(
                        imageVector        = Icons.Default.AddAPhoto,
                        contentDescription = null,
                        tint               = Color.White,
                        modifier           = Modifier.size(20.dp)
                    )
                }
            }

            if (isUploading) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color    = Color.Black.copy(alpha = 0.45f)
                ) {}
                CircularProgressIndicator(
                    modifier    = Modifier.size(32.dp),
                    color       = Color.White,
                    strokeWidth = 2.5.dp
                )
            }
        }

        if ((pendingAvatarBytes != null || avatarUrl != null) && !isUploading) {
            TextButton(onClick = onRemoveAvatar) {
                Icon(
                    imageVector        = Icons.Default.Close,
                    contentDescription = null,
                    modifier           = Modifier.size(14.dp)
                )
                Text(
                    text  = stringResource(Res.string.profile_remove_photo),
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }

        if (name.isNotBlank()) {
            Text(
                text  = name,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        if (email.isNotBlank()) {
            Text(
                text  = email,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ProfileSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val spacing = MaterialTheme.basilSpacing
    ElevatedCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier            = Modifier.padding(spacing.xl),
            verticalArrangement = Arrangement.spacedBy(spacing.md)
        ) {
            Text(
                text  = title.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
            content()
        }
    }
}

@Composable
private fun ProfileReadOnlyRow(label: String, value: String) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(
            text  = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text  = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun String.initials(): String =
    trim().split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercaseChar().toString() }
        .ifBlank { "?" }

// ─────────────────────────────────────────────────────────────
// Previews
// ─────────────────────────────────────────────────────────────

@Preview
@Composable
internal fun ProfileScreenContentPreview() {
    BasilTheme {
        ProfileScreenContent(
            state          = ProfileState(name = "Gavin Dunne", email = "gavin@weekendware.io"),
            onAvatarPicked = {},
            onRemoveAvatar = {}
        )
    }
}
