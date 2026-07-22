package org.weekendware.basil.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.weekendware.basil.domain.model.User

class FakeUserRepository : UserRepository {

    data class InsertCall(val id: String, val name: String, val email: String)

    val insertCalls = mutableListOf<InsertCall>()

    private val _usersFlow = MutableStateFlow<List<User>>(emptyList())

    override fun getAll(): List<User> = emptyList()

    override fun getAllAsFlow(): Flow<List<User>> = _usersFlow

    override fun insert(id: String, name: String, email: String) {
        insertCalls.add(InsertCall(id, name, email))
    }

    override fun updateAvatarUrl(userId: String, url: String?) = Unit

    override fun deleteAll() {
        insertCalls.clear()
    }
}
