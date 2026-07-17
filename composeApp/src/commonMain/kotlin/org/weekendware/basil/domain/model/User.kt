package org.weekendware.basil.domain.model

/**
 * Represents a Basil user account.
 *
 * @property id        Unique identifier (UUID string).
 * @property name      The user's display name.
 * @property email     The user's email address.
 * @property avatarUrl Storage path of the user's profile picture (e.g. `userId/avatar`),
 *                     or null if no photo has been set. Never a public URL — the avatars
 *                     bucket is private. Use [AvatarRepository.getSignedUrl] to obtain a
 *                     displayable URL.
 */
data class User(
    val id: String,
    val name: String,
    val email: String,
    val avatarUrl: String? = null,
)
