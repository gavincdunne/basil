package org.weekendware.basil.data.repository

import org.weekendware.basil.domain.model.User

class FakeUserRepository : UserRepository {

    data class InsertCall(val id: String, val name: String, val email: String)

    val insertCalls = mutableListOf<InsertCall>()

    override fun getAll(): List<User> = emptyList()

    override fun insert(id: String, name: String, email: String) {
        insertCalls.add(InsertCall(id, name, email))
    }

    override fun updateAvatarUrl(userId: String, url: String?) = Unit

    override fun deleteAll() {
        insertCalls.clear()
    }
}
