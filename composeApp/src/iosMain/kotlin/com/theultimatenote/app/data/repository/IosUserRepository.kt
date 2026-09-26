package com.theultimatenote.app.data.repository

import com.theultimatenote.app.data.model.User
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class IosUserRepository : UserRepository {

    private val db = Firebase.firestore
    private val usersCol = db.collection("users")

    override fun getUser(userId: String): Flow<User?> {
        return usersCol.document(userId)
            .snapshots
            .map { doc -> if (doc.exists) doc.data<User>() else null }
    }

    override suspend fun saveUser(user: User) {
        usersCol.document(user.id).set(user)
    }
}
