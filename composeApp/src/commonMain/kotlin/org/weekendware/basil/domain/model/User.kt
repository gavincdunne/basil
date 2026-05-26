package org.weekendware.basil.domain.model

/**
 * Represents a Basil user account.
 *
 * @property id        Unique identifier (UUID string).
 * @property name      The user's display name.
 * @property email     The user's email address.
 * @property avatarUrl Public URL of the user's profile picture in Supabase
 *                     Storage, or null if no photo has been set.
 */
data class User(
    val id: String,
    val name: String,
    val email: String,
    val avatarUrl: String? = null,
)
