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
    alias(libs.plugins.firebaseCrashlytics)
    alias(libs.plugins.kotlinCocoapods)
}

val localPropsFile = rootProject.file("local.properties")
val localProps = if (localPropsFile.exists()) {
    Properties().apply { load(localPropsFile.inputStream()) }
} else {
    Properties()
}

fun secret(key: String): String =
    localProps.getProperty(key, "").ifBlank { project.findProperty(key)?.toString() ?: "" }

configurations.all {
    resolutionStrategy {
        // Compose Multiplatform 1.9.3 strictly pins kotlinx-datetime to 0.6.1 on the
        // Android/JVM classpath, while letting it resolve to 0.7.1 on iOS/Native. That
        // makes `kotlinx.datetime.Clock` two different, incompatible types depending on
        // platform (0.6.1 predates the `typealias Clock = kotlin.time.Clock` unification).
        // Force the same modern version everywhere so commonMain code behaves identically.
        force(
            "org.jetbrains.kotlinx:kotlinx-datetime:${libs.versions.kotlinx.datetime.get()}",
            "org.jetbrains.kotlinx:kotlinx-datetime-jvm:${libs.versions.kotlinx.datetime.get()}",
        )
    }
}

kotlin {
    sourceSets.all {
        languageSettings.optIn("kotlin.time.ExperimentalTime")
    }

    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    // iOS targets — requires macOS + Xcode to build
    listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    cocoapods {
        version = "1.0.0"
        summary = "TheUltimateNote shared module"
        homepage = "https://github.com/thesairam/theultimatenote"
        ios.deploymentTarget = "15.0"
        podfile = project.file("../iosApp/Podfile")
        framework {
            baseName = "ComposeApp"
            isStatic = true
        }
        // Firebase iOS SDK isn't linked transitively by the GitLive Kotlin
        // wrapper — it has to be pulled in as a real CocoaPods dependency.
        pod("FirebaseCore")
        pod("FirebaseAuth")
        pod("FirebaseFirestore")
        pod("FirebaseStorage")
    }

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

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }

        val androidUnitTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.kotlinx.coroutines.test)
            }
        }

        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
            implementation(libs.androidx.activity.compose)

            implementation(libs.firebase.auth)
            implementation(libs.firebase.firestore)
            implementation(libs.firebase.storage)
            implementation(libs.firebase.crashlytics)
            implementation(libs.credentials)
            implementation(libs.credentials.play)
            implementation(libs.googleid)
            implementation(libs.play.services.auth.base)
            implementation(libs.billing)
            implementation(libs.coil.compose)
        }

        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependsOn(commonMain.get())
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)

            dependencies {
                implementation(libs.ktor.client.darwin)
                implementation(libs.gitlive.firebase.common)
                implementation(libs.gitlive.firebase.auth)
                implementation(libs.gitlive.firebase.firestore)
                implementation(libs.gitlive.firebase.storage)
            }
            kotlin.srcDir(layout.buildDirectory.dir("generated/iosBuildKeys"))
        }
    }
}

// Mirrors the Android BuildConfig fields below so AiService has the same
// Groq/Gemini keys available on iOS without an Android-only BuildConfig class.
val generateIosBuildKeys = tasks.register("generateIosBuildKeys") {
    val outputDir = layout.buildDirectory.dir("generated/iosBuildKeys/com/theultimatenote/app")
    outputs.dir(layout.buildDirectory.dir("generated/iosBuildKeys"))
    doLast {
        val dir = outputDir.get().asFile
        dir.mkdirs()
        File(dir, "BuildKeys.kt").writeText(
            """
            package com.theultimatenote.app

            internal object BuildKeys {
                const val GEMINI_API_KEY_1: String = "${secret("GEMINI_API_KEY_1")}"
                const val GEMINI_API_KEY_2: String = "${secret("GEMINI_API_KEY_2")}"
                const val GROQ_API_KEY: String = "${secret("GROQ_API_KEY")}"
            }
            """.trimIndent(),
        )
    }
}

tasks.matching { it.name.startsWith("compileKotlinIos") }.configureEach {
    dependsOn(generateIosBuildKeys)
}
tasks.matching { it.name == "podPublishReleaseXCFramework" || it.name == "podPublishDebugXCFramework" }.configureEach {
    dependsOn(generateIosBuildKeys)
}

// The Kotlin Cocoapods plugin's synthetic Podfile (used to resolve pods for
// cinterop) ships a post_install hook that only raises deployment targets
// below 11.0 (workaround for KT-57741). Newer Xcode/iOS SDKs refuse anything
// below 15.0 outright, so a few transitive Firebase pods (GoogleUtilities,
// RecaptchaInterop, etc. declaring 11.0-12.0) fail to build. Bump the
// hardcoded floor in that generated file to match our real deployment target.
val patchSyntheticPodfile = tasks.register("patchSyntheticPodfileDeploymentTarget") {
    doLast {
        val podfile = layout.projectDirectory.file("build/cocoapods/synthetic/ios/Podfile").asFile
        if (podfile.exists()) {
            val patched = podfile.readText()
                .replace(
                    "deployment_target_major < 11 || (deployment_target_major == 11 && deployment_target_minor < 0)",
                    "deployment_target_major < 15 || (deployment_target_major == 15 && deployment_target_minor < 0)",
                )
                .replace("version = \"#{11}.#{0}\"", "version = \"#{15}.#{0}\"")
            podfile.writeText(patched)
        }
    }
}
tasks.matching { it.name == "podInstallSyntheticIos" }.configureEach {
    dependsOn(patchSyntheticPodfile)
}
tasks.matching { it.name == "podGenIos" }.configureEach {
    finalizedBy(patchSyntheticPodfile)
}

android {
    namespace = "com.theultimatenote.app"
    compileSdk = 35

    signingConfigs {
        if (localProps.containsKey("RELEASE_STORE_FILE")) {
            create("release") {
                storeFile = file(localProps.getProperty("RELEASE_STORE_FILE"))
                storePassword = localProps.getProperty("RELEASE_STORE_PASSWORD")
                keyAlias = localProps.getProperty("RELEASE_KEY_ALIAS")
                keyPassword = localProps.getProperty("RELEASE_KEY_PASSWORD")
            }
        }
    }

    defaultConfig {
        applicationId = "com.theultimatenote.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
    }

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        buildConfigField("String", "GEMINI_API_KEY_1", "\"${secret("GEMINI_API_KEY_1")}\"")
        buildConfigField("String", "GEMINI_API_KEY_2", "\"${secret("GEMINI_API_KEY_2")}\"")
        buildConfigField("String", "GROQ_API_KEY", "\"${secret("GROQ_API_KEY")}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            if (signingConfigs.names.contains("release")) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
