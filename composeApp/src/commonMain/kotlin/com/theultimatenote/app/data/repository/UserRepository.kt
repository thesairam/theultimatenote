package com.theultimatenote.app.data.repository

import com.theultimatenote.app.data.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getUser(userId: String): Flow<User?>
    suspend fun saveUser(user: User)
}
