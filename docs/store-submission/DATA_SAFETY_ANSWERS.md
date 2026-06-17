# Data Safety Form Answers

Pre-filled answers for Google Play Data Safety and Apple App Privacy forms.

---

## Google Play — Data Safety Section

### Overview Questions

**Q: Does your app collect or share any of the required user data types?**
A: Yes

**Q: Is all of the user data collected by your app encrypted in transit?**
A: Yes (all communication uses HTTPS/TLS; Firebase enforces encryption)

**Q: Do you provide a way for users to request that their data is deleted?**
A: Yes (users can request account deletion via email to sairam.25@live.com)

---

### Data Types — What We Collect

#### Personal Info

| Data type | Collected? | Shared? | Ephemeral? | Required? | Purpose |
|-----------|-----------|---------|------------|-----------|---------|
| Name | Yes | No | No | Optional | App functionality (profile display) |
| Email address | Yes | No | No | Required | Account management, authentication |
| User IDs | Yes | No | No | Required | App functionality (Firebase UID) |
| Phone number | No | — | — | — | — |
| Address | No | — | — | — | — |

#### App Activity

| Data type | Collected? | Shared? | Ephemeral? | Required? | Purpose |
|-----------|-----------|---------|------------|-----------|---------|
| App interactions | Yes | No | No | Required | App functionality (tasks, projects, boards) |
| In-app search history | No | — | — | — | — |
| Installed apps | No | — | — | — | — |
| Other user-generated content | Yes | No | No | Required | App functionality (notebooks, journal) |

#### App Info and Performance

| Data type | Collected? | Shared? | Ephemeral? | Required? | Purpose |
|-----------|-----------|---------|------------|-----------|---------|
| Crash logs | Yes | No | Yes | Required | Analytics (Firebase Crashlytics) |
| Diagnostics | Yes | No | Yes | Required | Analytics |
| Other app performance data | No | — | — | — | — |

#### NOT Collected

- Location (fine, coarse, or approximate)
- Financial info (purchase history, credit info, etc.)
- Health and fitness
- Messages (SMS, email, other)
- Photos and videos
- Audio files
- Files and docs (stored in-app, not from device)
- Calendar
- Contacts
- Device or other IDs (no ad ID collected)
- Web browsing data

---

### Purposes for Each Data Type

**Name, Email:**
- App functionality: Display name in profile, authentication
- Account management: Sign-in, password reset

**User-generated content (tasks, projects, notes, chat):**
- App functionality: Core feature — storing and syncing user's productivity data

**Crash logs:**
- Analytics: Identify and fix crashes to improve stability

---

### Additional Questions

**Q: Does your app use data for advertising purposes?**
A: No

**Q: Does your app use data for personalized advertising?**
A: No

**Q: Does your app contain ads?**
A: No

**Q: Is your app a financial service?**
A: No

**Q: Is your app a government app?**
A: No

---

## Apple App Store — App Privacy

### Data Used to Track You
**None** — We do not track users across apps or websites.

### Data Linked to You

| Data type | Purpose |
|-----------|---------|
| Contact Info — Email Address | App Functionality |
| Contact Info — Name | App Functionality |
| User Content — Other User Content | App Functionality |

### Data Not Linked to You

| Data type | Purpose |
|-----------|---------|
| Diagnostics — Crash Data | App Functionality |

### Data Not Collected
Everything else — see the "NOT Collected" section above.

---

## AI-Specific Data Handling

Both stores may ask about AI data handling. Answers:

**Q: Does your app send user data to external AI services?**
A: Yes — chat messages are sent to Groq and/or Google Gemini to generate AI responses.

**Q: What data is sent?**
A: Only the current chat conversation messages (user messages and AI responses) and a system context describing the user's projects and tasks (names/titles only, no personal information beyond what the user typed).

**Q: Is AI data used for training?**
A: 
- Groq: As per their terms, data is not used for model training.
- Gemini: As per Google AI Studio terms, data through API is not used for training.

**Q: Is AI data stored by third parties?**
A: Data may be temporarily processed by Groq/Gemini servers but is not permanently stored by them per their API terms of service.

---

## Permissions Justification

For both stores, explain why each permission is needed:

| Permission | Justification |
|-----------|---------------|
| Internet | Sync data with Firebase, communicate with AI APIs, authentication |
| Push Notifications | Local reminders for scheduled tasks and daily motivation quotes |
| Exact Alarms | Schedule task reminders at precise times the user sets |
| Boot Completed | Reschedule task reminder alarms after device restart |

None of these permissions involve accessing user's personal device data (contacts, photos, location, etc.).
