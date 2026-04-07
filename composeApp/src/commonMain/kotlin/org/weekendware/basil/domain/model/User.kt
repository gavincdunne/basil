package org.weekendware.basil.domain.model

/**
 * Represents a Basil user account.
 *
 * @property id    Unique identifier (UUID string).
 * @property name  The user's display name.
 * @property email The user's email address.
 */
data class User(
    val id:    String,
    val name:  String,
    val email: String
)
