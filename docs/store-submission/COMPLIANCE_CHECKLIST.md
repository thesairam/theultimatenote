# Store Compliance Checklist

Everything that must be true for a smooth launch on both stores.

---

## Code & Build Requirements

### Android (Google Play)

- [x] Target SDK 35 (current requirement: target API 34+)
- [x] Min SDK 26 (covers 95%+ of devices)
- [x] Uses Android App Bundle (AAB) format — `./gradlew bundleRelease`
- [x] All permissions declared in AndroidManifest.xml
- [ ] Release signing key generated and stored securely
- [ ] ProGuard/R8 minification enabled for release builds
- [ ] Tested on multiple screen sizes (phone + tablet)
- [ ] Tested on API 26 and API 35

### iOS (App Store)

- [ ] Built with latest stable Xcode
- [ ] Supports latest iOS version (iOS 17+)
- [ ] Universal app or iPhone-only declared
- [ ] Sign in with Apple implemented (REQUIRED if Google Sign-In exists)
- [ ] App Transport Security compliant (HTTPS only — yes via Firebase)
- [ ] No private API usage
- [ ] Tested on multiple iPhone sizes

---

## Policy Compliance

### Google Play Policies

| Policy | Status | Notes |
|--------|--------|-------|
| Privacy Policy URL | Needs hosting | See PRIVACY_POLICY.md — host at a public URL |
| Data Safety form | Answers ready | See DATA_SAFETY_ANSWERS.md |
| Content rating | Answers ready | See CONTENT_RATING_ANSWERS.md |
| Target audience declaration | Ready | 18+ or All ages |
| Ads declaration | Ready | No ads |
| App access | Ready | Provide test credentials |
| Families Policy | N/A | Not targeting children |
| Permissions policy | Compliant | All permissions justified |
| Deceptive behavior | Compliant | No hidden functionality |
| User data policy | Compliant | No selling/sharing data |
| Financial features | N/A | No payments |
| AI-generated content | Compliant | AI is assistant, not primary content |

### Apple App Store Review Guidelines

| Guideline | Status | Notes |
|-----------|--------|-------|
| 1.1 Objectionable Content | Compliant | Productivity app, no objectionable content |
| 2.1 Performance — App Completeness | Check | Must not crash, all features must work |
| 2.3 Accurate Metadata | Ready | Descriptions match functionality |
| 3.1 Payments — In-App Purchase | N/A | No IAP in MVP |
| 4.0 Design | Compliant | Uses standard Compose Multiplatform patterns |
| 4.2 Minimum Functionality | Compliant | Multiple substantial features |
| 4.3 Spam | Compliant | Unique app, not a clone |
| 5.1 Privacy — Data Collection | Compliant | Minimal, justified collection |
| 5.1.1 Data Use and Sharing | Compliant | No selling, clear purposes |
| 5.1.2 Data Use and Sharing | Compliant | No tracking |
| Sign in with Apple | NOT DONE | Must implement before iOS submission |

---

## Required Code Changes Before Launch

### 1. Add Privacy Policy & Terms links in app

Both stores expect the privacy policy to be accessible from within the app. Add links in the Profile/Settings screen.

**Status**: Needs implementation

### 2. Add account deletion capability

Google Play requires apps that create accounts to allow account deletion. Apple requires the same.

**Status**: Needs implementation — add "Delete Account" button in Profile that:
- Deletes all Firestore data under the user's UID
- Deletes the Firebase Auth account
- Signs the user out

### 3. Sign in with Apple (iOS only)

Apple requires Sign in with Apple if any third-party sign-in (Google) is offered.

**Status**: Needs implementation before iOS submission. Not blocking Android launch.

### 4. Runtime permission requests (Android 13+)

POST_NOTIFICATIONS permission requires runtime request on Android 13+.

**Status**: Verify this is implemented. If not, add runtime permission dialog on first launch.

### 5. Release build configuration

- [ ] Enable R8/ProGuard minification
- [ ] Remove debug logging
- [ ] Ensure API keys are not in source code (they're in local.properties, gitignored — good)
- [ ] Set proper versionCode/versionName for release

### 6. Offline handling

Both stores test with airplane mode. Verify:
- [ ] App launches offline (Firestore persistence is enabled)
- [ ] Graceful error for AI chat when offline
- [ ] Tasks created offline sync when back online

---

## Legal Requirements

### GDPR (EU Users)
- [x] Privacy policy covers data rights (access, deletion, portability)
- [x] Clear explanation of data processing
- [ ] Consider adding in-app consent flow for EU users (optional for MVP)

### CCPA (California Users)
- [x] Privacy policy includes CCPA section
- [x] No selling of personal information

### COPPA (Children)
- [x] Not targeting children under 13
- [x] Privacy policy states this
- [x] No data collection from children

---

## Pre-Submission Testing Checklist

### Critical Flows (must work flawlessly)

- [ ] Fresh install → sign up with email → verify features work
- [ ] Fresh install → sign in with Google → verify features work
- [ ] Create project → add tasks → move between columns
- [ ] Create daily recurring task with time → get notification
- [ ] Switch Kanban ↔ Matrix view → data consistent
- [ ] AI chat → send message → get response
- [ ] Create notebook → add pages → format text
- [ ] Edit profile → save → verify persistence
- [ ] Sign out → sign in → data preserved
- [ ] Kill app → reopen → state preserved
- [ ] Airplane mode → use app → reconnect → data syncs

### Edge Cases

- [ ] Empty state (no projects, no tasks) — shows helpful messages
- [ ] Very long task titles — don't overflow
- [ ] Rapid tapping — no duplicate submissions
- [ ] Back button behavior — logical navigation
- [ ] Screen rotation — no crashes (or lock to portrait)
- [ ] Low memory — app doesn't crash

---

## Timeline Estimate

| Step | Duration |
|------|----------|
| Code changes (account deletion, privacy links) | 1-2 days |
| Create visual assets (icon, screenshots, feature graphic) | 1-2 days |
| Host privacy policy at public URL | 30 minutes |
| Fill Google Play Console forms | 2-3 hours |
| Fill App Store Connect forms | 2-3 hours |
| Android review | 3-7 days |
| iOS review | 1-3 days |
| **Total to first launch (Android)** | **~1-2 weeks** |
| **Total to first launch (iOS)** | **~2-3 weeks** (needs Sign in with Apple) |
