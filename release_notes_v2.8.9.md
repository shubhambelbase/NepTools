### NepTools v2.8.9 (versionCode 48) - Release Notes

This release restores full navigation access across all tools, refines daily horoscope display, fixes memory cache invalidation on habit deletion, and aligns notification preference keys.

#### Fixed
- **Tools Navigation Gate Failure**: Fixed an issue where tapping any utility from the Tools screen or Home screen silently dropped navigation because of an overly restrictive internal allowlist. All 50+ tool routes and dynamic date screens are now fully accessible.
- **Habit Log Cache Invalidation**: Deleting a habit previously deleted its persistent logs in storage but failed to clear the in-memory date index, causing deleted logs to linger until process termination. Cache invalidation is now properly invoked upon deletion.
- **Subscription Notification Setting Sync**: Resolved an inconsistency where turning subscription notifications on/off in Settings saved to `notif_sub` but startup preferences read `notif_subs`, causing user toggles to be ignored after app restart.
- **Ekadashi Year Switch State**: Keyed events state to `selectedYear` so switching years immediately resets the active cache and displays the proper loading state.

#### Changed
- **Daily Horoscope Interface**: Removed the redundant top-bar refresh button and associated random seed override; daily readings now deterministically generate based on the user's selected rashi and current date.
