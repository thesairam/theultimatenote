# Production Checklist — Google Play Store

Master checklist for deploying Connecting Dots to Google Play. References existing docs in `store-submission/` where applicable.

---

## CRITICAL BLOCKERS (must fix before submission)

### 1. App Name Mismatch — DONE
All docs, legal pages, and store descriptions updated to **"Connecting Dots"**:
- [x] `docs/privacy-policy.html`
- [x] `docs/terms-of-service.html`
- [x] `docs/index.html` (landing page)
- [x] `docs/store-submission/STORE_DESCRIPTIONS.md`
- [x] `docs/store-submission/GOOGLE_PLAY_STEPS.md`
- [x] `docs/store-submission/PRIVACY_POLICY.md`
- [x] `docs/store-submission/TERMS_OF_SERVICE.md`
- [x] `docs/store-submission/RELEASE_BUILD_GUIDE.md`
- [x] `docs/store-submission/ASSET_CHECKLIST.md`

### 2. Application ID — KEEPING AS-IS
Keeping `com.theultimatenote.app` — the ID is internal and never shown to users.
- [x] Decision: keep `com.theultimatenote.app`

### 3. Release Signing Key
Signing config added to build.gradle.kts (reads from local.properties).
- [ ] Generate release keystore (see `store-submission/RELEASE_BUILD_GUIDE.md` for commands)
- [ ] Store keystore file securely (password manager, encrypted drive)
  - **WARNING**: If you lose this file, you can NEVER update the app
- [x] Add signing config to `composeApp/build.gradle.kts`
- [ ] Add signing properties to `local.properties` (already gitignored):
  ```
  RELEASE_STORE_FILE=../keystore/release.jks
  RELEASE_STORE_PASSWORD=your_store_password
  RELEASE_KEY_ALIAS=your_key_alias
  RELEASE_KEY_PASSWORD=your_key_password
  ```
- [ ] Build and test `./gradlew assembleRelease` successfully

### 4. Privacy Policy Hosting
Google Play requires a publicly accessible privacy policy URL.
- [ ] Host `privacy-policy.html` at a public URL
  - Options: GitHub Pages (free), Firebase Hosting (free tier), Vercel
- [ ] Host `terms-of-service.html` at same domain
- [ ] Host `index.html` landing page
- [ ] **FILL IN**: Enter the hosted URL here: `___________________________`

### 5. Google Play Developer Account
- [ ] Create account at https://play.google.com/console ($25 one-time fee)
- [ ] Complete identity verification (can take 24-48 hours)
- [ ] **FILL IN**: Developer name for store: `___________________________`
- [ ] **FILL IN**: Developer email (public-facing): `___________________________`
- [ ] **FILL IN**: Developer website URL: `___________________________`

---

## STORE LISTING (fill in before submission)

### App Details
- **App name**: Connecting Dots
- **Short description** (80 chars max):
  > Plan, journal, and learn — Kanban boards, daily planner, and AI assistant in one.
- **Full description**: See `store-submission/STORE_DESCRIPTIONS.md` (update app name first)
- **Category**: Productivity
- **Tags**: productivity, planner, kanban, journal, todo

### Contact Info
- [ ] **FILL IN**: Support email: `___________________________`
- [ ] **FILL IN**: Website (optional): `___________________________`
- [ ] **FILL IN**: Phone (optional): `___________________________`

### Test Credentials (for Google reviewer)
- [ ] Create a test account for reviewers
- [ ] **FILL IN**: Test email: `___________________________`
- [ ] **FILL IN**: Test password: `___________________________`
- [ ] Add instructions: "Use provided credentials to sign in. App requires authentication to access all features."

---

## VISUAL ASSETS (all required)

See `store-submission/ASSET_CHECKLIST.md` for specs.

### Required
- [ ] **App icon**: 512x512 PNG (no transparency, no rounded corners — store applies mask)
  - Generate from current launcher icon design
- [ ] **Feature graphic**: 1024x500 PNG/JPG
  - Should include app name "Connecting Dots", tagline, app icon or UI preview
  - Use deep emerald + gold color scheme
- [ ] **Phone screenshots**: minimum 2, recommended 4-8 (1080x1920 portrait)
  - Screens to capture: Home, Kanban Board, Daily Dashboard, AI Chat, Notebook, Projects

### Optional but recommended
- [ ] Tablet screenshots (if supporting tablets)
- [ ] Promotional video (YouTube URL, 30s-2min)

### Screenshot Tips
- Use Demo Mode: `adb shell settings put global sysui_demo_allowed 1`
- Add descriptive text/frames using Canva, Figma, or Screenshots.pro (all free)
- Show real content, not placeholder text

---

## CODE CHANGES REQUIRED

### Already Implemented
- [x] Account deletion (in Profile screen)
- [x] In-app privacy policy & terms links (in Profile screen)
- [x] POST_NOTIFICATIONS runtime permission (in MainActivity)
- [x] ProGuard/R8 minification enabled for release builds
- [x] Firestore offline persistence enabled
- [x] Target SDK 35, Min SDK 26

### Code Changes — DONE
- [x] **ProGuard rules update**: Added keeps for Ktor, OkHttp, Coil, Credentials, Billing, kotlinx-datetime, multiplatform-settings
- [x] **Crashlytics**: Plugin applied in build.gradle.kts
- [x] **Debug logging**: No println/Log.d found in source code
- [x] **Version bump**: `versionName = "1.0.0"` set
- [x] **Signing config**: Added to build.gradle.kts (reads from local.properties)

### Still Needed (manual testing)
- [ ] **Verify offline handling**:
  - [ ] App launches in airplane mode
  - [ ] AI chat shows graceful error when offline
  - [ ] Tasks created offline sync when reconnected

---

## GOOGLE PLAY CONSOLE FORMS

Pre-filled answers in `store-submission/`. Just copy into the console.

### Content Rating (IARC)
- [ ] Complete questionnaire — answers ready in `CONTENT_RATING_ANSWERS.md`
- Expected result: **Everyone / PEGI 3**

### Data Safety
- [ ] Complete form — answers ready in `DATA_SAFETY_ANSWERS.md`
- Key declarations:
  - Email/name collected for auth (not shared)
  - User content stored in Firebase (not shared)
  - Data encrypted in transit (HTTPS/TLS)
  - Users can request deletion (in-app)

### Target Audience & Content
- [ ] Target age: **18+** (or All ages)
- [ ] Contains ads: **No**
- [ ] Is news app: **No**

### App Access
- [ ] Select "All or some functionality is restricted"
- [ ] Provide test credentials (from above)

---

## PRE-SUBMISSION TESTING

### Critical Flows (test on release build)
- [ ] Fresh install → sign up with email → verify all features
- [ ] Fresh install → sign in with Google → verify all features
- [ ] Create project → add tasks → move between columns
- [ ] Create daily recurring task with time → get notification
- [ ] AI chat → send message → get response
- [ ] Create notebook → add pages → format text
- [ ] Edit profile → save → verify persistence
- [ ] Sign out → sign in → data preserved
- [ ] Kill app → reopen → state preserved
- [ ] Airplane mode → use app → reconnect → data syncs
- [ ] Delete account → verify all data removed

### Edge Cases
- [ ] Empty states (no projects/tasks) show helpful messages
- [ ] Long task titles don't overflow
- [ ] Rapid tapping doesn't create duplicates
- [ ] Back button behavior is logical
- [ ] Screen rotation doesn't crash (or lock to portrait)
- [ ] App with low memory doesn't crash

### Devices to Test
- [ ] At least 2 different screen sizes
- [ ] API 26 (Android 8) if possible
- [ ] API 35 (Android 15) — latest

---

## RELEASE PROCESS

### Build
```bash
# 1. Build release AAB
./gradlew bundleRelease

# Output: composeApp/build/outputs/bundle/release/composeApp-release.aab

# 2. Optional: build release APK for local testing
./gradlew assembleRelease
adb install -r composeApp/build/outputs/apk/release/composeApp-release.apk
```

### Upload Path (recommended)
1. **Internal testing** — up to 100 testers, instant approval
2. **Closed testing** — invite testers, 2-3 day review
3. **Production** — full release, 3-7 day initial review

### Release Notes (for v1.0.0)
```
Connecting Dots — your all-in-one productivity companion.

Features:
• Kanban boards for project management
• Daily habits & recurring task tracking
• Learning path management
• Rich notebook journaling
• AI chat assistant (Gemini)
• Focus timer with stats
• Quick-add tasks from anywhere
• Star projects for quick access
```

---

## POST-LAUNCH

- [ ] Monitor crashes in Play Console + Firebase Crashlytics
- [ ] Respond to user reviews within 24 hours
- [ ] Plan update cadence (bi-weekly recommended)
- [ ] Set up Firebase Analytics dashboard

---

## COMMON REJECTION REASONS (reference)

| Issue | Our Status |
|-------|-----------|
| Missing privacy policy | Ready (needs hosting) |
| Login required without test credentials | Provide in App Access |
| Crash on launch | Test release build thoroughly |
| Misleading description | Descriptions match functionality |
| Missing data safety form | Answers prepared |
| Account deletion not available | Implemented in Profile |
| Broken functionality | Test all flows |

---

## TIMELINE ESTIMATE

| Step | Duration |
|------|----------|
| Update app name in all docs | 1 hour |
| Generate signing key + release build | 1 hour |
| Create visual assets (icon, screenshots, feature graphic) | 1-2 days |
| Host privacy policy | 30 minutes |
| Fill Google Play Console forms | 2-3 hours |
| Internal testing | 1-3 days |
| Production review (first submission) | 3-7 business days |
| **Total to launch** | **~1-2 weeks** |
