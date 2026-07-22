package org.weekendware.basil.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
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

    override fun getAllAsFlow(): Flow<List<User>> =
        database.userQueries.selectAll().asFlow().mapToList(Dispatchers.IO).map { list ->
            list.map { it.toDomain() }
        }

    override fun insert(id: String, name: String, email: String) =
        database.userQueries.insertUser(id, name, email)

    override fun updateAvatarUrl(userId: String, url: String?) =
        database.userQueries.updateAvatarUrl(url, userId)

    override fun deleteAll() = database.userQueries.deleteAll()

    // ── Mapping ───────────────────────────────────────────────

    private fun UserEntity.toDomain() = User(
        id = id,
        name = name,
        email = email,
        avatarUrl = avatar_url,
    )
}
