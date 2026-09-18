package com.neptools.app

import android.app.Application
import com.neptools.app.astrology.data.AstroRepo
import com.neptools.app.astrology.ephemeris.HighPrecisionEphemeris
import com.neptools.app.core.data.PatroRepo
import com.neptools.app.core.data.PlacesRepo
import com.neptools.app.core.notification.NepaliDateNotificationManager
import com.neptools.app.core.notification.NepaliDateStickyService
import com.neptools.app.ui.theme.ThemePrefs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PatroApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ThemePrefs.load(this)
        PatroRepo.init(this)
        PlacesRepo.init(this)
        AstroRepo.init(this, HighPrecisionEphemeris(useTrueNode = true))
        com.neptools.app.core.notification.SmartAlertNotificationManager.createNotificationChannels(this)

        // Cold-start rule: nothing expensive on the main thread before the first frame.
        // The calendar JSON parse, festival validation, panchang computation, notification
        // posting and widget refresh used to run synchronously here and stalled the launch
        // animation on slower phones (frozen splash until the user tapped the screen).
        CoroutineScope(Dispatchers.IO).launch {
            // Pre-warm the BS calendar + festivals so the first composition never blocks on parsing.
            runCatching { PatroRepo.d }
            NepaliDateNotificationManager.updateNotification(this@PatroApp)
            NepaliDateStickyService.start(this@PatroApp)
            com.neptools.app.core.work.WorkScheduler.scheduleAll(this@PatroApp)
            com.neptools.app.core.reminder.ReminderHelper.rescheduleAll(this@PatroApp)
            com.neptools.app.core.util.EmergencySyncManager.init(this@PatroApp)
            // Advisory integrity audit. Results are surfaced in Settings; the app never
            // self-terminates, because rooted devices are a supported configuration.
            com.neptools.app.core.security.NepToolsSecurityGuard.refresh(this@PatroApp)
        }
    }
}
