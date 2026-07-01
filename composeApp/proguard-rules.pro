# Keep Compose
-dontwarn androidx.compose.**
-keep class androidx.compose.** { *; }

# Keep Firebase
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# Keep data models for Firestore serialization
-keep class com.theultimatenote.app.data.model.** { *; }

# Keep Kotlin serialization
-keepattributes *Annotation*
-keepclassmembers class * {
    @kotlinx.serialization.Serializable *;
}

# Keep Koin
-keep class org.koin.** { *; }
-dontwarn org.koin.**
