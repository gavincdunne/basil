package org.weekendware.basil.data.repository

import kotlinx.coroutines.flow.Flow
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
     * Returns a [Flow] that emits the current user list and re-emits whenever
     * the database changes. Use this in ViewModels that need to react to inserts
     * that happen after initial composition (e.g. on desktop where the SQLite
     * users table is populated asynchronously during startup).
     */
    fun getAllAsFlow(): Flow<List<User>>

    /**
     * Inserts a new user record.
     *
     * @param id    A unique identifier for the user (UUID string recommended).
     * @param name  The user's display name.
     * @param email The user's email address.
     */
    fun insert(id: String, name: String, email: String)

    /**
     * Persists the avatar URL for [userId]. Pass null to clear the avatar.
     */
    fun updateAvatarUrl(userId: String, url: String?)

    /**
     * Deletes all user records. Used during development and reset flows.
     *
     * **Caution:** This is destructive and permanent.
     */
    fun deleteAll()
}
