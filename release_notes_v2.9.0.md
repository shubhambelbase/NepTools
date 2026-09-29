### NepTools v2.9.0 (versionCode 49) - Release Notes

This release resolves core issues in calendar event notifications, background alarm restoration across reboots, data restore cache invalidation, numeral preference persistence, and widget synchronization.

#### Fixed
- **Calendar Event Notification Fidelity & Collision**: Fixed an issue where calendar event reminder notifications showed a generic fallback label instead of the user's event title and note, and caused multiple event notifications to overwrite each other. Notifications now surface title, note, specific day deep-link, and unique IDs.
- **Reboot & Timezone Alarm Loss**: Restored the persistence of user calendar event alarms across device reboots, time changes, and app updates by adding `rescheduleAll` to `UserEventManager` wired into `BootCompletedReceiver` and application cold start.
- **Data Restore Cache Stale State**: Fixed an issue where restoring habits from a backup file wrote directly to disk without purging the in-memory `logsByDateCache` in `HabitRepository`, causing stale heatmap and streak data to persist. Also added automatic alarm rescheduling for restored calendar events.
- **Nepali Digits Preference Cold-Start Persistence**: Fixed a regression in `ThemePrefs.load` where the stored `"np_digits"` boolean was ignored and overwritten on every app launch, ensuring the user's numeral format choice is retained.
- **Midnight Monthly Calendar Widget Rollover**: Added `NepToolsMonthWidgetProvider.updateAllWidgets` to the midnight notification rollover routine, ensuring homescreen month calendar widgets accurately update the highlighted today cell at midnight.
