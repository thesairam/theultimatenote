# Privacy Policy

**The Ultimate Note**
**Last Updated: June 17, 2026**

---

## Introduction

The Ultimate Note ("we," "our," or "us") is committed to protecting your privacy. This Privacy Policy explains how we collect, use, and safeguard your information when you use our mobile application ("the App").

By using the App, you agree to the collection and use of information in accordance with this policy.

---

## Information We Collect

### Information You Provide

- **Account Information**: When you create an account, we collect your email address and display name. If you sign in with Google, we receive your name and email from your Google account.
- **Profile Information** (optional): Name, hobbies, areas of focus, skillset, and inspirational figures you choose to add to your profile.
- **User-Generated Content**: Tasks, projects, Kanban boards, notebooks, journal entries, and other content you create within the App.
- **AI Chat Messages**: Messages you send to and receive from the AI assistant are stored to maintain conversation history.

### Information We Do NOT Collect

- Location data
- Contacts or address book
- Photos, camera, or microphone data
- Financial or payment information
- Health or fitness data
- Browsing history
- Device advertising identifiers
- Call logs or SMS

### Automatically Collected Information

- **Crash Reports**: We use Firebase Crashlytics to collect anonymous crash data to improve app stability. This includes device type, operating system version, and crash stack traces. No personal data is included.

---

## How We Use Your Information

We use your information solely to:

1. **Provide App Functionality**: Store your tasks, projects, notebooks, and settings so they are available across sessions and devices.
2. **Authentication**: Verify your identity and maintain your session.
3. **AI Features**: Send your chat messages to AI providers (Groq, Google Gemini) to generate responses. Only the current conversation context is sent — no other personal data.
4. **Notifications**: Send you task reminders and motivational quotes at times you configure.
5. **Improve the App**: Use anonymous crash data to fix bugs and improve performance.

---

## Third-Party Services

The App uses the following third-party services:

| Service | Purpose | Data Shared | Privacy Policy |
|---------|---------|-------------|----------------|
| Firebase Authentication | User sign-in | Email, name | https://firebase.google.com/support/privacy |
| Cloud Firestore | Data storage | User-generated content | https://firebase.google.com/support/privacy |
| Firebase Crashlytics | Crash reporting | Anonymous crash data | https://firebase.google.com/support/privacy |
| Groq API | AI chat responses | Chat messages (current conversation only) | https://groq.com/privacy-policy |
| Google Gemini API | AI chat fallback | Chat messages (current conversation only) | https://ai.google.dev/terms |
| Google Sign-In | Authentication | Email, name (from Google account) | https://policies.google.com/privacy |

---

## Data Storage and Security

- All user data is stored in Google Cloud Firestore, scoped to your user account.
- Data is transmitted using HTTPS/TLS encryption.
- Data at rest is encrypted by Firebase/Google Cloud.
- We do not store your password — authentication is handled by Firebase Auth.
- Your data is not accessible to other users.

---

## Data Retention

- Your data is retained as long as your account is active.
- You may delete your account and all associated data at any time by contacting us (see below).
- Chat messages with the AI assistant are stored in your account and can be cleared from within the App.

---

## Data Sharing

**We do not sell, trade, or rent your personal information to third parties.**

We share data only:
- With the third-party services listed above, solely for the purposes described.
- If required by law, subpoena, or court order.
- To protect our rights, safety, or property.

---

## Your Rights

You have the right to:

1. **Access**: Request a copy of your personal data.
2. **Correction**: Update or correct your personal information through the app's Profile section.
3. **Deletion**: Request deletion of your account and all associated data.
4. **Portability**: Request your data in a portable format.
5. **Withdraw Consent**: Stop using the App at any time.

### For EU/EEA Users (GDPR)
You have additional rights under the General Data Protection Regulation, including the right to object to processing and the right to lodge a complaint with a supervisory authority.

### For California Users (CCPA)
You have the right to know what personal information is collected, request deletion, and opt out of the sale of personal information (we do not sell personal information).

To exercise any of these rights, contact us at the email below.

---

## Children's Privacy

The App is not directed at children under the age of 13. We do not knowingly collect personal information from children under 13. If you believe we have collected information from a child under 13, please contact us and we will promptly delete it.

---

## Notifications

The App sends local push notifications for:
- Task reminders at times you set
- Daily motivational quotes (if configured)

These are local notifications scheduled on your device. We do not send marketing push notifications.

---

## Changes to This Privacy Policy

We may update this Privacy Policy from time to time. We will notify you of any changes by updating the "Last Updated" date at the top of this policy. Continued use of the App after changes constitutes acceptance of the updated policy.

---

## Contact Us

If you have any questions about this Privacy Policy or wish to exercise your data rights, contact us at:

**Email**: sairam.25@live.com
**App**: The Ultimate Note
**Developer**: Sairam

---

## Hosting This Policy

This privacy policy must be hosted at a publicly accessible URL. Options:

1. **GitHub Pages** (free): Push this file to a `gh-pages` branch or `/docs` folder → enable Pages in repo settings → URL: `https://thesairam.github.io/theultimatenote/privacy-policy`
2. **Firebase Hosting** (free tier): `firebase init hosting` → deploy this as `privacy-policy.html`
3. **Any static hosting**: Netlify, Vercel, or your own domain

Both Google Play and Apple App Store require you to provide this URL during submission.
