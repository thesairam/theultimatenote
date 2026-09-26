# iOS app

Shares ~90% of its code with Android via Kotlin Multiplatform (`composeApp/src/commonMain`).
iOS-specific code lives in `composeApp/src/iosMain` and this folder.

## One-time setup

1. **Firebase config (required for login/data to work):**
   - Go to the [Firebase console](https://console.firebase.google.com) → project `theultimatenote-7a9cb` → Project settings → Add app → iOS
   - Bundle ID: `com.theultimatenote.app`
   - Download `GoogleService-Info.plist` and drop it into `iosApp/iosApp/` (next to `Info.plist`)
   - Open `iosApp.xcodeproj` in Xcode once and drag the file into the `iosApp` target if it doesn't show up in the project navigator automatically (Add Files to "iosApp" → check "Copy items if needed" + the iosApp target)
   - Until this file is added, the app still launches (login screen etc.) but Firebase calls will show a "Firebase isn't configured yet" message

2. **Regenerate the Xcode project after any change to `project.yml` or `composeApp/build.gradle.kts`'s `cocoapods {}` block:**
   ```bash
   cd iosApp
   xcodegen generate
   pod install
   ```
   Then open `iosApp.xcworkspace` (not `.xcodeproj`) in Xcode.

3. **Build & run:**
   ```bash
   cd iosApp
   xcodebuild -workspace iosApp.xcworkspace -scheme iosApp -destination 'platform=iOS Simulator,name=iPhone 16' build
   ```
   Or just open `iosApp.xcworkspace` in Xcode and hit Run.

## What's stubbed for now (needs a paid Apple Developer account or more setup)

- Google Sign-In (needs GoogleSignIn iOS SDK + URL scheme)
- Sign in with Apple (needs the capability enabled under a paid account)
- Google/Apple Calendar sync
- Subscriptions/billing (needs StoreKit + App Store Connect product)
- Picking a *new* photo to attach (viewing existing attached photos works)

Email/password auth, Firestore data (projects, tasks, notebooks, chat, pomodoro), and
local notification reminders are fully implemented and share the same Firebase
project as Android — the same account works on both apps.
