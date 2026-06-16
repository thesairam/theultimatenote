package com.theultimatenote.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import com.theultimatenote.app.data.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class FirebaseUserRepository : UserRepository {

    private val db = FirebaseFirestore.getInstance()
    private val usersCol = db.collection("users")

    override fun getUser(userId: String): Flow<User?> {
        return usersCol.document(userId)
            .snapshots()
            .map { doc ->
                if (!doc.exists()) return@map null
                User(
                    id = doc.id,
                    email = doc.getString("email") ?: "",
                    displayName = doc.getString("displayName") ?: "",
                    photoUrl = doc.getString("photoUrl"),
                    bio = doc.getString("bio") ?: "",
                    hobbies = (doc.get("hobbies") as? List<String>) ?: emptyList(),
                    areasOfFocus = (doc.get("areasOfFocus") as? List<String>) ?: emptyList(),
                    skills = (doc.get("skills") as? List<String>) ?: emptyList(),
                    idols = (doc.get("idols") as? List<String>) ?: emptyList(),
                    goals = (doc.get("goals") as? List<String>) ?: emptyList(),
                    currentLearningFocus = doc.getString("currentLearningFocus") ?: "",
                )
            }
    }

    override suspend fun saveUser(user: User) {
        usersCol.document(user.id).set(user.toMap()).await()
    }

    private fun User.toMap() = mapOf(
        "id" to id,
        "email" to email,
        "displayName" to displayName,
        "photoUrl" to photoUrl,
        "bio" to bio,
        "hobbies" to hobbies,
        "areasOfFocus" to areasOfFocus,
        "skills" to skills,
        "idols" to idols,
        "goals" to goals,
        "currentLearningFocus" to currentLearningFocus,
    )
}
