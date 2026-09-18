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
        NepaliDateNotificationManager.updateNotification(this)
        NepaliDateStickyService.start(this)
        com.neptools.app.core.notification.SmartAlertNotificationManager.createNotificationChannels(this)

        // Asynchronously initialize background workers, reminders, and emergency cache to eliminate cold-start main thread jank
        CoroutineScope(Dispatchers.IO).launch {
            com.neptools.app.core.work.WorkScheduler.scheduleAll(this@PatroApp)
            com.neptools.app.core.reminder.ReminderHelper.rescheduleAll(this@PatroApp)
            com.neptools.app.core.util.EmergencySyncManager.init(this@PatroApp)
        }
    }
}
