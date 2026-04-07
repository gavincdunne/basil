package org.weekendware.basil.data.repository

import org.weekendware.basil.database.BasilDatabase
import org.weekendware.basil.domain.model.User
import orgweekendwarebasil.database.User as UserEntity

/**
 * Repository for [User] persistence.
 *
 * Wraps the SQLDelight-generated `UserQueries` and maps between the
 * database entity and the domain [User] model. As user-related features
 * (profile setup, onboarding) are built out, query methods will be added here.
 *
 * @param database The [BasilDatabase] instance injected via Koin.
 */
class UserRepository(private val database: BasilDatabase) {

    /**
     * Returns all users in the database, mapped to [User] domain models.
     *
     * In practice the app currently stores a single user, but the query is
     * kept as `selectAll` to match the schema and remain extensible.
     */
    fun getAll(): List<User> =
        database.userQueries.selectAll().executeAsList().map { it.toDomain() }

    /**
     * Inserts a new user record.
     *
     * @param id    A unique identifier for the user (UUID string recommended).
     * @param name  The user's display name.
     * @param email The user's email address.
     */
    fun insert(id: String, name: String, email: String) =
        database.userQueries.insertUser(id, name, email)

    /**
     * Deletes all user records. Used during development and reset flows.
     *
     * **Caution:** This is destructive and permanent.
     */
    fun deleteAll() = database.userQueries.deleteAll()

    // ── Mapping ───────────────────────────────────────────────

    private fun UserEntity.toDomain() = User(id = id, name = name, email = email)
}
