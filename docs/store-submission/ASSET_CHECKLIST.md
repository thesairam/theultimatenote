# Visual Assets Checklist

All images/graphics needed for store submission.

---

## App Icons

### Android
Already have: `composeApp/src/androidMain/res/drawable/ic_launcher.xml`

For Play Store upload:
- [ ] **512x512 PNG** — Hi-res icon (no transparency, no rounded corners — store applies masking)
- Must match the app's launcher icon design
- Format: PNG, 32-bit with alpha
- File size: up to 1 MB

### iOS
- [ ] **1024x1024 PNG** — App Store icon (no alpha/transparency, no rounded corners)
- Plus all required sizes in Xcode asset catalog:
  - 180x180 (iPhone @3x)
  - 120x120 (iPhone @2x)
  - 167x167 (iPad Pro @2x)
  - 152x152 (iPad @2x)
  - 76x76 (iPad @1x)
  - 40x40, 60x60, 58x58, 80x80, 87x87, 120x120 (various contexts)

**Tip**: Design the 1024x1024 version, then use a tool like App Icon Generator (appicon.co) to auto-generate all sizes.

---

## Feature Graphic (Google Play only)

- [ ] **1024x500 PNG or JPG**
- Displayed at top of store listing
- Should include:
  - App name: "The Ultimate Note"
  - Tagline: "Plan. Prioritize. Learn. Journal. Grow."
  - App icon or UI preview
  - Use the deep emerald + gold color scheme
- No excessive text (Google may reject)

---

## Screenshots

### Google Play — Phone (Required)

Minimum: 2 | Recommended: 4-8 | Maximum: 8

Specs:
- Aspect ratio: 16:9 or 9:16
- Min dimension: 320px
- Max dimension: 3840px
- Recommended: **1080x1920** (portrait) or **1920x1080** (landscape)
- Format: PNG or JPG (no alpha)

### Google Play — Tablet (Optional but recommended)

- 7-inch: 1080x1920 or similar
- 10-inch: 1920x1200 or similar

### Apple App Store — iPhone (Required)

Must provide for these sizes:

| Device | Resolution | Required? |
|--------|-----------|-----------|
| iPhone 6.7" (15 Pro Max, 16 Plus) | 1290x2796 | Yes |
| iPhone 6.5" (11 Pro Max, XS Max) | 1242x2688 | Yes |
| iPhone 5.5" (8 Plus, 7 Plus, 6s Plus) | 1242x2208 | Only if supporting |

Minimum: 3 per size | Maximum: 10

### Apple App Store — iPad (If supporting iPad)

| Device | Resolution | Required? |
|--------|-----------|-----------|
| iPad Pro 12.9" (6th gen) | 2048x2732 | If iPad supported |
| iPad Pro 11" | 1668x2388 | If iPad supported |

---

## Screenshots to Capture

Take these from the emulator/device:

| # | Screen | Description | Key elements to show |
|---|--------|-------------|---------------------|
| 1 | Home | Command center | Greeting, today's tasks, progress card |
| 2 | Kanban Board | Project board | Columns with task cards |
| 3 | Matrix View | Priority matrix | Four quadrants with tasks |
| 4 | Daily Dashboard | Daily planner | Recurring + temporary tasks |
| 5 | AI Chat | Assistant | Conversation with task suggestions |
| 6 | Notebook | Journaling | Page with formatted content |
| 7 | Projects List | All projects | Project cards with icons |

### Screenshot Tips

- Use a clean device/emulator state with sample data
- Remove status bar clutter (use Demo Mode on Android: `adb shell settings put global sysui_demo_allowed 1`)
- Add descriptive text/frames around screenshots using tools like:
  - **Canva** (free)
  - **Screenshots.pro** (free)
  - **Figma** (free)
  - **AppMockUp** (free)
- Use consistent framing and colors matching the emerald+gold theme
- Show real, readable content — not lorem ipsum

---

## Promotional Video (Optional)

### Google Play
- YouTube URL
- 30 seconds to 2 minutes
- Landscape preferred
- Autoplays in store listing

### Apple App Store
- App Preview video
- 15-30 seconds
- Must be captured from the app (screen recording)
- Up to 3 previews per device size

---

## Tools for Creating Assets

| Tool | Use | Cost |
|------|-----|------|
| Figma | Icons, feature graphic, screenshot frames | Free |
| Canva | Feature graphic, screenshot frames | Free |
| Android Studio | Emulator screenshots | Free |
| Xcode Simulator | iOS screenshots | Free |
| appicon.co | Generate all icon sizes | Free |
| AppMockUp | Device frame mockups | Free |
| Screenshots.pro | Store screenshot designer | Free |
