# Release Build Guide

How to create signed release builds for Google Play and Apple App Store.

---

## Android — Release AAB (App Bundle)

### 1. Generate Signing Key (one time only)

```bash
keytool -genkey -v \
  -keystore theultimatenote-release.jks \
  -keyalg RSA -keysize 2048 \
  -validity 10000 \
  -alias theultimatenote \
  -storepass YOUR_STORE_PASSWORD \
  -keypass YOUR_KEY_PASSWORD
```

When prompted:
- First and last name: Your name
- Organization: Your name or company
- City, State, Country: Your location

**IMPORTANT**: Store this .jks file securely. If you lose it, you cannot update your app.

### 2. Configure signing in build.gradle.kts

Add to `composeApp/build.gradle.kts` inside the `android` block:

```kotlin
signingConfigs {
    create("release") {
        val props = Properties().apply {
            load(rootProject.file("local.properties").inputStream())
        }
        storeFile = file(props.getProperty("RELEASE_STORE_FILE", ""))
        storePassword = props.getProperty("RELEASE_STORE_PASSWORD", "")
        keyAlias = props.getProperty("RELEASE_KEY_ALIAS", "")
        keyPassword = props.getProperty("RELEASE_KEY_PASSWORD", "")
    }
}

buildTypes {
    release {
        isMinifyEnabled = true
        isShrinkResources = true
        proguardFiles(
            getDefaultProguardFile("proguard-android-optimize.txt"),
            "proguard-rules.pro"
        )
        signingConfig = signingConfigs.getByName("release")
    }
}
```

### 3. Add signing properties to local.properties

```properties
RELEASE_STORE_FILE=../theultimatenote-release.jks
RELEASE_STORE_PASSWORD=YOUR_STORE_PASSWORD
RELEASE_KEY_ALIAS=theultimatenote
RELEASE_KEY_PASSWORD=YOUR_KEY_PASSWORD
```

### 4. Create ProGuard rules

Create `composeApp/proguard-rules.pro`:

```proguard
# Keep Kotlin serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep Firebase
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# Keep Ktor
-keep class io.ktor.** { *; }

# Keep app data models
-keep class com.theultimatenote.app.data.model.** { *; }
-keep class com.theultimatenote.app.data.repository.Groq* { *; }
-keep class com.theultimatenote.app.data.repository.Gemini* { *; }
```

### 5. Build the release AAB

```bash
./gradlew bundleRelease
```

Output: `composeApp/build/outputs/bundle/release/composeApp-release.aab`

### 6. Test the release build locally

```bash
# Build release APK for local testing
./gradlew assembleRelease

# Install on device
adb install -r composeApp/build/outputs/apk/release/composeApp-release.apk
```

---

## iOS — Release Archive

### Prerequisites
- Mac with Xcode installed
- Apple Developer account configured in Xcode
- Distribution certificate and provisioning profile

### 1. Configure Xcode project

1. Open `iosApp/iosApp.xcodeproj` in Xcode
2. Select the target
3. Set Bundle Identifier: `com.theultimatenote.app`
4. Set Version: `0.1.0`
5. Set Build: `1`
6. Select your team for signing

### 2. Build KMP framework

```bash
./gradlew iosArm64Binaries
```

### 3. Archive in Xcode

1. Select "Any iOS Device (arm64)" as build destination
2. Product menu > Archive
3. Wait for build to complete
4. Organizer window opens automatically
5. Select the archive > Distribute App
6. Choose "App Store Connect"
7. Follow the wizard to upload

### 4. Or use command line

```bash
xcodebuild -project iosApp/iosApp.xcodeproj \
  -scheme iosApp \
  -configuration Release \
  -archivePath build/TheUltimateNote.xcarchive \
  archive \
  CODE_SIGN_IDENTITY="Apple Distribution" \
  DEVELOPMENT_TEAM="YOUR_TEAM_ID"

xcodebuild -exportArchive \
  -archivePath build/TheUltimateNote.xcarchive \
  -exportOptionsPlist iosApp/ExportOptions.plist \
  -exportPath build/iosExport
```

---

## Version Management

Before each release, update these:

### Android
In `composeApp/build.gradle.kts`:
```kotlin
defaultConfig {
    versionCode = 2  // Increment for each upload
    versionName = "0.2.0"  // Human-readable version
}
```

### iOS
In Xcode project settings:
- Version: `0.2.0`
- Build: `2`

### Versioning scheme
- **Major.Minor.Patch** (e.g., 0.1.0 → 0.2.0 → 1.0.0)
- versionCode always increments (1, 2, 3, 4...)
- MVP = 0.x.x, first public = 1.0.0

---

## Security Reminders

- NEVER commit signing keys (.jks files) to git
- NEVER commit local.properties (already in .gitignore)
- NEVER commit API keys in source code
- Store signing key backup in a secure location (password manager, encrypted drive)
- Use Play App Signing (Google manages your app signing key, you keep upload key)
