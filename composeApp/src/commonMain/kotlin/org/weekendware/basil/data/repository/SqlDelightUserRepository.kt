package org.weekendware.basil.data.repository

import org.weekendware.basil.database.BasilDatabase
import org.weekendware.basil.domain.model.User
import orgweekendwarebasil.database.User as UserEntity

/**
 * SQLDelight-backed implementation of [UserRepository].
 *
 * Wraps the generated [BasilDatabase.userQueries] and maps between the
 * database entity and the domain [User] model.
 *
 * @param database The [BasilDatabase] instance injected via Koin.
 */
class SqlDelightUserRepository(private val database: BasilDatabase) : UserRepository {

    override fun getAll(): List<User> =
        database.userQueries.selectAll().executeAsList().map { it.toDomain() }

    override fun insert(id: String, name: String, email: String) =
        database.userQueries.insertUser(id, name, email)

    override fun deleteAll() = database.userQueries.deleteAll()

    // ── Mapping ───────────────────────────────────────────────

    private fun UserEntity.toDomain() = User(id = id, name = name, email = email)
}
