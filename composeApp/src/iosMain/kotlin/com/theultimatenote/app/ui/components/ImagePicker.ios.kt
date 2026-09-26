package com.theultimatenote.app.ui.components

import androidx.compose.runtime.Composable

// TODO: wire up PHPickerViewController so users can attach new photos on iOS.
// Viewing images already attached from Android works today (see ImageThumbnail).
@Composable
actual fun rememberImagePickerLauncher(onImagePicked: (ByteArray) -> Unit): () -> Unit {
    return {
        // No-op until the native photo picker is implemented.
    }
}
