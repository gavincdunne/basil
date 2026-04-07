package org.weekendware.basil.data.repository

import org.weekendware.basil.database.BasilDatabase
import org.weekendware.basil.domain.model.User
import orgweekendwarebasil.database.User as UserEntity

class UserRepository(private val database: BasilDatabase) {

    fun getAll(): List<User> =
        database.userQueries.selectAll().executeAsList().map { it.toDomain() }

    fun insert(id: String, name: String, email: String) =
        database.userQueries.insertUser(id, name, email)

    fun deleteAll() =
        database.userQueries.deleteAll()

    private fun UserEntity.toDomain() = User(id = id, name = name, email = email)
}
