# Google Play Store Submission — Step by Step

## Prerequisites

- [ ] Google Play Developer Account ($25 one-time fee) — https://play.google.com/console
- [ ] Signed release APK/AAB (Android App Bundle preferred)
- [ ] App icon: 512x512 PNG (no transparency, no rounded corners)
- [ ] Feature graphic: 1024x500 PNG/JPG
- [ ] At least 2 screenshots per device type (phone mandatory)
- [ ] Privacy Policy hosted at a public URL
- [ ] Email address for store listing contact

---

## Step 1: Create Developer Account

1. Go to https://play.google.com/console
2. Sign in with your Google account
3. Pay $25 registration fee
4. Fill in developer profile (name, email, website)
5. Wait for verification (can take 24-48 hours)

---

## Step 2: Create App in Play Console

1. Click "Create app"
2. Fill in:
   - App name: **The Ultimate Note**
   - Default language: English (US)
   - App or game: **App**
   - Free or paid: **Free**
3. Accept declarations and click "Create app"

---

## Step 3: Store Listing

### Main Store Listing

- **App name**: The Ultimate Note
- **Short description** (80 chars max):
  > Plan, journal, and learn — Kanban boards, daily planner, and AI assistant in one.
- **Full description** (4000 chars max): See `STORE_DESCRIPTIONS.md`
- **App icon**: 512x512 PNG
- **Feature graphic**: 1024x500 PNG
- **Screenshots**: Minimum 2, recommended 4-8 per device type
  - Phone: 16:9 or 9:16, min 320px, max 3840px
  - Tablet (optional): same ratios
  - Recommended resolution: 1080x1920 or 1920x1080

### Screenshots to capture:
1. Home screen with daily summary
2. Kanban board view
3. Eisenhower Matrix view (toggle)
4. Daily Dashboard
5. AI Chat
6. Notebook/Journal page
7. Project creation

---

## Step 4: Content Rating (IARC Questionnaire)

Navigate to: Policy > App content > Content rating

Answer the questionnaire:
- **Violence**: No
- **Sexual content**: No
- **Language**: No
- **Controlled substances**: No
- **Miscellaneous**: 
  - User interaction: Yes (AI chat)
  - Shares location: No
  - Shares personal info: No (stays in user's Firebase)
  - Digital purchases: No (for MVP, free tier)

**Expected rating**: PEGI 3 / Everyone

---

## Step 5: Data Safety Form

Navigate to: Policy > App content > Data safety

### Data collected:

| Data type | Collected | Shared | Purpose |
|-----------|-----------|--------|---------|
| Email address | Yes | No | Account management |
| Name | Yes | No | Account management |
| App interactions | Yes | No | App functionality |
| User-generated content | Yes | No | App functionality |

### Declarations:
- **Is data encrypted in transit?** Yes (HTTPS/TLS + Firebase)
- **Can users request data deletion?** Yes (account deletion)
- **Does your app follow the Families Policy?** Not targeted at children

### Detailed answers:

**Personal info collected:**
- Email (required for auth)
- Name (optional, for profile)

**App activity collected:**
- Tasks, projects, notes (user-generated, stored in Firebase)
- AI chat messages (stored in Firebase for conversation history)

**Data NOT collected:**
- Location
- Financial info
- Health info
- Device identifiers for advertising
- Photos/videos
- Contacts
- Call logs

---

## Step 6: App Access (if needed)

If your app requires login to review:
1. Go to: Policy > App content > App access
2. Select "All or some functionality is restricted"
3. Provide test credentials:
   - Email: `admin@theultimatenote.dev`
   - Password: (provide reviewer password)
4. Add instructions: "Use provided credentials to sign in. App requires authentication to access all features."

---

## Step 7: Target Audience & Ads

- **Target age group**: 18+ (or "All ages" if no mature content)
- **Contains ads**: No
- **Is this a news app?**: No
- **Government apps declaration**: No

---

## Step 8: Build Release

### Generate Signing Key (first time only):
```bash
keytool -genkey -v -keystore release-key.jks \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias theultimatenote
```

### Create release AAB:
```bash
./gradlew bundleRelease
```

Output: `composeApp/build/outputs/bundle/release/composeApp-release.aab`

### Or use Play App Signing (recommended):
1. In Play Console: Setup > App signing
2. Choose "Let Google manage and protect your app signing key"
3. Upload your upload key (or let Google generate one)

---

## Step 9: Create Release Track

1. Go to: Release > Production (or Internal testing first)
2. Click "Create new release"
3. Upload the AAB
4. Add release notes:
   > Initial release of The Ultimate Note — your all-in-one productivity companion with Kanban boards, daily planning, journaling, and AI assistance.
5. Review and roll out

### Recommended testing progression:
1. **Internal testing** (up to 100 testers, instant approval)
2. **Closed testing** (invite testers, 2-3 day review)
3. **Open testing** (public opt-in, 2-3 day review)
4. **Production** (full release, 3-7 day review)

---

## Step 10: Review Timeline

- First submission: 3-7 business days
- Updates after approval: 1-3 days
- Rejections come with specific reasons

---

## Common Rejection Reasons & Prevention

| Issue | Our Status |
|-------|-----------|
| Missing privacy policy | Included (see PRIVACY_POLICY.md) |
| Login required without test creds | Provide in App Access |
| Crash on launch | Test on multiple API levels |
| Misleading description | Keep description accurate |
| Missing data safety form | Filled above |
| Deceptive permissions | All permissions justified |
| Broken functionality | Test all flows before submit |

---

## Permissions Justification (for reviewer)

| Permission | Justification |
|-----------|---------------|
| INTERNET | Required for Firebase sync, AI chat, authentication |
| POST_NOTIFICATIONS | Task reminders and daily motivation quotes |
| SCHEDULE_EXACT_ALARM | Precise task reminder scheduling |
| RECEIVE_BOOT_COMPLETED | Reschedule alarms after device restart |

---

## Post-Launch Checklist

- [ ] Monitor crash reports in Play Console
- [ ] Respond to user reviews within 24h
- [ ] Set up Firebase Crashlytics dashboard
- [ ] Plan update cadence (bi-weekly recommended)
