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

# Keep Ktor
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# Keep OkHttp (used by Ktor on Android)
-keep class okhttp3.** { *; }
-dontwarn okhttp3.**
-keep class okio.** { *; }
-dontwarn okio.**

# Keep Kotlinx Serialization
-keepclassmembers @kotlinx.serialization.Serializable class ** {
    *** Companion;
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep Google Credentials / Identity
-keep class com.google.android.libraries.identity.** { *; }
-dontwarn com.google.android.libraries.identity.**
-keep class androidx.credentials.** { *; }
-dontwarn androidx.credentials.**

# Keep Coil
-keep class coil.** { *; }
-dontwarn coil.**

# Keep Billing
-keep class com.android.vending.billing.** { *; }
-dontwarn com.android.vending.billing.**

# Keep kotlinx-datetime
-keep class kotlinx.datetime.** { *; }
-dontwarn kotlinx.datetime.**

# Keep multiplatform-settings
-keep class com.russhwolf.settings.** { *; }
-dontwarn com.russhwolf.settings.**

# Keep R8 from stripping Kotlin metadata needed by serialization
-keep class kotlin.Metadata { *; }
