package com.theultimatenote.app.data.repository

import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class FirebaseImageStorageRepository : ImageStorageRepository {

    private val storage = FirebaseStorage.getInstance()

    override suspend fun uploadImage(userId: String, imageBytes: ByteArray, fileName: String): String {
        val ref = storage.reference.child("users/$userId/images/$fileName")
        ref.putBytes(imageBytes).await()
        return ref.downloadUrl.await().toString()
    }

    override suspend fun deleteImage(imageUrl: String) {
        try {
            val ref = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl)
            ref.delete().await()
        } catch (_: Exception) { }
    }
}
