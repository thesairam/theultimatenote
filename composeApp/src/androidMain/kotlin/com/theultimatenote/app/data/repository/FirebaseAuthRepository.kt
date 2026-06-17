package com.theultimatenote.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseAuthRepository : AuthRepository {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override val currentUser: Flow<AuthUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser?.toAuthUser())
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override val isLoggedIn: Boolean
        get() = auth.currentUser != null

    override suspend fun signIn(email: String, password: String): AuthResult {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user = result.user ?: return AuthResult.Error("Sign in failed. Please try again.")
            AuthResult.Success(user.toAuthUser())
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            AuthResult.Error("Incorrect email or password.")
        } catch (e: FirebaseAuthInvalidUserException) {
            AuthResult.Error("No account found with this email.")
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Sign in failed. Please try again.")
        }
    }

    override suspend fun signUp(email: String, password: String, displayName: String): AuthResult {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user ?: return AuthResult.Error("Account creation failed. Please try again.")
            user.updateProfile(userProfileChangeRequest { this.displayName = displayName }).await()
            AuthResult.Success(user.toAuthUser())
        } catch (e: FirebaseAuthWeakPasswordException) {
            AuthResult.Error("Password is too weak. Use at least 6 characters.")
        } catch (e: FirebaseAuthUserCollisionException) {
            AuthResult.Error("An account with this email already exists.")
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            AuthResult.Error("Invalid email format.")
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Sign up failed. Please try again.")
        }
    }

    override suspend fun signInWithGoogle(idToken: String): AuthResult {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()
            val user = result.user ?: return AuthResult.Error("Google sign-in failed.")
            AuthResult.Success(user.toAuthUser())
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Google sign-in failed. Please try again.")
        }
    }

    override suspend fun sendPasswordReset(email: String): AuthResult {
        return try {
            auth.sendPasswordResetEmail(email).await()
            AuthResult.Success(AuthUser(uid = "", email = email, displayName = null))
        } catch (e: FirebaseAuthInvalidUserException) {
            AuthResult.Error("No account found with this email.")
        } catch (e: Exception) {
            AuthResult.Error("Failed to send reset email. Please try again.")
        }
    }

    override suspend fun signOut() {
        auth.signOut()
    }

    override suspend fun deleteAccount(): AuthResult {
        val user = auth.currentUser ?: return AuthResult.Error("No user signed in.")
        val uid = user.uid
        val db = FirebaseFirestore.getInstance()
        return try {
            // Delete user profile document
            db.collection("users").document(uid).delete().await()

            // Delete all user's projects and their tasks
            val projectsSnapshot = db.collection("projects")
                .whereEqualTo("ownerId", uid)
                .get().await()

            for (projectDoc in projectsSnapshot.documents) {
                val tasksSnapshot = projectDoc.reference.collection("tasks").get().await()
                for (taskDoc in tasksSnapshot.documents) {
                    taskDoc.reference.delete().await()
                }
                projectDoc.reference.delete().await()
            }

            // Delete chat history
            db.collection("users").document(uid).collection("chat_messages")
                .get().await().documents.forEach { it.reference.delete().await() }

            // Delete notebooks
            val notebooksSnapshot = db.collection("notebooks")
                .whereEqualTo("ownerId", uid)
                .get().await()
            for (notebookDoc in notebooksSnapshot.documents) {
                val pagesSnapshot = notebookDoc.reference.collection("pages").get().await()
                for (pageDoc in pagesSnapshot.documents) {
                    pageDoc.reference.delete().await()
                }
                notebookDoc.reference.delete().await()
            }

            // Delete Firebase Auth account
            user.delete().await()

            AuthResult.Success(AuthUser(uid = uid, email = null, displayName = null))
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Failed to delete account. You may need to sign in again before deleting.")
        }
    }

    private fun com.google.firebase.auth.FirebaseUser.toAuthUser() = AuthUser(
        uid = uid,
        email = email,
        displayName = displayName,
    )
}
