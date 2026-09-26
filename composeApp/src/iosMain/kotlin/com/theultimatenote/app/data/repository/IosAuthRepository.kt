package com.theultimatenote.app.data.repository

import com.theultimatenote.app.FirebaseInit
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

private const val NOT_CONFIGURED_MESSAGE =
    "Firebase isn't configured yet — add GoogleService-Info.plist to the iOS app (see iosApp/README.md)."

class IosAuthRepository : AuthRepository {

    private val auth get() = Firebase.auth

    override val currentUser: Flow<AuthUser?> =
        if (FirebaseInit.isConfigured) auth.authStateChanged.map { it?.toAuthUser() } else flowOf(null)

    override val isLoggedIn: Boolean
        get() = FirebaseInit.isConfigured && auth.currentUser != null

    override suspend fun signIn(email: String, password: String): AuthResult {
        if (!FirebaseInit.isConfigured) return AuthResult.Error(NOT_CONFIGURED_MESSAGE)
        return try {
            val result = auth.signInWithEmailAndPassword(email, password)
            val user = result.user ?: return AuthResult.Error("Sign in failed. Please try again.")
            AuthResult.Success(user.toAuthUser())
        } catch (e: Exception) {
            AuthResult.Error(friendlyAuthError(e))
        }
    }

    override suspend fun signUp(email: String, password: String, displayName: String): AuthResult {
        if (!FirebaseInit.isConfigured) return AuthResult.Error(NOT_CONFIGURED_MESSAGE)
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password)
            val user = result.user ?: return AuthResult.Error("Account creation failed. Please try again.")
            user.updateProfile(displayName = displayName)
            AuthResult.Success(user.toAuthUser().copy(displayName = displayName))
        } catch (e: Exception) {
            AuthResult.Error(friendlyAuthError(e))
        }
    }

    override suspend fun signInWithGoogle(idToken: String): AuthResult {
        // Google Sign-In isn't wired up on iOS yet (needs the Google Sign-In iOS SDK
        // + URL scheme registration in Xcode). Email/password works today.
        return AuthResult.Error("Google sign-in isn't available on iOS yet — use email & password.")
    }

    override suspend fun sendPasswordReset(email: String): AuthResult {
        return try {
            auth.sendPasswordResetEmail(email)
            AuthResult.Success(AuthUser(uid = "", email = email, displayName = null))
        } catch (e: Exception) {
            AuthResult.Error(friendlyAuthError(e))
        }
    }

    override suspend fun signOut() {
        if (FirebaseInit.isConfigured) auth.signOut()
    }

    override suspend fun deleteAccount(): AuthResult {
        val user = auth.currentUser ?: return AuthResult.Error("No user signed in.")
        val uid = user.uid
        val db = Firebase.firestore
        return try {
            db.collection("users").document(uid).delete()

            val projectsSnapshot = db.collection("projects")
                .where { "ownerId" equalTo uid }
                .get()
            for (projectDoc in projectsSnapshot.documents) {
                val tasksSnapshot = projectDoc.reference.collection("tasks").get()
                for (taskDoc in tasksSnapshot.documents) {
                    taskDoc.reference.delete()
                }
                projectDoc.reference.delete()
            }

            val boardsSnapshot = db.collection("kanban_boards")
                .where { "ownerId" equalTo uid }
                .get()
            for (boardDoc in boardsSnapshot.documents) {
                boardDoc.reference.delete()
            }

            db.collection("users").document(uid).collection("pomodoro_sessions")
                .get().documents.forEach { it.reference.delete() }

            db.collection("users").document(uid).collection("chat_messages")
                .get().documents.forEach { it.reference.delete() }

            val notebooksSnapshot = db.collection("notebooks")
                .where { "ownerId" equalTo uid }
                .get()
            for (notebookDoc in notebooksSnapshot.documents) {
                val pagesSnapshot = notebookDoc.reference.collection("pages").get()
                for (pageDoc in pagesSnapshot.documents) {
                    pageDoc.reference.delete()
                }
                notebookDoc.reference.delete()
            }

            user.delete()

            AuthResult.Success(AuthUser(uid = uid, email = null, displayName = null))
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Failed to delete account. You may need to sign in again before deleting.")
        }
    }

    private fun friendlyAuthError(e: Exception): String {
        val message = e.message ?: return "Something went wrong. Please try again."
        return when {
            message.contains("password is invalid", ignoreCase = true) ||
                message.contains("INVALID_LOGIN_CREDENTIALS", ignoreCase = true) ->
                "Incorrect email or password."
            message.contains("no user record", ignoreCase = true) ->
                "No account found with this email."
            message.contains("already in use", ignoreCase = true) ->
                "An account with this email already exists."
            message.contains("weak", ignoreCase = true) ->
                "Password is too weak. Use at least 6 characters."
            message.contains("badly formatted", ignoreCase = true) ->
                "Invalid email format."
            else -> message
        }
    }

    private fun FirebaseUser.toAuthUser() = AuthUser(
        uid = uid,
        email = email,
        displayName = displayName,
    )
}
