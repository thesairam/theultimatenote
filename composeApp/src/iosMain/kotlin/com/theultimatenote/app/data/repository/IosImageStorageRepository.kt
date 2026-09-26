package com.theultimatenote.app.data.repository

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.storage.Data
import dev.gitlive.firebase.storage.storage
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.create

class IosImageStorageRepository : ImageStorageRepository {

    private val storage = Firebase.storage

    override suspend fun uploadImage(userId: String, imageBytes: ByteArray, fileName: String): String {
        val ref = storage.reference.child("users/$userId/images/$fileName")
        ref.putData(Data(imageBytes.toNSData()))
        return ref.getDownloadUrl()
    }

    override suspend fun deleteImage(imageUrl: String) {
        try {
            storage.getReferenceFromUrl(imageUrl).delete()
        } catch (_: Exception) {
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun ByteArray.toNSData(): NSData = if (isEmpty()) {
    NSData()
} else {
    usePinned { pinned -> NSData.create(bytes = pinned.addressOf(0), length = size.toULong()) }
}
