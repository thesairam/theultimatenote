import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.googleServices)
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
            implementation(libs.kotlinx.datetime)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.multiplatform.settings)
        }

        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
            implementation(libs.androidx.activity.compose)

            implementation(libs.firebase.auth)
            implementation(libs.firebase.firestore)
            implementation(libs.credentials)
            implementation(libs.credentials.play)
            implementation(libs.googleid)
            implementation(libs.play.services.auth.base)
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

    defaultConfig {
        val localPropsFile = rootProject.file("local.properties")
        val props = if (localPropsFile.exists()) {
            Properties().apply { load(localPropsFile.inputStream()) }
        } else {
            Properties()
        }
        val geminiKey1 = props.getProperty("GEMINI_API_KEY_1", "")
            .ifBlank { project.findProperty("GEMINI_API_KEY_1")?.toString() ?: "" }
        val geminiKey2 = props.getProperty("GEMINI_API_KEY_2", "")
            .ifBlank { project.findProperty("GEMINI_API_KEY_2")?.toString() ?: "" }
        val groqKey = props.getProperty("GROQ_API_KEY", "")
            .ifBlank { project.findProperty("GROQ_API_KEY")?.toString() ?: "" }
        buildConfigField("String", "GEMINI_API_KEY_1", "\"$geminiKey1\"")
        buildConfigField("String", "GEMINI_API_KEY_2", "\"$geminiKey2\"")
        buildConfigField("String", "GROQ_API_KEY", "\"$groqKey\"")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
