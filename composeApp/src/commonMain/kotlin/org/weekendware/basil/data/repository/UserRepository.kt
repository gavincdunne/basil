package org.weekendware.basil.data.repository

import org.weekendware.basil.domain.model.User

/**
 * Contract for [User] persistence.
 *
 * Abstracts the storage mechanism so ViewModels and use cases depend only
 * on this interface, not on any specific database implementation.
 */
interface UserRepository {

    /**
     * Returns all users in the database, mapped to [User] domain models.
     */
    fun getAll(): List<User>

    /**
     * Inserts a new user record.
     *
     * @param id    A unique identifier for the user (UUID string recommended).
     * @param name  The user's display name.
     * @param email The user's email address.
     */
    fun insert(id: String, name: String, email: String)

    /**
     * Deletes all user records. Used during development and reset flows.
     *
     * **Caution:** This is destructive and permanent.
     */
    fun deleteAll()
}
