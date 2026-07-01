package com.theultimatenote.app.data.repository

interface ImageStorageRepository {
    suspend fun uploadImage(userId: String, imageBytes: ByteArray, fileName: String): String
    suspend fun deleteImage(imageUrl: String)
}
