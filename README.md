# Connecting Dots

**Plan. Prioritize. Learn. Journal. Grow.**

A mobile productivity app combining Kanban boards, daily planning, learning paths, journaling, and AI chat — built with Kotlin Multiplatform for Android and iOS.

## Features

- **Kanban Boards** — Create projects with custom columns, drag-and-drop tasks
- **Daily Planner** — Recurring and temporary tasks with time-based reminders
- **Learning Paths** — Track courses and learning goals with dedicated boards
- **Notebooks** — Rich-text journaling with headings, lists, code blocks, and images
- **AI Chat** — Gemini-powered assistant for ideation and planning
- **Eisenhower Matrix** — Prioritize tasks by urgency and importance
- **Motivational Quotes** — Morning push notifications from your chosen idols
- **Profile** — Hobbies, skills, focus areas, and inspiration people

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Shared code | Kotlin Multiplatform + Compose Multiplatform |
| Android UI | Jetpack Compose |
| iOS UI | Compose Multiplatform (via Kotlin/Native) |
| Backend | Firebase (Auth, Firestore, Storage, Cloud Messaging) |
| AI | Gemini API |
| DI | Koin |
| Architecture | MVVM |

## Project Structure

```
theultimatenote/
├── composeApp/          # Shared KMP module (UI + logic)
│   └── src/
│       ├── commonMain/  # Shared code (~90%)
│       ├── androidMain/ # Android-specific (Firebase, notifications)
│       └── iosMain/     # iOS-specific (Firebase via GitLive SDK)
├── androidApp/          # Android app entry point
├── iosApp/              # iOS app entry point (Xcode project)
├── docs/                # Landing page, privacy policy, terms of service
└── gradle/              # Gradle wrapper and version catalog
```

## Getting Started

### Prerequisites

- **Android:** Android Studio with SDK 24+
- **iOS:** macOS with Xcode 16+ and CocoaPods (`brew install cocoapods`)
- **Firebase:** Add your own `google-services.json` (Android) and `GoogleService-Info.plist` (iOS) from the Firebase console

### Build & Run

```bash
# Android debug build
./gradlew assembleDebug

# Install on connected device/emulator
./gradlew installDebug

# iOS (macOS only)
cd iosApp
xcodegen generate
pod install
xcodebuild -workspace iosApp.xcworkspace -scheme iosApp \
  -destination 'platform=iOS Simulator,name=iPhone 17' build
```

### Run Tests

```bash
./gradlew test                        # All tests
./gradlew ktlintCheck                 # Lint
./gradlew detekt                      # Static analysis
./gradlew iosSimulatorArm64Test       # iOS tests (Apple Silicon)
```

## Publishing to App Stores

### Google Play Store

1. **Create a developer account** — [play.google.com/console](https://play.google.com/console) ($25 one-time)

2. **Generate a signing key:**
   ```bash
   keytool -genkey -v -keystore connecting-dots-release.keystore \
     -alias connectingdots -keyalg RSA -keysize 2048 -validity 10000
   ```
   Store the keystore and passwords securely — you cannot update the app without them.

3. **Configure signing** in `androidApp/build.gradle.kts`:
   ```kotlin
   android {
       signingConfigs {
           create("release") {
               storeFile = file("connecting-dots-release.keystore")
               storePassword = System.getenv("KEYSTORE_PASSWORD")
               keyAlias = "connectingdots"
               keyPassword = System.getenv("KEY_PASSWORD")
           }
       }
       buildTypes {
           release {
               signingConfig = signingConfigs.getByName("release")
               isMinifyEnabled = true
               proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
           }
       }
   }
   ```

4. **Build the release bundle:**
   ```bash
   ./gradlew :androidApp:bundleRelease
   ```
   Output: `androidApp/build/outputs/bundle/release/androidApp-release.aab`

5. **Upload to Play Console:**
   - Create app → name: **Connecting Dots**, category: Productivity
   - Add store listing: description, screenshots (1080×1920), feature graphic (1024×500), icon (512×512)
   - Set privacy policy URL → your `docs/privacy-policy.html`
   - Complete content rating questionnaire and data safety form
   - Upload `.aab` under Production → Create new release
   - Submit for review (1–3 days)

### Apple App Store

1. **Join Apple Developer Program** — [developer.apple.com/programs](https://developer.apple.com/programs/) ($99/year)

2. **Configure signing in Xcode:**
   - Open `iosApp/iosApp.xcworkspace`
   - Target → Signing & Capabilities → select your team
   - Bundle ID: `com.theultimatenote.app`

3. **Create app in App Store Connect:**
   - [appstoreconnect.apple.com](https://appstoreconnect.apple.com) → My Apps → + New App
   - Platform: iOS, Name: **Connecting Dots**, Bundle ID: `com.theultimatenote.app`

4. **Archive and upload:**
   ```bash
   cd iosApp
   xcodebuild -workspace iosApp.xcworkspace -scheme iosApp \
     -destination 'generic/platform=iOS' \
     -archivePath build/iosApp.xcarchive archive

   xcodebuild -exportArchive \
     -archivePath build/iosApp.xcarchive \
     -exportOptionsPlist ExportOptions.plist \
     -exportPath build/export
   ```
   Or in Xcode: Product → Archive → Distribute App → App Store Connect → Upload

5. **App Store listing:**
   - Subtitle: Plan, Learn & Journal
   - Keywords: kanban, planner, journal, productivity, todo, tasks, learning
   - Screenshots: required for 6.7" and 6.1" sizes
   - Icon: 1024×1024 (already in `iosApp/iosApp/Assets.xcassets/AppIcon.appiconset`)
   - Privacy policy URL, age rating (4+), data privacy questionnaire
   - Provide demo credentials for the review team
   - Submit for review (1–2 days)

## Color Theme

| Role | Color |
|------|-------|
| Primary | Deep Forest Green |
| Secondary | Sage Green |
| Accent | Warm Beige |
| Background | Soft Cream |
| Highlights | Muted Gold |

## License

Private repository. All rights reserved.
