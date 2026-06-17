# Apple App Store Submission — Step by Step

## Prerequisites

- [ ] Apple Developer Account ($99/year) — https://developer.apple.com
- [ ] Mac with Xcode installed (required for building iOS)
- [ ] App Store Connect access
- [ ] App icon: 1024x1024 PNG (no alpha/transparency, no rounded corners)
- [ ] At least 3 screenshots per required device size
- [ ] Privacy Policy hosted at a public URL
- [ ] DUNS number (for organization accounts, not needed for individual)

---

## Step 1: Enroll in Apple Developer Program

1. Go to https://developer.apple.com/programs/
2. Click "Enroll"
3. Choose Individual or Organization
   - Individual: Simpler, uses your personal name
   - Organization: Needs DUNS number, shows company name
4. Pay $99/year
5. Wait for approval (24-48 hours for individual, 1-2 weeks for organization)

---

## Step 2: Create App ID & Certificates

### In Apple Developer Portal:
1. Go to Certificates, Identifiers & Profiles
2. Create App ID:
   - Platform: iOS
   - Bundle ID: `com.theultimatenote.app`
   - Description: The Ultimate Note
   - Capabilities: Push Notifications, Sign in with Apple
3. Create Distribution Certificate (or use Xcode automatic signing)
4. Create Provisioning Profile for App Store distribution

### In Xcode:
1. Open `iosApp/iosApp.xcodeproj`
2. Select the target > Signing & Capabilities
3. Set Bundle Identifier: `com.theultimatenote.app`
4. Select your team
5. Enable "Automatically manage signing"

---

## Step 3: Create App in App Store Connect

1. Go to https://appstoreconnect.apple.com
2. Click "My Apps" > "+" > "New App"
3. Fill in:
   - Platform: iOS
   - Name: **The Ultimate Note**
   - Primary language: English (US)
   - Bundle ID: `com.theultimatenote.app`
   - SKU: `theultimatenote001`
4. Click "Create"

---

## Step 4: App Information

### General:
- **Category**: Productivity
- **Secondary category**: Lifestyle (optional)
- **Content rights**: Does not contain third-party content (or declare if it does)
- **Age rating**: Fill questionnaire (see below)

### Age Rating Questionnaire:
- Cartoon/fantasy violence: None
- Realistic violence: None
- Sexual content: None
- Profanity: None
- Drugs/alcohol/tobacco: None
- Horror/fear themes: None
- Simulated gambling: None
- Medical/treatment info: None
- Contests: None
- Unrestricted web access: No
- Gambling with real money: No

**Expected rating**: 4+ (equivalent of Everyone)

---

## Step 5: App Privacy (Privacy Nutrition Labels)

Navigate to: App Store Connect > App > App Privacy

### Data Linked to You:
| Data type | Collected | Used for |
|-----------|-----------|----------|
| Email Address | Yes | App Functionality |
| Name | Yes | App Functionality |
| User Content | Yes | App Functionality |

### Data Not Linked to You:
| Data type | Collected | Used for |
|-----------|-----------|----------|
| Diagnostics | Yes | Analytics (Crashlytics) |

### Data NOT Collected:
- Location
- Financial Info
- Health & Fitness
- Browsing History
- Search History
- Identifiers (no ads SDK)
- Purchases
- Contacts
- Photos/Videos
- Sensitive Info

### Privacy declarations:
- **Do you or your third-party partners use data for tracking?** No
- **Does your app collect data?** Yes (see above)

---

## Step 6: Store Listing

### App Store Metadata:
- **Name**: The Ultimate Note (30 char limit)
- **Subtitle** (30 chars): Plan. Prioritize. Learn. Journal. Grow.

  Note: This is 38 chars. Shortened version: "Plan. Prioritize. Learn. Grow."

- **Promotional text** (170 chars, can update without new build):
  > Your all-in-one productivity companion. Kanban boards, priority matrix, daily planner, journaling, and AI assistant — exactly enough to stay organized.

- **Description**: See `STORE_DESCRIPTIONS.md`
- **Keywords** (100 chars, comma-separated):
  > kanban,planner,journal,productivity,tasks,notes,daily,learning,priority,matrix,AI

- **Support URL**: Your website or GitHub pages
- **Marketing URL**: Optional
- **Privacy Policy URL**: Required (see PRIVACY_POLICY.md)

### Screenshots Required:

| Device | Size | Required |
|--------|------|----------|
| iPhone 6.7" (15 Pro Max) | 1290x2796 | Yes (mandatory) |
| iPhone 6.5" (11 Pro Max) | 1242x2688 | Yes (mandatory) |
| iPhone 5.5" (8 Plus) | 1242x2208 | Optional |
| iPad Pro 12.9" | 2048x2732 | If supporting iPad |
| iPad Pro 11" | 1668x2388 | If supporting iPad |

**Minimum 3 screenshots per required size, maximum 10.**

Screenshots to capture:
1. Home screen — daily summary
2. Kanban board
3. Eisenhower Matrix view
4. Daily Dashboard
5. AI Chat assistant
6. Notebook/Journal
7. Project creation

---

## Step 7: Build & Archive (on Mac)

### Build the iOS app:
```bash
# In the project root
./gradlew iosArm64Binaries

# Then open in Xcode
open iosApp/iosApp.xcodeproj
```

### Archive in Xcode:
1. Select "Any iOS Device (arm64)" as destination
2. Product > Archive
3. Wait for archive to complete
4. In Organizer: Distribute App > App Store Connect
5. Upload

### Or via command line:
```bash
xcodebuild -workspace iosApp/iosApp.xcworkspace \
  -scheme iosApp \
  -configuration Release \
  -archivePath build/iosApp.xcarchive \
  archive

xcodebuild -exportArchive \
  -archivePath build/iosApp.xcarchive \
  -exportOptionsPlist ExportOptions.plist \
  -exportPath build/iosExport
```

---

## Step 8: Submit for Review

1. In App Store Connect, select your build
2. Fill in "What's New" / Release Notes:
   > Initial release: Kanban boards, Eisenhower priority matrix, daily planning, journaling, learning tracker, and AI chat assistant.
3. Set release option:
   - **Manually release** (recommended for first launch)
   - Or "Automatically release"
4. Add review notes:
   > Test account: admin@theultimatenote.dev / [password]
   > The app requires Firebase authentication. Use the test account or create a new account with email signup.
5. Submit for review

---

## Step 9: Review Timeline

- Average: 24-48 hours (can be up to 7 days)
- Expedited review available for critical fixes
- App Review Board for appeals

---

## Step 10: Sign in with Apple Requirement

**IMPORTANT**: Apple requires any app with third-party sign-in (Google) to ALSO offer Sign in with Apple.

### Implementation needed:
1. Enable "Sign in with Apple" capability in Xcode
2. Add Apple Sign-In button on login screen
3. Handle Apple ID credentials
4. This is a **hard requirement** — app WILL be rejected without it

### Status: NOT YET IMPLEMENTED
This must be added before iOS submission.

---

## Common Rejection Reasons & Prevention

| Issue | Our Status | Action Needed |
|-------|-----------|---------------|
| Missing Sign in with Apple | Not implemented | Must add before iOS submit |
| Login wall without guest mode | Requires login | Provide test creds in review notes |
| Incomplete functionality | MVP complete | Test all features |
| Privacy policy missing/wrong | Included | Host at public URL |
| Crash on launch | Must test | Test on real devices |
| Metadata issues | Prepare carefully | Follow guidelines exactly |
| In-app purchase misuse | No IAP yet | N/A |
| IPv6 compatibility | Firebase handles | Should work |

---

## App Store Guidelines Key Points

1. **4.2 Minimum Functionality**: App must provide enough value. Our app has multiple features (Kanban, Matrix, Daily, Notebooks, AI) — this is well covered.
2. **4.3 Spam**: Don't submit duplicates or thin apps. Not applicable.
3. **5.1 Privacy**: Must have privacy policy, must declare data collection accurately.
4. **5.1.1 Data Collection**: Only collect what's needed. We only collect what's necessary for app function.
5. **4.0 Design**: Must use Apple UI paradigms properly. Compose Multiplatform handles this.
6. **2.1 Performance**: Must work without crashes. Test thoroughly.

---

## iOS-Specific Code Considerations

1. **Push Notifications**: Need APNs certificate configured in Firebase
2. **Sign in with Apple**: Must implement (see above)
3. **App Transport Security**: Already using HTTPS, compliant
4. **Background execution**: Declare if needed for notifications
5. **Universal Links**: Optional, for deep linking

---

## Post-Launch Checklist

- [ ] Monitor App Store Connect for crash reports
- [ ] Respond to App Store reviews
- [ ] Set up TestFlight for beta testing future versions
- [ ] Plan update cadence (bi-weekly recommended)
- [ ] Monitor App Store rating and reviews
