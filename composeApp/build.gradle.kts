import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
    // TODO: Uncomment after adding google-services.json to composeApp/
    // alias(libs.plugins.googleServices)
    // alias(libs.plugins.firebaseCrashlytics)
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    // iOS targets — requires macOS to build
    // listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach { iosTarget ->
    //     iosTarget.binaries.framework {
    //         baseName = "ComposeApp"
    //         isStatic = true
    //     }
    // }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.materialIconsExtended)

            implementation(libs.lifecycle.viewmodel.compose)
            implementation(libs.navigation.compose)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.multiplatform.settings)
        }

        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
            implementation(libs.androidx.activity.compose)

            // TODO: Uncomment after adding google-services.json to composeApp/
            // implementation(platform(libs.firebase.bom))
            // implementation(libs.firebase.auth)
            // implementation(libs.firebase.firestore)
            // implementation(libs.firebase.storage)
            // implementation(libs.firebase.messaging)
            // implementation(libs.firebase.analytics)
            // implementation(libs.firebase.crashlytics)
        }
    }
}

android {
    namespace = "com.theultimatenote.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.theultimatenote.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"
    }

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
