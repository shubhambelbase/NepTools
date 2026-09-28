### NepTools v2.8.8 (versionCode 47) - Release Notes

A correctness, performance, release-safety and accessibility pass. No new tools; this release makes
existing features correct and usable on real devices.

#### Fixed
- **Habit streak counter (important)**: The current-streak figure was being reset to 0 every morning
  before you logged the day, and stayed at 0 on any scheduled rest day. A run of completed days was
  discarded on screen. Streaks now carry over from yesterday, a day you have not logged yet no longer
  breaks the chain, and a scheduled rest day is treated as neutral rather than a miss.
- **Ekadashi and sacred tithi list**: Opening the screen, or changing year, froze the UI for several
  seconds while a full year of panchang calculations ran on the main thread. Calculation now happens
  in the background with a loading indicator, and each year is solved only once.
- **Habit heatmap and statistics**: The 52-week grid became progressively slower the more history you
  accumulated, because the full check-in log was re-parsed for every day on every refresh. The log is
  now indexed once and reused.
- **Background battery drain**: Leaving the sound level meter or the spy camera detector with the
  screen off no longer keeps a high-frequency sensor loop alive.
- **Cold start colour flash**: The window background followed the system dark setting while the app
  followed its own setting, producing a one-frame colour jump. Both now resolve from the same choice.
- **Default language**: A first run on an English phone opened in Nepali. The app now follows the
  device language until you choose otherwise.
- **Any app could crash NepTools**: The exported main activity accepted an unvalidated navigation
  route from any installed app. External routes are now validated against an allowlist.

#### Improved
- **System dark mode**: The app ignored the Android dark theme setting. Appearance is now a
  System / Light / Dark choice in Settings, defaulting to System.
- **Keyboard handling**: The on-screen keyboard covered the bottom of every form, hiding the field
  being typed into and the submit button.
- **Readability**: Secondary text contrast was below the WCAG AA minimum in light mode, which was
  hardest to read outdoors. Improved across the palette.
- **Confirmation before deleting**: Deleting a habit or subscription now asks first, since both
  permanently discard history.
- **Boot performance**: Restoring the daily date notification after a reboot no longer runs on the
  main thread.
- **Build reliability**: Gradle heap raised and compiler strategy corrected; emulator-only native code
  no longer ships in release builds.

#### Security
- **Release signing is now enforced**: If the release keystore is missing, the release build fails
  instead of quietly producing an APK signed with the publicly known Android debug key. Previously a
  missing keystore still produced a working, distributable build that anyone could impersonate.
- Obsolete APK v1 signing disabled.
- Removed a permission the app never used, and removed three debug-only broadcast handlers that any
  app on the device could have triggered.

#### Verification
- 50/50 unit tests pass, including 6 new regression tests for streak arithmetic.
- Debug build compiles with no errors; release-signing guard verified to fail closed.
- Manual on-device testing is still recommended, particularly for the habit streak values and the
  new appearance control.

- SHA-256: 5dce09a35600b69f365c89dd0abcead185ab699d18cc1dc11bac019a87dd1075
- Size: 9510064 bytes (9.07 MB)
- Signed with the release key: CN=NepTools, O=NepTools, L=Kathmandu, ST=Bagmati, C=NP
  Cert SHA-256: 59:F9:5B:14:D6:BE:15:1E:03:F8:7B:7D:CE:59:CC:D7:3D:97:9D:03:4F:FB:E7:D4:D9:EF:7C:13:72:7D:66:A9
- APK Signature Scheme: v2 + v3 (V1 disabled)
- ABIs: arm64-v8a, armeabi-v7a (emulator-only x86_64 no longer shipped)
- Updater: publish `app-release.apk.sha256` alongside the APK. The in-app updater
  refuses to auto-install when no checksum resolves, so this asset is required.
